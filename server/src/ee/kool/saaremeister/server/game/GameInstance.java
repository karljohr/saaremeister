package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.*;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.game.object.projectiles.Bullet;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import com.badlogic.gdx.physics.box2d.World;
import lombok.Getter;
import message.dto.ExplosionState;
import message.dto.PlayerRole;
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

    @Getter
    private final List<Player> players = new ArrayList<>();
    private final Queue<Bullet> pendingBullets = new ConcurrentLinkedDeque<>();

    @Getter
    private final Map<PlayerRole, List<Vector2>> spawnPoints;
    private final Queue<ExplosionState> pendingExplosions = new ConcurrentLinkedDeque<>();

    private final GameConnectionManager connectionManager;
    private final GameTickProcessor tickProcessor;

    private final Connection masterConnection;

    /**
     * Creates a new game instance.
     */
    public GameInstance(LobbyManager lobbyManager, int lobbyId, Connection masterConnection) {
        this.masterConnection = masterConnection;

        World world = new World(new Vector2(0, 0), true);
        BulletCollisionHandler bulletCollisionHandler = new BulletCollisionHandler(world);
        bulletCollisionHandler.setGameInstance(this);

        Box2dWorldGenerator box2dWorldGenerator = new Box2dWorldGenerator();
        List<StaticBodyState> staticBodyData = box2dWorldGenerator.initializeWorld(world);

        BuildHandler buildHandler = new BuildHandler(world, this);
        EconomyManager economyManager = new EconomyManager();

        CaptureZoneLoader captureZoneLoader = new CaptureZoneLoader();
        List<CaptureZone> captureZones = new ArrayList<>(captureZoneLoader.loadCaptureZones());

        GameStateHandler gameStateHandler = new GameStateHandler();
        Set<Connection> connections = Collections.synchronizedSet(new HashSet<>());
        List<Bullet> bullets = new ArrayList<>();
        Map<Integer, Player> activePlayers = new ConcurrentHashMap<>();
        SpawnPointLoader spawnPointLoader = new SpawnPointLoader();
        spawnPoints = spawnPointLoader.loadSpawnPoints();

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
                staticBodyData,
                spawnPoints
        );

        GameDisposer gameDisposer = new GameDisposer(players, connections, lobbyManager, lobbyId);

        MatchRules matchRules = new MatchRules();
        this.tickProcessor = new GameTickProcessor(
                world,
                gameStateHandler,
                buildHandler,
                bulletCollisionHandler,
                matchRules,
                players,
                pendingBullets,
                pendingExplosions,
                bullets,
                captureZones,
                connections,
                gameDisposer,
                lobbyManager,
                economyManager,
                lobbyId
        );
    }

    /**
     * Adds a connection to the game instance.
     */
    public void addLobbyConnections(Collection<Connection> connections) {
        connectionManager.addLobbyConnections(connections, masterConnection);
        connectionManager.spawnBots();
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

    public void addExplosion(ExplosionState explosionState) {
        pendingExplosions.add(explosionState);
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
