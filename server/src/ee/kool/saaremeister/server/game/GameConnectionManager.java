package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.*;
import ee.kool.saaremeister.server.listener.ShopPurchaseListener;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import message.dto.PlayerRole;
import message.dto.StaticBodyState;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static constant.Constants.PLAYER_COUNT_INGAME_LIMIT;
import static constant.Constants.PPM;

/**
 * Manages the connections to the game and assigns roles to players.
 */
public class GameConnectionManager {
    private final LobbyManager lobbyManager;
    private final GameStateHandler gameStateHandler;
    private final BuildHandler buildHandler;
    private final World world;
    private final GameInstance gameInstance;

    private final List<Player> players;
    private final List<Bullet> bullets;
    private final List<CaptureZone> captureZones;
    private final Map<Integer, Player> activePlayers;
    private final Set<Connection> connections;
    private final List<StaticBodyState> staticBodyData;
    private final Map<PlayerRole, List<Vector2>> spawnPoints;

    public GameConnectionManager(
            LobbyManager lobbyManager,
            GameStateHandler gameStateHandler,
            BuildHandler buildHandler,
            World world,
            GameInstance gameInstance,
            List<Player> players,
            List<Bullet> bullets,
            List<CaptureZone> captureZones,
            Map<Integer, Player> activePlayers,
            Set<Connection> connections,
            List<StaticBodyState> staticBodyData,
            Map<PlayerRole, List<Vector2>> spawnPoints
    ) {
        this.lobbyManager = lobbyManager;
        this.gameStateHandler = gameStateHandler;
        this.buildHandler = buildHandler;
        this.world = world;
        this.gameInstance = gameInstance;
        this.players = players;
        this.bullets = bullets;
        this.captureZones = captureZones;
        this.activePlayers = activePlayers;
        this.connections = connections;
        this.staticBodyData = staticBodyData;
        this.spawnPoints = spawnPoints;
    }

    /**
     * Adds a connection to the game and assigns it a player role.
     */
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
            case MASTER -> new Master(connection, gameInstance, world, buildHandler);
            case OFFICER -> new Officer(connection, gameInstance, world, buildHandler);
        };
        newPlayer.setUsername(lobbyManager.getUsername(connection));
        activePlayers.put(connection.getID(), newPlayer);
        players.add(newPlayer);
        connections.add(connection);

       SpawnPointLoader.setRandomSpawnPoint(newPlayer, role, spawnPoints);

        connection.addListener(new ShopPurchaseListener(activePlayers));

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
        connections.remove(connection);
        Player p = activePlayers.remove(connection.getID());

        if (p != null) {
            players.remove(p);
            captureZones.forEach(zone -> zone.clearPlayer(p.getId()));
        }

        var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets, buildHandler.getBuildings());
        connections.forEach(c -> c.sendTCP(gameStateMessage));

        Log.info("Player left the game: " + connection.getID());
    }

}
