package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Player;
import message.PlayerShootingMessage;

import java.time.Duration;
import java.time.LocalTime;

import static constant.Constants.BULLET_TIMEOUT_MILLISECONDS;

/**
 * Handles shooting input of a client and applies shooting logic.
 */
public class PlayerShootingListener extends Listener {

    private final Player player;
    private LocalTime lastShotTime = LocalTime.MIN;

    public PlayerShootingListener(Player player) {
        this.player = player;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (!(object instanceof PlayerShootingMessage msg)) {
            return;
        }
        if (msg.getDirection() == null) {
            return;
        }
        LocalTime now = LocalTime.now();
        if (now.isBefore(lastShotTime.plus(Duration.ofMillis(BULLET_TIMEOUT_MILLISECONDS)))) {
            return;
        }
        lastShotTime = now;
        player.shoot(msg.getDirection());
        Log.info("shot msg from " + connection.getID() + ", dir=" + msg.getDirection());
    }
}
