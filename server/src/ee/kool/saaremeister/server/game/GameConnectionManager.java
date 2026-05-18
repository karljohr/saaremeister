package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.*;
import ee.kool.saaremeister.server.game.object.player.Bot;
import ee.kool.saaremeister.server.game.object.player.Master;
import ee.kool.saaremeister.server.game.object.player.Officer;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.game.object.projectiles.Bullet;
import ee.kool.saaremeister.server.listener.ItemUsedListener;
import ee.kool.saaremeister.server.listener.ShopPurchaseListener;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import message.dto.PlayerRole;
import message.dto.StaticBodyState;

import java.util.*;

import static constant.Constants.PLAYER_COUNT_INGAME_LIMIT;

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
    private final Map<Integer, Listener> itemUsedListeners = new HashMap<>();
    private final Map<Integer, Listener> shopPurchaseListeners = new HashMap<>();

    /**
     * Creates a new connection manager for one running game instance.
     */
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
    public void addLobbyConnections(Collection<Connection> connections, Connection masterConnection) {
        List<Connection> connectionList = new ArrayList<>(connections);
        if (connectionList.isEmpty()) return;
        if (masterConnection == null) {
            throw new IllegalArgumentException("Master connection cannot be null");
        }

        for (Connection connection : connectionList) {
            PlayerRole role = connection.getID() == masterConnection.getID()
                    ? PlayerRole.MASTER
                    : PlayerRole.OFFICER;
            addConnection(connection, role);
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

        // Remove any stale per-match listeners before wiring this connection into a new round.
        removeGameplayListeners(connection);

        Listener itemUsedListener = new ItemUsedListener(activePlayers);
        Listener shopPurchaseListener = new ShopPurchaseListener(activePlayers);
        itemUsedListeners.put(connection.getID(), itemUsedListener);
        shopPurchaseListeners.put(connection.getID(), shopPurchaseListener);

        connection.addListener(itemUsedListener);
        connection.addListener(shopPurchaseListener);

        // Debug hitbox info
        StaticBodyState[] arr = staticBodyData.toArray(new StaticBodyState[0]);
        connection.sendTCP(arr);

        if (hasEnoughPlayers()) {
            gameStateHandler.setAllPlayersHaveJoined(true);
        }
    }

    /**
     * Spawns as many bots as necessary to fill the player limit.
     */
    public void spawnBots() {
        int currentPlayers = players.size();
        int botsToAdd = PLAYER_COUNT_INGAME_LIMIT - currentPlayers;

        for (int i = 0; i < botsToAdd; i++) {
            int botId = 1000 + i;
            Bot bot = new Bot(botId, gameInstance, world, buildHandler);

            SpawnPointLoader.setRandomSpawnPoint(bot, PlayerRole.OFFICER, spawnPoints);

            activePlayers.put(botId, bot);
            players.add(bot);
            Log.info("Bot added to game: " + bot.getUsername());
        }
        gameStateHandler.setAllPlayersHaveJoined(true);
    }

    /**
     * Removes a player from the game instance.
     */
    public void removeConnection(com.esotericsoftware.kryonet.Connection connection) {
        connections.remove(connection);
        Player p = activePlayers.remove(connection.getID());

        if (p != null) {
            p.dispose();
            players.remove(p);
            captureZones.forEach(zone -> zone.clearPlayer(p.getRole()));
        }

        removeGameplayListeners(connection);

        var gameStateMessage = gameStateHandler.getGameStateMessage(
                players,
                bullets,
                buildHandler.getBuildings(),
                new ArrayList<>()
        );
        connections.forEach(c -> c.sendTCP(gameStateMessage));

        Log.info("Player left the game: " + connection.getID());
    }

    /**
     * Disposes all per-match listeners and clears tracked connection state.
     */
    public void dispose() {
        players.forEach(Player::dispose);
        new ArrayList<>(connections).forEach(this::removeGameplayListeners);
        activePlayers.clear();
        connections.clear();
    }

    /**
     * Removes the gameplay listeners registered for one connection.
     */
    private void removeGameplayListeners(Connection connection) {
        Listener itemUsedListener = itemUsedListeners.remove(connection.getID());
        if (itemUsedListener != null) {
            connection.removeListener(itemUsedListener);
        }

        Listener shopPurchaseListener = shopPurchaseListeners.remove(connection.getID());
        if (shopPurchaseListener != null) {
            connection.removeListener(shopPurchaseListener);
        }
    }
}
