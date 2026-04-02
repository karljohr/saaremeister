package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.CaptureZone;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.listener.ServerListener;
import ee.kool.saaremeister.server.lobby.Lobby;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import com.badlogic.gdx.physics.box2d.World;
import message.dto.CaptureZoneState;
import message.dto.StaticBodyState;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static constant.Constants.*;

/**
 * Represents the management and game logic of a single game instance on the server.
 */
public class GameInstance extends Thread {

    private final ServerListener serverListener;
    private final LobbyManager lobbyManager;
    private final int lobbyId;
    private final GameStateHandler gameStateHandler = new GameStateHandler();
    private final Set<Connection> connections = Collections.synchronizedSet(new HashSet<>());
    private final List<Player> players = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private final List<CaptureZone> captureZones = new ArrayList<>();
    private final Map<Integer, Player> activePlayers = new ConcurrentHashMap<>();
    private final World world;
    private List<StaticBodyState> staticBodyData;
    private final BuildHandler buildHandler;
    private final MatchRules matchRules = new MatchRules();

    public GameInstance(ServerListener serverListener, LobbyManager lobbyManager, int lobbyId) {
        this.serverListener = serverListener;
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
        this.world = new World(new Vector2(0, 0), true);

        Box2dWorldGenerator box2dWorldGenerator = new Box2dWorldGenerator();
        staticBodyData = box2dWorldGenerator.initializeWorld(world);

        buildHandler = new BuildHandler();

        CaptureZoneLoader captureZoneLoader = new CaptureZoneLoader();
        captureZones.addAll(captureZoneLoader.loadCaptureZones());

    }

    public boolean hasEnoughPlayers() {
        return players.size() == PLAYER_COUNT_INGAME_LIMIT;
    }

    public void addConnection(com.esotericsoftware.kryonet.Connection connection) {
        if (hasEnoughPlayers()) {
            Log.info("Cannot add player, game is full.");
            return;
        }

        Player newPlayer = new Player(connection, this, world, buildHandler);
        newPlayer.setUsername(lobbyManager.getUsername(connection));
        activePlayers.put(connection.getID(), newPlayer);
        players.add(newPlayer);
        connections.add(connection);

        connection.addListener(new com.esotericsoftware.kryonet.Listener() {
            @Override
            public void received(com.esotericsoftware.kryonet.Connection conn, Object object) {
                if (object instanceof message.ShopPurchaseMessage) {
                    message.ShopPurchaseMessage msg = (message.ShopPurchaseMessage) object;

                    Player p = activePlayers.get(conn.getID());
                    if (p != null) {
                        message.dto.ShopItem item = common.ShopRegistry.getItemById(msg.getItemId());

                        if (item != null) {
                            // Use the price defined in the ShopItem
                            boolean success = p.attemptPurchase(item.getPrice());

                            if (success) {
                                Log.info("Purchased " + item.getName() + " for " + p.getUsername() + ". New balance: " + p.getMoney());
                                // TODO: Give the item/benefit to the player here (to the hotbar)
                            } else {
                                Log.info(p.getUsername() + " didn't have enough money for " + item.getName());
                            }
                        } else {
                            Log.error("Received purchase request for non-existent item ID: " + msg.getItemId());
                        }
                    } else {
                        Log.error("Player object not found for ID " + conn.getID());
                    }
                }
            }
        });

        // Debug hitbox info
        StaticBodyState[] arr = staticBodyData.toArray(new StaticBodyState[0]);
        connection.sendTCP(arr);

        if (hasEnoughPlayers()) {
            gameStateHandler.setAllPlayersHaveJoined(true);
        }
    }

    public void removeConnection(com.esotericsoftware.kryonet.Connection connection) {
        this.connections.remove(connection);
        Player p = activePlayers.remove(connection.getID());
        if (p != null) {
            players.remove(p);
            captureZones.forEach(zone -> zone.clearPlayer(p.getId()));
        }

        var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());
        connections.forEach(c -> c.sendTCP(gameStateMessage));

        Log.info("Player left the game: " + connection.getID());
    }

    private void disposeGame() {
        players.forEach(Player::dispose);
        connections.clear();

        Lobby lobby = lobbyManager.getLobby(lobbyId);
        if (lobby != null) {
            lobby.disposeGame();
            Log.info("Game disposed in lobby " + lobbyId);
        } else {
            Log.warn("Lobby not found for game " + lobbyId);
        }
    }

    @Override
    public void run() {
        boolean isGameRunning = true;
        System.out.println("Game is running: " + isGameRunning);
        while (isGameRunning) {
            float deltaTime = 1f / GAME_TICK_RATE;
            gameStateHandler.incrementGameTimeIfPlayersPresent();

            // Step Box2D simulation so bodies move.
            // Timestep based on tick rate; velocity/position iterations are standard defaults.
            world.step(deltaTime, 6, 2);

            // Update game objects.
            bullets.forEach(Bullet::update);
            players.forEach(Player::updateFromPhysics);
            var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());

            // Capture Zone updates according to the players (not) inside it.
            captureZones.forEach(zone -> zone.update(players, deltaTime));
            ArrayList<CaptureZoneState> zoneStates = captureZones.stream()
                    .map(CaptureZone::toState)
                    .collect(Collectors.toCollection(ArrayList::new));
            gameStateHandler.setCaptureZoneStates(zoneStates);

            // Send the game state to all clients.
            var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets);
            connections.forEach(connection -> connection.sendUDP(gameStateMessage));

            // If there is a match end reason, stop the game.
            MatchRules.MatchEndReason endReason = matchRules.determineEndReason(
                    captureZones, connections, gameStateHandler.getGameTime()
            );
            if (endReason != MatchRules.MatchEndReason.NONE) {
                int winnerPlayerId = matchRules.getGameWinnerId();
                gameStateHandler.setWinnerPlayerId(winnerPlayerId);
                gameStateHandler.setGameOver(true);
                var finalGameStateMessage = gameStateHandler.getGameStateMessage(players, bullets);
                switch (endReason) {
                    case NO_PLAYERS_CONNECTED -> Log.info("No players connected, stopping game.");
                    case ALL_ZONES_CAPTURED -> Log.info("All zones captured by one player, stopping game.");
                    case TIME_LIMIT_REACHED_WITH_RESULT -> Log.info("Time limit reached, someone won, stopping game.");
                    case TIME_LIMIT_REACHED_WITH_DRAW -> Log.info("Time limit reached, no one won, stopping game.");
                }
                connections.forEach(connection -> connection.sendTCP(finalGameStateMessage));
                disposeGame();
                isGameRunning = false;
                lobbyManager.disposeLobby(lobbyId);
            }

            // Print player positions in the console log.
            players.forEach(player -> System.out.println(
                    player.getUsername() + ": " + player.getX() + " ; " + player.getY()
            ));

            // Sleep for 1 second to maintain a constant tick rate.
            try {
                Thread.sleep(Duration.ofMillis(1000 / GAME_TICK_RATE));
            } catch (InterruptedException e) {
                Log.error("Game loop sleep interrupted", e);
            }
        }
    }
}
