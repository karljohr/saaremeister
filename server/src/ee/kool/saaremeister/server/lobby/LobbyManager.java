package ee.kool.saaremeister.server.lobby;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.chat.Chat;
import ee.kool.saaremeister.server.chat.ChatManager;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.listener.ServerListener;
import lombok.Data;
import message.GameJoinMessage;
import message.LobbyExitMessage;
import message.StartGameMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class LobbyManager {

    private final Map<Integer, Lobby> lobbies = new HashMap<>();
    private final ServerListener serverListener;
    private final Map<Integer, String> usernames = new ConcurrentHashMap<>();

    public LobbyManager(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    public void handleJoinRequest(Connection connection, GameJoinMessage gameJoinMessage) {
        int lobbyId = gameJoinMessage.getLobbyId();
        if (lobbyId <= 0) {
            Log.warn("Invalid lobby id: " + lobbyId);
            return;
        }
        storeUsername(connection, gameJoinMessage.getUsername());

        removePlayerFromAllLobbies(connection);
        Lobby lobby = lobbies.computeIfAbsent(lobbyId, Lobby::new);     // Stores lobbies as <lobbyID, Lobby> map.

        ChatManager chatManager = (ChatManager) serverListener.getChatManager();
        if (!chatManager.getChats().containsKey(lobbyId)) {
            // Loo uus Chat. Eeldame, et Lobby klassis on meetod getConnections()
            Chat newChat = new Chat(lobbyId, lobby.getConnections());
            chatManager.getChats().put(lobbyId, newChat);
            Log.info("Chat initialized for lobby " + lobbyId);
        }

        boolean success = lobby.addPlayer(connection, gameJoinMessage.getUsername());
        Log.info("Player joined with username: '" + gameJoinMessage.getUsername() + "'");

        if (!success) {
            Log.info("Failed to add player to lobby: " + lobbyId);
        }
    }

    public void handleStartGameRequest(Connection connection, StartGameMessage startMessage) {
        int lobbyId = startMessage.getLobbyId();
        Lobby lobby = lobbies.get(lobbyId);

        if (lobby != null && lobby.hasPlayer(connection)) {
            if (lobby.getGameInstance() == null) {
                lobby.startGame(connection, serverListener, this);
            } else {
                Log.warn("Game already active in lobby: " + lobbyId + ", cannot start game");
            }

        } else {
            Log.warn("Player attempted to start game in lobby they're not in: " + lobbyId);
        }
    }

    public void handleExitRequest(Connection connection, LobbyExitMessage exitMessage) {
        int lobbyId = exitMessage.getLobbyId();
        Lobby lobby = lobbies.get(lobbyId);

        if (lobby != null) {
            if (lobby.getGameInstance() != null) {
                lobby.getGameInstance().removeConnection(connection);
            }

            lobby.removePlayer(connection, usernames.get(connection.getID()));
            LobbyExitMessage confirmationMessage = new LobbyExitMessage();
            confirmationMessage.setLobbyId(lobbyId);
            connection.sendTCP(confirmationMessage);

            if (lobby.isEmpty()) {
                lobbies.remove(lobbyId);
                Log.info("Lobby " + lobbyId + " removed (empty)");
            }
        }
    }

    public void removePlayerFromAllLobbies(Connection connection) {
        // Kasutame eemaldamiseks turvalisemat viisi
        lobbies.values().removeIf(lobby -> {
            boolean removed = lobby.removePlayer(connection, usernames.get(connection.getID()));
            if (removed) {
                Log.info("Removed player from lobby: " + lobby.getLobbyId());
                if (lobby.getGameInstance() != null) {
                    lobby.getGameInstance().removeConnection(connection);
                }
            }
            return lobby.isEmpty(); // Kui lobby on tühi, eemaldatakse see map-ist
        });
    }

    public void storeUsername(Connection connection, String username) {
        usernames.put(connection.getID(), username);
    }

    public String getUsername(Connection connection) {
        return usernames.get(connection.getID());
    }

    public Lobby getLobby(int lobbyId) {
        return lobbies.get(lobbyId);
    }
}