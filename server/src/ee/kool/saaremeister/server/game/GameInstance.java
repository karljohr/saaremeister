package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.*;
import ee.kool.saaremeister.server.listener.ServerListener;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import com.badlogic.gdx.physics.box2d.World;
import lombok.Getter;
import message.dto.StaticBodyState;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

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

    private final GameConnectionManager connectionManager;
    private final GameTickProcessor tickProcessor;

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

        this.buildHandler = new BuildHandler(world, this);

        CaptureZoneLoader captureZoneLoader = new CaptureZoneLoader();
        captureZones.addAll(captureZoneLoader.loadCaptureZones());

        this.connectionManager = new GameConnectionManager(
                lobbyManager,
                gameStateHandler,
                buildHandler,
                world,
                this,
                players,
                bullets,
                captureZones,
                activePlayers,
                connections,
                staticBodyData
        );

        GameDisposer gameDisposer = new GameDisposer(players, connections, lobbyManager, lobbyId);

        this.tickProcessor = new GameTickProcessor(
                world,
                gameStateHandler,
                buildHandler,
                bulletCollisionHandler,
                matchRules,
                players,
                pendingBullets,
                bullets,
                captureZones,
                connections,
                gameDisposer,
                lobbyManager,
                lobbyId
        );
    }

    /**
     * Adds a connection to the game instance.
     */
    public void addLobbyConnections(Collection<Connection> connections) {
        connectionManager.addLobbyConnections(connections);
    }

    /**
     * Returns true if the game instance has enough players to start.
     */
    public boolean hasEnoughPlayers() {
        return connectionManager.hasEnoughPlayers();
    }

    /**
     * Removes a connection from the game instance.
     */
    public void removeConnection(Connection connection) {
        connectionManager.removeConnection(connection);
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
            isGameRunning = tickProcessor.tick();

            // Sleep for 1 second to maintain a constant tick rate.
            try {
                Thread.sleep(Duration.ofMillis(1000 / GAME_TICK_RATE));
            } catch (InterruptedException e) {
                Log.error("Game loop sleep interrupted", e);
            }
        }
    }
}
