package ee.kool.saaremeister.server.lobby;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.chat.Chat;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.listener.ServerListener;
import lombok.Data;
import lombok.Getter;
import message.LobbyUpdateMessage;
import message.RoundEndMessage;
import message.StartGameMessage;
import message.dto.PlayerRole;

import java.util.*;

import static constant.Constants.LOBBY_MAX_PLAYERS;

/**
 * Represents a lobby for a game on the server side.
 */
@Data
public class Lobby {

    @Getter
    private final int lobbyId;
    private final Set<Connection> players = new LinkedHashSet<>();
    private GameInstance gameInstance;
    private boolean started = false;
    private List<String> usernames = new ArrayList<>();
    private final Chat chat;

    // Series state
    private List<Connection> realPlayersInOrder = new ArrayList<>();
    private int currentRound = 0;
    private int totalRounds = 0;
    private boolean seriesActive = false;
    private Map<Integer, Integer> winsByPlayerId = new HashMap<>();

    public Lobby(int lobbyId) {
        this.lobbyId = lobbyId;
        this.chat = new Chat(lobbyId, players);
    }

    /**
     * Adds a player to the lobby if there is less than the maximum number of players.
     */
    public boolean addPlayer(Connection connection, String username) {
        if (players.size() < LOBBY_MAX_PLAYERS) {
            players.add(connection);
            usernames.add(username);
            broadcastLobbyUpdate();
            chat.sendChatHistory(connection);
            Log.info("Player joined lobby " + lobbyId + ", player count: " + players.size());
            return true;
        } else {
            LobbyUpdateMessage updateMessage = new LobbyUpdateMessage();
            updateMessage.setLobbyId(lobbyId);
            updateMessage.setPlayerCount(-1);
            updateMessage.setUsernames(usernames);
            updateMessage.setCurrentRound(currentRound);
            updateMessage.setTotalRounds(totalRounds);
            updateMessage.setSeriesActive(seriesActive);
            connection.sendTCP(updateMessage);
            Log.info("Lobby " + lobbyId + "is full, cannot add player");
            return false;
        }
    }

    /**
     * Removes a player from the lobby, provided they currently are in the lobby.
     */
    public boolean removePlayer(Connection connection, String username) {
        if (players.remove(connection)) {
            usernames.remove(username);
            broadcastLobbyUpdate();
            Log.info("Player left lobby " + lobbyId + ", player count: " + players.size());
            return true;
        }
        return false;
    }

    public boolean hasPlayer(Connection connection) {
        return players.contains(connection);
    }

    /**
     * Starts a game instance for the lobby.
     */
    public void startGame(Connection startingPlayer, ServerListener serverListener,
                          LobbyManager lobbyManager) {
        // Warnings if the lobby cannot start a game
        if (players.isEmpty()) {
            Log.warn("Cannot start game - no players in lobby: " + lobbyId);
            return;
        }
        if (!players.contains(startingPlayer)) {
            Log.warn("Player not in lobby attempted to start game: " + lobbyId);
            return;
        }

        if (!seriesActive) {
            startSeries();
        }
        if (!hasNextRound()) {
            Log.warn("Cannot start game - no more rounds in lobby: " + lobbyId);
            return;
        }
        Connection masterConnection = currentMaster();
        if (masterConnection == null || !players.contains(masterConnection)) {
            Log.warn("Cannot start game - no Island Master in lobby: " + lobbyId);
            return;
        }
        gameInstance = new GameInstance(lobbyManager, lobbyId, masterConnection);
        gameInstance.addLobbyConnections(players);
        gameInstance.start();
        StartGameMessage startMessage = new StartGameMessage();
        startMessage.setLobbyId(lobbyId);
        startMessage.setCurrentRound(currentRound);
        startMessage.setTotalRounds(totalRounds);
        players.forEach(player -> player.sendTCP(startMessage));
        players.forEach(chat::sendChatHistory);
        started = true;
        Log.info("Game started for lobby: " + lobbyId + " with " + players.size() + " players");
    }

    /**
     * Sends a lobby update message to all players in the lobby.
     */
    public void broadcastLobbyUpdate() {
        LobbyUpdateMessage updateMessage = new LobbyUpdateMessage();
        updateMessage.setLobbyId(lobbyId);
        updateMessage.setPlayerCount(players.size());
        updateMessage.setStarted(started);
        updateMessage.setUsernames(usernames);
        updateMessage.setCurrentRound(currentRound);
        updateMessage.setTotalRounds(totalRounds);
        updateMessage.setSeriesActive(seriesActive);
        updateMessage.setWinsByPlayerId(new HashMap<>(winsByPlayerId));
        players.forEach(connection -> connection.sendTCP(updateMessage));
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Disposes the game instance for the lobby.
     */
    public void disposeGame() {
        gameInstance = null;
        started = false;
        broadcastLobbyUpdate();
        Log.info("Disposed game instance for lobby " + lobbyId);
    }

    /**
     * Initializes the game series (as many rounds as there are players) for a lobby.
     */
    public void startSeries() {
        if (seriesActive) return;
        realPlayersInOrder = new ArrayList<>(players);
        Collections.shuffle(realPlayersInOrder);
        totalRounds = realPlayersInOrder.size();
        currentRound = 0;
        seriesActive = true;
        winsByPlayerId = new HashMap<>();
        for (Connection connection : realPlayersInOrder) {
            winsByPlayerId.put(connection.getID(), 0);
        }
    }

    /**
     * Returns the current master player in the series.
     */
    public Connection currentMaster() {
        if (!seriesActive) return null;
        if (currentRound < 0 || currentRound >= totalRounds) return null;
        return realPlayersInOrder.get(currentRound);
    }

    public boolean hasNextRound() {
        return seriesActive && currentRound < totalRounds;
    }

    public boolean isFinalRound() {
        return seriesActive && currentRound == totalRounds - 1;
    }

    /**
     * Records the winner of a round for the leaderboard.
     */
    public void recordRoundWinner(PlayerRole winnerRole, List<Player> roundPlayers) {
        if (!seriesActive) return;

        // Additions to the leaderboard.
        if (winnerRole == PlayerRole.MASTER) {
            for (Player player : roundPlayers) {
                if (player.getRole() == PlayerRole.MASTER) {
                    winsByPlayerId.merge(player.getId(), 1, Integer::sum);
                    break;
                }
            }
        } else if (winnerRole == PlayerRole.OFFICER) {
            for (Player player : roundPlayers) {
                if (player.getRole() == PlayerRole.OFFICER && player.getConnection() != null) {
                    winsByPlayerId.merge(player.getId(), 1, Integer::sum);
                }
            }
        }

        currentRound++;
        if (currentRound >= totalRounds) {
            seriesActive = false;
        }
    }

    /**
     * Returns the round end message for the current round when it ends.
     */
    public RoundEndMessage finishRound(PlayerRole winnerRole, List<Player> roundPlayers) {
        int finishedRound = currentRound + 1;
        boolean finalRound = isFinalRound();
        recordRoundWinner(winnerRole, roundPlayers);

        RoundEndMessage roundEndMessage = new RoundEndMessage();
        roundEndMessage.setLobbyId(lobbyId);
        roundEndMessage.setCurrentRound(finishedRound);
        roundEndMessage.setTotalRounds(totalRounds);
        roundEndMessage.setFinalRound(finalRound);
        roundEndMessage.setWinnerRole(winnerRole);
        roundEndMessage.setWinsByPlayerId(new HashMap<>(winsByPlayerId));
        return roundEndMessage;
    }
}