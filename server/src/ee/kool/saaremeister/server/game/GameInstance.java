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

    private final LobbyManager lobbyManager;
    private final int lobbyId;
    private final GameStateHandler gameStateHandler = new GameStateHandler();
    private final Set<Connection> connections = Collections.synchronizedSet(new HashSet<>());
    private final List<Player> players = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private final List<CaptureZone> captureZones = new ArrayList<>();
    private final Map<Integer, Player> activePlayers = new ConcurrentHashMap<>();
    private final World world;
    List<StaticBodyState> staticBodyData;

    public GameInstance(ServerListener serverListener, LobbyManager lobbyManager, int lobbyId) {
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
        this.world = new World(new Vector2(0, 0), true);

        Box2dWorldGenerator box2dWorldGenerator = new Box2dWorldGenerator();
        staticBodyData = box2dWorldGenerator.initializeWorld(world);

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

        Player newPlayer = new Player(connection, this, world);
        newPlayer.setUsername(lobbyManager.getUsername(connection));
        players.add(newPlayer);
        activePlayers.put(connection.getID(), newPlayer);
        connections.add(connection);

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

        var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets);
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

            // Capture Zone updates according to the players (not) inside it.
            captureZones.forEach(zone -> zone.update(players, deltaTime));
            ArrayList<CaptureZoneState> zoneStates = captureZones.stream()
                    .map(CaptureZone::toState)
                    .collect(Collectors.toCollection(ArrayList::new));
            gameStateHandler.setCaptureZoneStates(zoneStates);

            // Send the game state to all clients.
            var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets);
            connections.forEach(connection -> connection.sendUDP(gameStateMessage));

            // If one player has captured all the capture zones, the game is over.
            int test = 0;
            for (int i = 0; i < captureZones.size() - 1; i++) {
                if (captureZones.get(i).getOwnerPlayerId() == captureZones.get(i + 1).getOwnerPlayerId()
                && captureZones.get(i).getOwnerPlayerId() != -1) {
                    test++;
                }
            }
            if (test == captureZones.size() - 1) {
                connections.forEach(connection -> connection.sendTCP(gameStateMessage));
                Log.info("Game over, stopping game.");
                disposeGame();
                isGameRunning = false;
            }

            // If game time is up, the game is over.
            if (gameStateHandler.getGameTime() >= MATCH_DURATION_SECONDS) {
                connections.forEach(connection -> connection.sendTCP(gameStateMessage));
                Log.info("Game time over, stopping game.");
                disposeGame();
                isGameRunning = false;
            }

            // If no players are connected, the game is over and get rid of the game instance.
            if (connections.isEmpty()) {
                Log.info("No players connected, stopping game.");
                disposeGame();
                isGameRunning = false;
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
