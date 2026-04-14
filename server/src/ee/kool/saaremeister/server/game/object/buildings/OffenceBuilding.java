package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.Player;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;

import static constant.Constants.PPM;

/**
 * Represents an offense building that shoots bullets at players.
 */
public class OffenceBuilding extends Building {

    private final int shootingFrequency = 650;
    private LocalTime lastShotTime = LocalTime.MIN;
    private final GameInstance gameInstance;


    public OffenceBuilding(int id, int tileX, int tileY, World world, GameInstance gameInstance, int health) {
        super(id, tileX, tileY, world, health);
        this.gameInstance = gameInstance;

        defineBody();

        super.x = body.getPosition().x * PPM;
        super.y = body.getPosition().y * PPM;

    }

    /**
     * Shoots a bullet at the closest player.
     */
    public void shoot() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(lastShotTime.plus(Duration.ofMillis(shootingFrequency)))) {
            return;
        }
        lastShotTime = now;

        Player closestPlayer = findClosestPlayer();
        if (closestPlayer == null) {
            return;
        }

        Vector2 aim = buildingToPlayerAim(closestPlayer);

        gameInstance.addBullet(
                new Bullet(this.getX(), this.getY(), aim, this.getUniqueId())
        );
        // Log.info("spawning bullet for " + uniqueId + " at " + x + "," + y + " dir(vector)=" + aim);
    }

    /**
     * Finds the closest player to the offense building.
     */
    private Player findClosestPlayer() {
        return gameInstance
                .getPlayers()
                .stream()
                .min(Comparator.comparingInt(player -> (int) ((
                        (player.getX() - this.getX()) * (player.getX() - this.getX()))
                        + ((player.getY() - this.getY()) * (player.getY() - this.getY())))
                ))
                .orElse(null);
    }

    /**
     * Returns the vector pointing from the offense building to the player.
     */
    private Vector2 buildingToPlayerAim(Player player) {
        float vectorX = player.getX() - this.getX();
        float vectorY = player.getY() - this.getY();

        Vector2 aim = new Vector2(vectorX, vectorY);
        if (aim.len2() == 0) {
            return new Vector2(0, 0);
        }

        return aim.nor();
    }
}
