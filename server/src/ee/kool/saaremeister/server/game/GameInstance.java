package ee.kool.saaremeister.server.game;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.listener.ServerListener;
import ee.kool.saaremeister.server.lobby.LobbyManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static constant.Constants.GAME_TICK_RATE;
import static constant.Constants.PLAYER_COUNT_INGAME_LIMIT;

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
    private final Map<Integer, Player> activePlayers = new ConcurrentHashMap<>();

<<<<<<< server/src/ee/kool/saaremeister/server/game/GameInstance.java
    public GameInstance(ServerListener serverListener, Connection connection) {
        this.serverListener = serverListener;
        addConnection(connection);
=======
    public GameInstance(ServerListener serverListener, LobbyManager lobbyManager, int lobbyId, Connection connection) {
        this.serverListener = serverListener;
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
        addConnection(connection);
>>>>>>> server/src/ee/kool/saaremeister/server/game/GameInstance.java
    }

    public boolean hasEnoughPlayers() {
        return players.size() >= PLAYER_COUNT_INGAME_LIMIT;
    }

    public void addConnection(com.esotericsoftware.kryonet.Connection connection) {
        if (hasEnoughPlayers()) {
            Log.info("Cannot add player, game is full.");
            return;
        }

        Player newPlayer = new Player(connection, this);
        players.add(newPlayer);
        activePlayers.put(connection.getID(), newPlayer);
        connections.add(connection);

        if (hasEnoughPlayers()) {
            gameStateHandler.setAllPlayersHaveJoined(true);
        }
    }

    public void removeConnection(com.esotericsoftware.kryonet.Connection connection) {
        this.connections.remove(connection);
        Player p = activePlayers.remove(connection.getID());
        if (p != null) players.remove(p);

        var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets);
        connections.forEach(c -> c.sendTCP(gameStateMessage));

        Log.info("Player left the game: " + connection.getID());
    }

    private void disposeGame() {
        players.forEach(Player::dispose);
        connections.clear();
        serverListener.disposeGame();
    }

    @Override
    public void run() {
        boolean isGameRunning = true;
        System.out.println("Game is running: " + isGameRunning);
        while (isGameRunning) {
            gameStateHandler.incrementGameTimeIfPlayersPresent();
            bullets.forEach(Bullet::update);
            var gameStateMessage = gameStateHandler.getGameStateMessage(players, bullets);
            connections.forEach(connection -> connection.sendUDP(gameStateMessage));

            if (players.stream().anyMatch(x -> x.getLives() == 0)) {
                connections.forEach(connection -> connection.sendTCP(gameStateMessage));
                disposeGame();
                isGameRunning = false;
            }
            if (connections.isEmpty()) {
                Log.info("No players connected, stopping game.");
                disposeGame();
                isGameRunning = false;
            }

            try {
                Thread.sleep(Duration.ofMillis(1000 / GAME_TICK_RATE));
            } catch (InterruptedException e) {
                Log.error("Game loop sleep interrupted", e);
            }
        }
    }
}
