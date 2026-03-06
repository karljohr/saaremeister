package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.server.game.object.Player;

public class PlayerShootingListener extends Listener {

    private final Player player;

    public PlayerShootingListener(Player player) {
        this.player = player;
    }
}
