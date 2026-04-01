package ee.kool.saaremeister.game;

import com.esotericsoftware.kryonet.Client;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.network.listener.GameStateMessageListener;
import lombok.Getter;
import lombok.Setter;
import message.GameStateMessage;

import java.util.ArrayList;

@Setter
@Getter
public class GameStateManager {

    private Client client;
    private final GameStateMessageListener gameStateMessageListener;
    private GameStateMessage latestGameStateMessage;

    /**
     * Creates a new instance of the game state manager.
     */
    public GameStateManager() {
        this.client = ServerConnection.getInstance().getClient();
        this.gameStateMessageListener = new GameStateMessageListener(this);
        this.client.addListener(gameStateMessageListener);

        latestGameStateMessage = new GameStateMessage();
        latestGameStateMessage.setPlayerStates(new ArrayList<>());
        latestGameStateMessage.setBulletStates(new ArrayList<>());
        latestGameStateMessage.setGameTime(0);
        latestGameStateMessage.setAllPlayersHaveJoined(false);
        latestGameStateMessage.setCaptureZones(new ArrayList<>());
        latestGameStateMessage.setGameOver(false);
        latestGameStateMessage.setWinnerPlayerId(-1);
    }

    public void dispose() {
        client.removeListener(gameStateMessageListener);
    }
}
