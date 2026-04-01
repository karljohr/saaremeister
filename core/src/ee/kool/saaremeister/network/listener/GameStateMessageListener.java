package ee.kool.saaremeister.network.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.game.GameStateManager;
import message.GameStateMessage;

/**
 * Listener for incoming game state messages from the server.
 */
public class GameStateMessageListener extends Listener {

    private final GameStateManager gameStateManager;

    public GameStateMessageListener(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Determines what to do with the received message based on the message type.
     */
    @Override
    public void received(Connection connection, Object object) {
        super.received(connection, object);

        if (object instanceof GameStateMessage gameStateMessage) {
            gameStateManager.setLatestGameStateMessage(gameStateMessage);
        }
    }
}
