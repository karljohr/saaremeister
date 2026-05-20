package ee.kool.saaremeister.game;

import com.esotericsoftware.kryonet.Client;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.network.listener.GameStateMessageListener;
import lombok.Getter;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.PlayerRole;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the game state on the client side.
 */
@Setter
@Getter
public class GameStateManager {

    private Client client;
    private final GameStateMessageListener gameStateMessageListener;
    private volatile GameStateMessage latestGameStateMessage;
    private PlayerRole myRole;
    private final Map<Integer, String> usernamesByPlayerId = new HashMap<>();

    public GameStateManager() {
        this.client = ServerConnection.getInstance().getClient();
        this.gameStateMessageListener = new GameStateMessageListener(this);
        this.client.addListener(gameStateMessageListener);

        latestGameStateMessage = new GameStateMessage();
        latestGameStateMessage.setPlayerStates(new ArrayList<>());
        latestGameStateMessage.setBulletStates(new ArrayList<>());
        latestGameStateMessage.setBuildingStates(new ArrayList<>());
        latestGameStateMessage.setGameTime(0);
        latestGameStateMessage.setAllPlayersHaveJoined(false);
        latestGameStateMessage.setCaptureZones(new ArrayList<>());
        latestGameStateMessage.setGameOver(false);
        latestGameStateMessage.setExplosionStates(new ArrayList<>());
        latestGameStateMessage.setWinnerPlayerRole(null);
    }

    /**
     * Resolves the player's role.
     */
    public PlayerRole resolveMyRole(GameStateMessage msg) {
        if (myRole != null || msg == null) return myRole;
        myRole = msg.getPlayerStates().stream()
                .filter(p -> p.getId() == client.getID())
                .map(PlayerState::getRole)
                .findFirst()
                .orElse(null);
        return myRole;
    }

    /**
     * Maps wins by playerId to username.
     */
    public Map<String, Integer> getWinsByUsername(Map<Integer, Integer> winsByPlayerId) {
        Map<String, Integer> winsByUsername = new HashMap<>();
        winsByPlayerId.forEach((playerId, wins) -> {
            String username = usernamesByPlayerId.get(playerId);
            winsByUsername.put(username, wins);
        });
        return winsByUsername;
    }

    /**
     * Caches usernames for the given GameStateMessage.
     */
    public void cacheUsernames(GameStateMessage msg) {
        if (msg == null || msg.getPlayerStates() == null) return;

        for (PlayerState playerState : msg.getPlayerStates()) {
            usernamesByPlayerId.put(playerState.getId(), playerState.getUsername());
        }
    }

    /**
     * Disposes the game state manager.
     */
    public void dispose() {
        client.removeListener(gameStateMessageListener);
    }
}
