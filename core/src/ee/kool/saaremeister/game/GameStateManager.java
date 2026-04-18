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

/**
 * Manages the game state on the client side.
 */
@Setter
@Getter
public class GameStateManager {

    private Client client;
    private final GameStateMessageListener gameStateMessageListener;
    private GameStateMessage latestGameStateMessage;
    private PlayerRole myRole;

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
     * Disposes the game state manager.
     */
    public void dispose() {
        client.removeListener(gameStateMessageListener);
    }
}
