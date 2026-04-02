package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.Player;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.PPM;

/**
 * Handles bullet collisions with players, objects, and arena borders.
 */
public class BulletCollisionHandler {

    public List<Bullet> handleBulletCollisions(List<Bullet> bullets, List<Player> players) {
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Player player : players) {
            for (Bullet bullet : bullets) {
                if (bullet.getShotById() != player.getId() && bulletHitsPlayer(bullet, player)) {
                    player.decreaseLives();
                    bulletsToRemove.add(bullet);
                    Log.info("Player with id " + player.getId() + " was hit. " + player.getLives() + " lives left.");
                }
            }
        }
        bulletsToRemove.addAll(findOutOfBoundsBullets(bullets));
        bullets.removeAll(bulletsToRemove);
        return bullets;
    }

    /**
     * Checks if a bullet hits a player.
     */
    private boolean bulletHitsPlayer(Bullet bullet, Player player) {
        Body body = player.getBody();
        if (body == null) return false;

        float bulletX = bullet.getX() / PPM;
        float bulletY = bullet.getY() / PPM;

        for (Fixture fixture : body.getFixtureList()) {
            if (fixture.testPoint(bulletX, bulletY)) {
                return true;
            }
        }
        return false;
    }

    private List<Bullet> findOutOfBoundsBullets(List<Bullet> bullets) {
        List<Bullet> outOfBoundsBullets = new ArrayList<>();
        // TODO: implement
        return outOfBoundsBullets;
    }
}
