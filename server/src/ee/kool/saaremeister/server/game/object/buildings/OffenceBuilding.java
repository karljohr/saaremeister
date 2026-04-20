package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.projectiles.Bullet;
import ee.kool.saaremeister.server.game.object.player.Officer;
import ee.kool.saaremeister.server.game.object.player.Player;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;

import static constant.Constants.PPM;

/**
 * Represents an offense building that shoots bullets at players.
 */
public class OffenceBuilding extends Building {

    protected int shootingFrequency = 650;
    protected LocalTime lastShotTime = LocalTime.MIN;
    protected final GameInstance gameInstance;
    protected int range;


    public OffenceBuilding(int id,
                           int tileX,
                           int tileY,
                           World world,
                           GameInstance gameInstance,
                           int health,
                           int range) {
        super(id, tileX, tileY, world, health);
        this.gameInstance = gameInstance;
        this.range = range;

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
        // Log.info("[BUILDING] shooting at " + closestPlayer);
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
    protected Player findClosestPlayer() {
        return gameInstance
                .getPlayers()
                .stream()
                .filter(Officer.class::isInstance)
                .filter(Player::isAlive)
                .filter(player ->
                        calcDistanceBetweenTwoPoints(
                                player.getX(), player.getY(),
                                this.getX(), this.getY()
                        ) < range
                )
                .min(Comparator.comparingInt(player -> (int) (
                        calcDistanceBetweenTwoPoints(
                                player.getX(), player.getY(),
                                this.getX(), this.getY())
                        )
                ))
                .orElse(null);
    }

    /**
     * Returns the vector pointing from the offense building to the player.
     */
    protected Vector2 buildingToPlayerAim(Player player) {
        float vectorX = player.getX() - this.getX();
        float vectorY = player.getY() - this.getY();

        Vector2 aim = new Vector2(vectorX, vectorY);
        if (aim.len2() == 0) {
            return new Vector2(0, 0);
        }

        return aim.nor();
    }

    /**
     * Returns the distance between 2 points.
     * @param x1 x coordinate of the first point.
     * @param y1 y coordinate of the first point.
     * @param x2 x coordinate of the second point.
     * @param y2 y coordinate of the second point.
     * @return distance between the 2 points.
     */
    protected float calcDistanceBetweenTwoPoints(float x1, float y1,
                                             float x2, float y2) {
        return new Vector2(x2 - x1, y2 - y1).len();
    }
}
