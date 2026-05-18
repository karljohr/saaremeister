package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.buildings.Building;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.game.object.projectiles.Bullet;
import ee.kool.saaremeister.server.game.object.projectiles.Rocket;
import lombok.Setter;
import message.dto.ExplosionState;
import message.dto.PlayerRole;

import java.util.*;

import static constant.Constants.BULLET_DAMAGE;
import static constant.Constants.PPM;

/**
 * Handles bullet collisions with players, objects, and arena borders.
 */
public class BulletCollisionHandler {

    private final World world;
    private final Array<Body> worldBodies = new Array<>();
    @Setter
    private GameInstance gameInstance;

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

            if (bullet instanceof Rocket rocket) {
                if (!rocket.hasLanded()) continue;
            }

            float bulletX = bullet.getX() / PPM;
            float bulletY = bullet.getY() / PPM;

            if (bullet instanceof Rocket rocket) {

                // Rocket hitting ground & checking if anyone got hit.
                List<Player> hitPlayers = findHitPlayersInRange(
                        bulletX,
                        bulletY,
                        bullet.getShotById(),
                        players,
                        buildings
                );
                if (!hitPlayers.isEmpty()) {
                    hitPlayers.forEach(player -> player.takeBulletDamage(rocket.getDamage()));
                    Log.info("Players "
                            + hitPlayers.stream().map(Player::getId).toList()
                            + " got hit by rocket explosion"
                    );
                }
                if (gameInstance != null) {
                    ExplosionState explosionState = new ExplosionState();
                    explosionState.setX(bullet.getX());
                    explosionState.setY(bullet.getY());
                    gameInstance.addExplosion(explosionState);
                }
                bulletIterator.remove();
                continue;

            } else {

                // Hitting another player (not the shooter itself).
                Player hit = findHitPlayer(bulletX, bulletY, bullet.getShotById(), players, buildings);
                if (hit != null) {
                    hit.takeBulletDamage(resolvePlayerDamage(bullet.getShotById(), playersById));
                    checkDeathAndHandleProfits(hit, bullet.getShotById(), playersById);
                    Log.info("Player " + hit.getId() + " hit by bullet from " + bullet.getShotById());
                    bulletIterator.remove();
                    continue;
                }
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
    private Player findHitPlayer(float bulletX,
                                 float bulletY,
                                 int shooterId,
                                 List<Player> players,
                                 List<Building> buildings
    ) {
        PlayerRole shooterRole = findShooterRole(buildings, players, shooterId);

        for (Player player : players) {
            if (player.getId() == shooterId) continue;
            Body body = player.getBody();
            if (body == null) continue;

            if (player.getRole() == shooterRole) continue;

            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.testPoint(bulletX, bulletY)) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     * Finds the players that were in blast radius.
     * @return list of players.
     */
    private List<Player> findHitPlayersInRange(float bulletX,
                                               float bulletY,
                                               int shooterId,
                                               List<Player> players,
                                               List<Building> buildings
    ) {
        List<Player> hitPlayers = new ArrayList<>();
        List<Fixture> hitFixtures = new ArrayList<>();

        world.QueryAABB(fixture -> {
                    hitFixtures.add(fixture);
                    return true;
                },
                bulletX - Rocket.getBLAST_RADIUS(),
                bulletY - Rocket.getBLAST_RADIUS(),
                bulletX + Rocket.getBLAST_RADIUS(),
                bulletY + Rocket.getBLAST_RADIUS()
        );

        for (Player player : players) {
            PlayerRole shooterRole = findShooterRole(buildings, players, shooterId);
            if (player.getRole() == shooterRole) continue;
            Body body = player.getBody();
            if (body == null) continue;

            for (Fixture fixture : body.getFixtureList()) {
                if (hitFixtures.contains(fixture)) {
                    hitPlayers.add(player);
                }
            }
        }
        return hitPlayers;
    }

    /**
     * Checks if a bullet hits a blocking static body.
     */
    private boolean hitsBlockingStaticBody(float bulletX, float bulletY, int shooterId, Map<Integer, Player> playersById, Map<Integer, Building> buildingById) {
        Player shooter = playersById.get(shooterId);
        Body shooterBody = shooter != null ? shooter.getBody() : null;

        Building origin = buildingById.get(shooterId);
        Body originBody = origin != null ? origin.getBody() : null;

        PlayerRole shooterRole = findShooterRole(
                buildingById.values().stream().toList(),
                playersById.values().stream().toList(),
                shooterId
        );

        for (Body body : worldBodies) {
            if (body == shooterBody) continue;
            if (body == originBody) continue;
            if (body.getType() != BodyDef.BodyType.StaticBody) continue;

            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.testPoint(bulletX, bulletY)) {
                    if (shooterRole == PlayerRole.OFFICER) {
                        buildingById.entrySet().stream()
                                .filter(entry -> entry.getValue().getBody() == fixture.getBody())
                                .findAny()
                                .ifPresent(entry -> {
                                    entry.getValue().decreaseHealth(resolvePlayerDamage(shooterId, playersById));
                                    checkDeathAndHandleProfits(entry.getValue(), shooterId, playersById);
                                });
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private PlayerRole findShooterRole(List<Building> buildings, List<Player> players, int shooterId) {
        PlayerRole shooterRole;
        if (buildings.stream()
                .map(Building::getUniqueId)
                .anyMatch(id -> id == shooterId)
                || players.stream()
                .filter(p -> p.getRole() == PlayerRole.MASTER)
                .anyMatch(p -> p.getId() == shooterId)) {
            shooterRole = PlayerRole.MASTER;
        } else {
            shooterRole = PlayerRole.OFFICER;
        }
        return shooterRole;
    }

    private float resolvePlayerDamage(int shooterId, Map<Integer, Player> playersById) {
        Player shooter = playersById.get(shooterId);
        if (shooter == null) {
            return BULLET_DAMAGE;
        }
        return shooter.getBulletDamage();
    }

    /**
     * Checks if the player that got shot died, if so then distributes profits.
     * @param hit player that got hit.
     * @param shooterId id of the player that shot the bullet.
     * @param playersById a map of players' id to player object.
     */
    private void checkDeathAndHandleProfits(Player hit, int shooterId, Map<Integer, Player> playersById) {
        if (!playersById.containsKey(shooterId)) return;
        if (hit.getLives() > 0) return;

        playersById.get(shooterId).earnFromKill();
    }

    /**
     * Checks if the building that got shot broke, if so then distributes profits.
     * @param hit building that got hit.
     * @param shooterId id of the player that shot the bullet.
     * @param playersById a map of players' id to player object.
     */
    private void checkDeathAndHandleProfits(Building hit, int shooterId, Map<Integer, Player> playersById) {
        if (!playersById.containsKey(shooterId)) return;
        if (hit.getHealth() > 0) return;

        Player shooter = playersById.get(shooterId);
        shooter.earnFromDestroying();
    }
}
