package ee.kool.saaremeister.server.lobby;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.listener.ServerListener;
import lombok.Data;
import lombok.Getter;
import message.LobbyUpdateMessage;
import message.StartGameMessage;

import java.util.HashSet;
import java.util.Set;

import static constant.Constants.LOBBY_MAX_PLAYERS;

@Data
public class Lobby {

    @Getter
    private final int lobbyId;
    private final Set<Connection> players = new HashSet<>();
    private GameInstance gameInstance;

    public Lobby(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Adds a player to the lobby if there is less than the maximum number of players.
     */
    public boolean addPlayer(com.esotericsoftware.kryonet.Connection connection) {
        if (players.size() < LOBBY_MAX_PLAYERS) {
            players.add(connection);
            broadcastLobbyUpdate();
            Log.info("Player joined lobby " + lobbyId + ", player count: " + players.size());
            return true;
        } else {
            LobbyUpdateMessage updateMessage = new LobbyUpdateMessage();
            updateMessage.setLobbyId(lobbyId);
            updateMessage.setPlayerCount(-1);
            connection.sendTCP(updateMessage);
            Log.info("Lobby " + lobbyId + "is full, cannot add player");
            return false;
        }
    }

    /**
     * Removes a player from the lobby, provided they currently are in the lobby.
     */
    public boolean removePlayer(com.esotericsoftware.kryonet.Connection connection) {
        if (players.remove(connection)) {
            broadcastLobbyUpdate();
            Log.info("Player left lobby " + lobbyId + ", player count: " + players.size());
            return true;
        }
        return false;
    }

    public boolean hasPlayer(com.esotericsoftware.kryonet.Connection connection) {
        return players.contains(connection);
    }

    public void startGame(com.esotericsoftware.kryonet.Connection startingPlayer, ServerListener serverListener, LobbyManager lobbyManager) {
        if (players.isEmpty()) {
            Log.warn("Cannot start game - no players in lobby: " + lobbyId);
            return;
        }

        if (!players.contains(startingPlayer)) {
            Log.warn("Player not in lobby attempted to start game: " + lobbyId);
            return;
        }

        gameInstance = new GameInstance(serverListener, lobbyManager, lobbyId);
        players.forEach(player -> gameInstance.addConnection(player));
        gameInstance.start();
        StartGameMessage startMessage = new StartGameMessage();
        startMessage.setLobbyId(lobbyId);
        players.forEach(player -> player.sendTCP(startMessage));
        Log.info("Game started for lobby: " + lobbyId + " with " + players.size() + " players");
    }

    /**
     * Sends a lobby update message to all players in the lobby.
     */
    private void broadcastLobbyUpdate() {
        LobbyUpdateMessage updateMessage = new LobbyUpdateMessage();
        updateMessage.setLobbyId(lobbyId);
        updateMessage.setPlayerCount(players.size());
        players.forEach(conn -> conn.sendTCP(updateMessage));
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public void disposeGame() {
        gameInstance = null;
        Log.info("Disposed game instance for lobby " + lobbyId);
    }
}