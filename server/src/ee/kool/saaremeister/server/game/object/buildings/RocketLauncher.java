package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.game.object.projectiles.Rocket;

import java.time.Duration;
import java.time.LocalTime;

public class RocketLauncher extends OffenceBuilding {

    private static final int OPEN_TIME = 500;

    public  RocketLauncher(int id,
                           int tileX,
                           int tileY,
                           World world,
                           GameInstance gameInstance,
                           int health,
                           int range) {
        super(id, tileX, tileY, world, gameInstance, health, range);
        shootingFrequency = 5000;
    }

    @Override
    /**
     * Launches a rocket at the closest player.
     */
    public void shoot() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(lastShotTime.plus(Duration.ofMillis(OPEN_TIME)))) {
            id = 149;
        }
        if (now.isBefore(lastShotTime.plus(Duration.ofMillis(shootingFrequency)))) {
            return;
        }
        lastShotTime = now;

        Player closestPlayer = super.findClosestPlayer();
        // Log.info("[BUILDING] shooting at " + closestPlayer);
        if (closestPlayer == null) {
            return;
        }

        Vector2 target = new Vector2(closestPlayer.getX(), closestPlayer.getY());

        gameInstance.addBullet(
                new Rocket(this.getX(), this.getY(), target, this.getUniqueId())
        );
        id = 150;
        // Log.info("spawning bullet for " + uniqueId + " at " + x + "," + y + " dir(vector)=" + aim);
    }
}
