package ee.kool.saaremeister.server.game;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.lobby.Lobby;
import ee.kool.saaremeister.server.lobby.LobbyManager;

import java.util.List;
import java.util.Set;

/**
 * Handles the disposal of the game.
 */
public class GameDisposer {

    private final List<Player> players;
    private final Set<Connection> connections;
    private final LobbyManager lobbyManager;
    private final int lobbyId;

    public GameDisposer(List<Player> players, Set<Connection> connections, LobbyManager lobbyManager, int lobbyId) {
        this.players = players;
        this.connections = connections;
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
    }

    /**
     * Disposes the game by removing all players and connections.
     */
    public void dispose() {
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
}
