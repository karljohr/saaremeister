package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.server.game.object.Player;
import message.PlayerBuildMessage;

public class BuildingListener extends Listener {

    private Player player;

    public BuildingListener(Player player) {
        this.player = player;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof PlayerBuildMessage message) {
            player.build(message);
        }
    }
}
