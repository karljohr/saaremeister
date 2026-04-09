package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import constant.Category;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.object.*;
import ee.kool.saaremeister.server.listener.ServerListener;
import ee.kool.saaremeister.server.lobby.Lobby;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import com.badlogic.gdx.physics.box2d.World;
import lombok.Getter;
import message.dto.CaptureZoneState;
import message.dto.PlayerRole;
import message.dto.StaticBodyState;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
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
    @Getter
    private final List<Player> players = new ArrayList<>();
    private final Queue<Bullet> pendingBullets = new ConcurrentLinkedDeque<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<CaptureZone> captureZones = new ArrayList<>();
    private final Map<Integer, Player> activePlayers = new ConcurrentHashMap<>();
    private final World world;
    private List<StaticBodyState> staticBodyData;
    private final BuildHandler buildHandler;
    private final MatchRules matchRules = new MatchRules();
    private final BulletCollisionHandler bulletCollisionHandler;

    /**
     * Creates a new game instance.
     */
    public GameInstance(ServerListener serverListener, LobbyManager lobbyManager, int lobbyId) {
        this.serverListener = serverListener;
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
        this.world = new World(new Vector2(0, 0), true);
        this.bulletCollisionHandler = new BulletCollisionHandler(world);

        Box2dWorldGenerator box2dWorldGenerator = new Box2dWorldGenerator();
        staticBodyData = box2dWorldGenerator.initializeWorld(world);

        buildHandler = new BuildHandler(world, this);

        CaptureZoneLoader captureZoneLoader = new CaptureZoneLoader();
        captureZones.addAll(captureZoneLoader.loadCaptureZones());
    }

    public void addLobbyConnections(Collection<Connection> connections) {
        List<Connection> connectionList = new ArrayList<>(connections);
        if (connectionList.isEmpty()) return;

        int masterIndex = ThreadLocalRandom.current().nextInt(connectionList.size());
        for (int i = 0; i < connectionList.size(); i++) {
            PlayerRole role = (i == masterIndex) ? PlayerRole.MASTER : PlayerRole.OFFICER;
            addConnection(connectionList.get(i), role);
        }
    }

    /**
     * Returns true if the game instance has reached the maximum player limit.
     */
    public boolean hasEnoughPlayers() {
        return players.size() == PLAYER_COUNT_INGAME_LIMIT;
    }

    /**
     * Adds a new player to the game instance.
     */
    private void addConnection(Connection connection, PlayerRole role) {
        if (hasEnoughPlayers()) {
            Log.info("Cannot add player, game is full.");
            return;
        }

        Player newPlayer = switch (role) {
            case MASTER -> new Master(connection, this, world, buildHandler);
            case OFFICER -> new Officer(connection, this, world, buildHandler);
        };
        newPlayer.setUsername(lobbyManager.getUsername(connection));
        activePlayers.put(connection.getID(), newPlayer);
        players.add(newPlayer);
        connections.add(connection);

        connection.addListener(new com.esotericsoftware.kryonet.Listener() {
            /**
             * Handles incoming shop messages from the client.
             */
            @Override
            public void received(com.esotericsoftware.kryonet.Connection conn, Object object) {
                if (object instanceof message.ShopPurchaseMessage msg) {
                    Player p = activePlayers.get(conn.getID());
                    if (p != null) {
                        message.dto.ShopItem item = common.ShopRegistry.getItemById(msg.getItemId());

                        if (item == null) return;

                        common.ArmorType itemArmor = common.ArmorType.fromName(item.getName());
                        boolean isArmor = (itemArmor != common.ArmorType.NONE);

                        // Check if item needs a slot in hotbar
                        boolean needsHotbarSpace = (item.getCategory() == Category.RELVAD || item.getCategory() == Category.TARVIKUD) && !isArmor;

                        // If hotbar full, dont deduct money
                        if (needsHotbarSpace && p.isHotbarFull()) {
                            Log.info(p.getUsername() + " hotbar on täis, ost tühistatud.");
                            return;
                        }

                        // Now deduct money
                        if (p.attemptPurchase(item.getPrice())) {
                            if (isArmor) {
                                p.applyArmor(itemArmor.getLevel(), itemArmor.getDefenseValue());
                            } else {
                                if (needsHotbarSpace) {
                                    p.addHotbarItem();
                                }

                                if (item.getCategory() == Category.EHITISED) {
                                    p.addBuildingPermit(BuildingInfo.getTileId(item.getId()));
                                }
                            }

                            message.ShopPurchaseMessage response = new message.ShopPurchaseMessage();
                            response.setItemId(item.getId());
                            conn.sendTCP(response);
                        }
                    }
                }

                if (object instanceof message.ItemUsedMessage) {
                    Player p = activePlayers.get(conn.getID());
                    if (p != null) {
                        p.removeHotbarItem();
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

    /**
     * Removes a player from the game instance.
     */
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

    /**
     * Disposes the game instance, removing all players and connections.
     */
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

    /**
     * Adds a bullet to the game instance when shot (requested) by a client.
     */
    public void addBullet(Bullet bullet) {
        pendingBullets.add(bullet);
    }

    /**
     * The main game loop.
     */
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

            // Buildings shoot
            buildHandler.shoot();
            buildHandler.buildFromQueue();

            // Update game objects.
            bullets.forEach(Bullet::update);
            // Log.info("bullets in game state: " + bullets.size());
            Bullet bullet;
            while ((bullet = pendingBullets.poll()) != null) {
                bullets.add(bullet);
            }
            bulletCollisionHandler.handleBulletCollisions(bullets, players, buildHandler.getBuildings());

            // Check if any players are dead and need to be given the respawn timer.
            matchRules.checkRespawn(players);

            // Update player positions.
            players.forEach(Player::updateFromPhysics);
            var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());

            // Capture Zone updates according to the players (not) inside it.
            captureZones.forEach(zone -> zone.update(players, deltaTime));
            ArrayList<CaptureZoneState> zoneStates = captureZones.stream()
                    .map(CaptureZone::toState)
                    .collect(Collectors.toCollection(ArrayList::new));
            gameStateHandler.setCaptureZoneStates(zoneStates);

            // Send the game state to all clients.
            connections.forEach(connection -> connection.sendUDP(gameStateMessage));

            // If there is a match end reason, stop the game.
            MatchRules.MatchEndReason endReason = matchRules.determineEndReason(
                    captureZones, connections, gameStateHandler.getGameTime()
            );
            if (endReason != MatchRules.MatchEndReason.NONE) {
                int winnerPlayerId = matchRules.getGameWinnerId();
                gameStateHandler.setWinnerPlayerId(winnerPlayerId);
                gameStateHandler.setGameOver(true);
                var finalGameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());
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
            // players.forEach(player -> System.out.println(
            //         player.getUsername() + ": " + player.getX() + " ; " + player.getY()
            // ));

            // Sleep for 1 second to maintain a constant tick rate.
            try {
                Thread.sleep(Duration.ofMillis(1000 / GAME_TICK_RATE));
            } catch (InterruptedException e) {
                Log.error("Game loop sleep interrupted", e);
            }
        }
    }
}
