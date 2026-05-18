package ee.kool.saaremeister.server.game;

import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.lobby.Lobby;
import ee.kool.saaremeister.server.lobby.LobbyManager;

/**
 * Handles the disposal of the game.
 */
public class GameDisposer {

    private final GameConnectionManager connectionManager;
    private final LobbyManager lobbyManager;
    private final int lobbyId;

    /**
     * Creates a disposer responsible for cleaning up one finished game.
     */
    public GameDisposer(GameConnectionManager connectionManager, LobbyManager lobbyManager, int lobbyId) {
        this.connectionManager = connectionManager;
        this.lobbyManager = lobbyManager;
        this.lobbyId = lobbyId;
    }

    /**
     * Disposes the game by removing all players and connections.
     */
    public void dispose() {
        connectionManager.dispose();

        Lobby lobby = lobbyManager.getLobby(lobbyId);
        if (lobby != null) {
            lobby.disposeGame();
            Log.info("Game disposed in lobby " + lobbyId);
        } else {
            Log.warn("Lobby not found for game " + lobbyId);
        }
    }
}
