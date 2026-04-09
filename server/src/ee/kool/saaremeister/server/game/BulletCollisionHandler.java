package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Building;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.Player;

import java.util.*;

import static constant.Constants.PPM;

/**
 * Handles bullet collisions with players, objects, and arena borders.
 */
public class BulletCollisionHandler {

    private final World world;
    private final Array<Body> worldBodies = new Array<>();

    public BulletCollisionHandler(World world) {
        this.world = world;
    }

    /**
     * Handles bullet collisions with players and objects.
     */
    public void handleBulletCollisions(List<Bullet> bullets, List<Player> players, List<Building> buildings) {
        Map<Integer, Player> playersById = new HashMap<>();
        for (Player player : players) {
            playersById.put(player.getId(), player);
        }
        Map<Integer, Building> buildingById = new HashMap<>();
        for (Building building : buildings) {
            buildingById.put(building.getUniqueId(), building);
        }

        world.getBodies(worldBodies);

        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            float bulletX = bullet.getX() / PPM;
            float bulletY = bullet.getY() / PPM;

            // Hitting another player (not the shooter itself).
            Player hit = findHitPlayer(bulletX, bulletY, bullet.getShotById(), players);
            if (hit != null) {
                hit.decreaseLives();
                Log.info("Player " + hit.getId() + " hit by bullet from " + bullet.getShotById());
                bulletIterator.remove();
                continue;
            }

            // Hitting a blocking static body.
            if (hitsBlockingStaticBody(bulletX, bulletY, bullet.getShotById(), playersById, buildingById)) {
                bulletIterator.remove();
            }
        }
    }

    /**
     * Finds the player that the bullet hits.
     */
    private Player findHitPlayer(float bulletX, float bulletY, int shooterId, List<Player> players) {
        for (Player player : players) {
            if (player.getId() == shooterId) continue;
            Body body = player.getBody();
            if (body == null) continue;

            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.testPoint(bulletX, bulletY)) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a bullet hits a blocking static body.
     */
    private boolean hitsBlockingStaticBody(float bulletX, float bulletY, int shooterId, Map<Integer, Player> playersById, Map<Integer, Building> buildingById) {
        Player shooter = playersById.get(shooterId);
        Body shooterBody = shooter != null ? shooter.getBody() : null;

        Building origin = buildingById.get(shooterId);
        Body originBody = origin != null ? origin.getBody() : null;

        for (Body body : worldBodies) {
            if (body == shooterBody) continue;
            if (body == originBody) continue;
            if (body.getType() != BodyDef.BodyType.StaticBody) continue;

            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.testPoint(bulletX, bulletY)) {
                    return true;
                }
            }
        }
        return false;
    }
}
