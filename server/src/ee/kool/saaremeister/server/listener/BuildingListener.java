package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.server.game.object.Player;
import message.PlayerBuildMessage;

/**
 * Handles player building messages.
 */
public class BuildingListener extends Listener {

    private Player player;

    public BuildingListener(Player player) {
        this.player = player;
    }

    /**
     * Handles player building messages. If the player is not alive, the message is ignored.
     */
    @Override
    public void received(Connection connection, Object object) {
        if (!player.isAlive()) return;
        if (object instanceof PlayerBuildMessage message) {
            player.build(message);
        }
    }
}
