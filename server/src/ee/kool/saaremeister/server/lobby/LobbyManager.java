package ee.kool.saaremeister.server.lobby;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
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
        storeUsername(connection, gameJoinMessage.getUsername());
        removePlayerFromAllLobbies(connection);

        int targetLobbyId = -1;
        Lobby bestLobby = null;
        int maxPlayers = -1;

        // Find the fullest lobby to join
        for (Lobby lobby : lobbies.values()) {
            if (!lobby.isStarted() && lobby.getPlayers().size() < constant.Constants.LOBBY_MAX_PLAYERS) {
                int currentPlayers = lobby.getPlayers().size();

                if (currentPlayers > maxPlayers) {
                    maxPlayers = currentPlayers;
                    bestLobby = lobby;
                }

                else if (currentPlayers == maxPlayers && bestLobby != null) {
                    if (lobby.getLobbyId() < bestLobby.getLobbyId()) {
                        bestLobby = lobby;
                    }
                }
            }
        }

        if (bestLobby != null) {
            targetLobbyId = bestLobby.getLobbyId();
        } else {
            // If no lobby, then create one
            targetLobbyId = 1;
            while (lobbies.containsKey(targetLobbyId)) {
                targetLobbyId++;
            }
        }

        Lobby lobby = lobbies.computeIfAbsent(targetLobbyId, Lobby::new);

        boolean success = lobby.addPlayer(connection, gameJoinMessage.getUsername());
        Log.info("Player joined with username: '" + gameJoinMessage.getUsername() + "'");

        if (success) {
            GameJoinMessage response = new GameJoinMessage();
            response.setLobbyId(targetLobbyId);
            response.setUsername(gameJoinMessage.getUsername());
            connection.sendTCP(response);

            lobby.broadcastLobbyUpdate();

            Log.info("Player '" + gameJoinMessage.getUsername() + "' joined lobby: " + targetLobbyId);
        } else {
            Log.info("Failed to add player to lobby: " + targetLobbyId);
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