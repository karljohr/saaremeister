package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.server.game.object.player.Player;
import message.PlayerMovementMessage;

/**
 * Handles player movement messages.
 */
public class PlayerMovementListener extends Listener {

    private final Player player;

    public PlayerMovementListener(Player player) {
        this.player = player;
    }

    /**
     * Handles player movement messages. If the player is not alive, the message is ignored.
     */
    public void received(Connection connection, Object object) {
        if (!player.isAlive()) return;
        if (object instanceof PlayerMovementMessage playerMovementMessage) {
            player.move(playerMovementMessage.getDirectionX(), playerMovementMessage.getDirectionY());
        }
    }
}
