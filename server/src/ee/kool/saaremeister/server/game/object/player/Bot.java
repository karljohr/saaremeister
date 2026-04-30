package ee.kool.saaremeister.server.game.object.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.CaptureZone;
import ee.kool.saaremeister.server.game.object.buildings.Building;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.OffenceBuilding;
import ee.kool.saaremeister.server.utility.AStar;
import message.dto.PlayerRole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static constant.Constants.*;

/**
 * An AI player that replaces real players in the game if the game instance is not full.
 */
public class Bot extends Player {
    private static final float STUCK_REPATH_SECONDS = 0.6f;
    private static final float STUCK_DISTANCE_EPSILON = 0.02f;

    private List<AStar.Node> currentPath = new ArrayList<>();
    private float pathUpdateTimer = 0;
    private float shootTimer = 0;
    private int currentTargetZoneId = -1;
    private final Vector2 lastBotPosition = new Vector2();
    private boolean hasLastBotPosition = false;
    private float stuckTimer = 0;

    /**
     * Creates a new bot player, that bypasses the network.
     */
    public Bot(int id, GameInstance gameInstance, World world, BuildHandler buildHandler) {
        super(null, id, gameInstance, world, buildHandler, 500, 3, PlayerRole.OFFICER);
        this.setUsername("BOT_" + id);
    }

    /**
     * Updates the AI movement toward capture objectives and its combat target selection.
     */
    public void updateAI(
            List<Player> players,
            List<Building> buildings,
            List<CaptureZone> captureZones,
            int[][] collisionGrid
    ) {
        if (!isAlive() || getBody() == null) return;

        pathUpdateTimer += 1 / 60f;
        shootTimer += 1000f / 60f;

        Vector2 botPos = getBody().getPosition();
        CaptureZone targetZone = findTargetZone(botPos, captureZones);
        Vector2 shootingTarget = findBestShootingTarget(botPos, players, buildings);
        updateStuckState(botPos, targetZone);

        if (targetZone == null && shootingTarget == null) {
            getBody().setLinearVelocity(0, 0);
            return;
        }

        if (targetZone == null) {
            getBody().setLinearVelocity(0, 0);
            currentPath.clear();
            currentTargetZoneId = -1;
            stuckTimer = 0;
        } else {
            if (currentTargetZoneId != targetZone.getZoneId()) {
                currentPath.clear();
                currentTargetZoneId = targetZone.getZoneId();
                stuckTimer = 0;
            }
            updateMovement(botPos, targetZone, collisionGrid);
        }

        if (shootTimer >= BULLET_TIMEOUT_MILLISECONDS) {
            handleShooting(botPos, shootingTarget);
            shootTimer = 0;
        }
    }

    /**
     * Finds the closest capture zone that officers do not control yet.
     */
    private CaptureZone findTargetZone(Vector2 botPos, List<CaptureZone> captureZones) {
        CaptureZone closestZone = null;
        float minDistance = Float.MAX_VALUE;

        for (CaptureZone zone : captureZones) {
            if (zone.getOwnerEnum() == CaptureZone.ZoneOwner.OFFICER) {
                continue;
            }

            Vector2 zoneCenter = new Vector2(
                    (zone.getX() + zone.getWidth() / 2f) / PPM,
                    (zone.getY() + zone.getHeight() / 2f) / PPM
            );
            float distance = botPos.dst2(zoneCenter);
            if (distance < minDistance) {
                minDistance = distance;
                closestZone = zone;
            }
        }

        return closestZone;
    }

    /**
     * Chooses the nearest combat target by priority:
     * 1. Master, gun tower, or ballistic rocket base.
     * 2. Walls.
     * 3. Nothing.
     */
    private Vector2 findBestShootingTarget(Vector2 botPos, List<Player> players, List<Building> buildings) {
        Vector2 priorityTarget = null;
        float priorityDistance = Float.MAX_VALUE;

        for (Player player : players) {
            if (player.getRole() != PlayerRole.MASTER || !player.isAlive() || player.getBody() == null) {
                continue;
            }

            float distance = botPos.dst2(player.getBody().getPosition());
            if (distance < priorityDistance) {
                priorityDistance = distance;
                priorityTarget = player.getBody().getPosition().cpy();
            }
        }

        for (Building building : buildings) {
            if (!(building instanceof OffenceBuilding) || building.getBody() == null) {
                continue;
            }

            float distance = botPos.dst2(building.getBody().getPosition());
            if (distance < priorityDistance) {
                priorityDistance = distance;
                priorityTarget = building.getBody().getPosition().cpy();
            }
        }

        if (priorityTarget != null) {
            return priorityTarget;
        }

        Vector2 fallbackTarget = null;
        float fallbackDistance = Float.MAX_VALUE;
        for (Building building : buildings) {
            if (!(building instanceof DefenceBuilding) || building.getBody() == null) {
                continue;
            }

            float distance = botPos.dst2(building.getBody().getPosition());
            if (distance < fallbackDistance) {
                fallbackDistance = distance;
                fallbackTarget = building.getBody().getPosition().cpy();
            }
        }

        return fallbackTarget;
    }

    /**
     * Updates bot movement toward its current capture-zone objective.
     */
    private void updateMovement(Vector2 botPos, CaptureZone targetZone, int[][] collisionGrid) {
        if (shouldHoldPosition(botPos, targetZone)) {
            getBody().setLinearVelocity(0, 0);
            currentPath.clear();
            stuckTimer = 0;
            return;
        }

        Vector2 zoneCenter = new Vector2(
                (targetZone.getX() + targetZone.getWidth() / 2f) / PPM,
                (targetZone.getY() + targetZone.getHeight() / 2f) / PPM
        );
        boolean needsRepath = pathUpdateTimer >= 0.5f || currentPath.isEmpty() || stuckTimer >= STUCK_REPATH_SECONDS;
        if (needsRepath) {
            currentPath = findPathToZone(botPos, targetZone, zoneCenter, collisionGrid);
            pathUpdateTimer = 0;
            if (!currentPath.isEmpty()) {
                stuckTimer = 0;
            }
        }

        if (!currentPath.isEmpty()) {
            AStar.Node nextNode = currentPath.get(0);
            Vector2 nextStepWorld = Box2dWorldGenerator.tileIndexToWorld(nextNode.x, nextNode.y);

            if (botPos.dst(nextStepWorld) < 0.4f) {
                currentPath.remove(0);
                if (currentPath.isEmpty()) {
                    getBody().setLinearVelocity(0, 0);
                    return;
                }
                nextNode = currentPath.get(0);
                nextStepWorld = Box2dWorldGenerator.tileIndexToWorld(nextNode.x, nextNode.y);
            }

            Vector2 direction = nextStepWorld.sub(botPos).nor();
            getBody().setLinearVelocity(direction.scl(BOT_SPEED / PPM));
            return;
        }

        Vector2 fallbackTarget = findClosestZoneWorldPoint(targetZone, botPos, collisionGrid);
        if (fallbackTarget == null) {
            getBody().setLinearVelocity(0, 0);
            return;
        }

        Vector2 direction = new Vector2(fallbackTarget).sub(botPos);
        if (direction.isZero(0.001f)) {
            getBody().setLinearVelocity(0, 0);
            return;
        }
        getBody().setLinearVelocity(direction.nor().scl(BOT_SPEED / PPM));
    }

    /**
     * Holds position only after the bot is inside the target zone and officer capture has actually started.
     */
    private boolean shouldHoldPosition(Vector2 botPos, CaptureZone targetZone) {
        return targetZone.contains(botPos.x * PPM, botPos.y * PPM)
                && targetZone.getCapturingEnum() == CaptureZone.ZoneOwner.OFFICER
                && !targetZone.isContested();
    }

    /**
     * Tracks whether the bot is failing to make progress toward a zone.
     */
    private void updateStuckState(Vector2 botPos, CaptureZone targetZone) {
        if (targetZone == null || shouldHoldPosition(botPos, targetZone)) {
            stuckTimer = 0;
            lastBotPosition.set(botPos);
            hasLastBotPosition = true;
            return;
        }

        if (!hasLastBotPosition) {
            lastBotPosition.set(botPos);
            hasLastBotPosition = true;
            stuckTimer = 0;
            return;
        }

        if (botPos.dst(lastBotPosition) < STUCK_DISTANCE_EPSILON) {
            stuckTimer += 1 / 60f;
        } else {
            stuckTimer = 0;
        }
        lastBotPosition.set(botPos);
    }

    /**
     * Finds the best path into the zone by testing reachable walkable tiles inside it.
     */
    private List<AStar.Node> findPathToZone(
            Vector2 botPos,
            CaptureZone zone,
            Vector2 zoneCenter,
            int[][] collisionGrid
    ) {
        Vector2 startTile = Box2dWorldGenerator.worldToTileIndex(botPos.x, botPos.y);
        int startX = (int) startTile.x;
        int startY = (int) startTile.y;
        AStar aStar = new AStar(collisionGrid);

        List<Vector2> candidateTiles = findZoneTiles(zone, zoneCenter, collisionGrid);
        candidateTiles.sort(Comparator.comparingDouble(tile -> {
            Vector2 tileWorld = Box2dWorldGenerator.tileIndexToWorld((int) tile.x, (int) tile.y);
            return tileWorld.dst2(zoneCenter) + tileWorld.dst2(botPos) * 0.25f;
        }));

        for (Vector2 candidateTile : candidateTiles) {
            List<AStar.Node> path = aStar.findPath(startX, startY, (int) candidateTile.x, (int) candidateTile.y);
            if (path != null && !path.isEmpty()) {
                return path;
            }
        }

        return new ArrayList<>();
    }

    /**
     * Finds walkable tiles inside the zone.
     */
    private List<Vector2> findZoneTiles(CaptureZone zone, Vector2 referenceWorldPos, int[][] collisionGrid) {
        Vector2 minCorner = Box2dWorldGenerator.worldToTileIndex(zone.getX() / PPM, zone.getY() / PPM);
        Vector2 maxCorner = Box2dWorldGenerator.worldToTileIndex(
                (zone.getX() + zone.getWidth()) / PPM,
                (zone.getY() + zone.getHeight()) / PPM
        );

        int minX = Math.max(0, Math.min((int) minCorner.x, (int) maxCorner.x) - 1);
        int maxX = Math.min(collisionGrid[0].length - 1, Math.max((int) minCorner.x, (int) maxCorner.x) + 1);
        int minY = Math.max(0, Math.min((int) minCorner.y, (int) maxCorner.y) - 1);
        int maxY = Math.min(collisionGrid.length - 1, Math.max((int) minCorner.y, (int) maxCorner.y) + 1);

        List<Vector2> candidateTiles = new ArrayList<>();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (collisionGrid[y][x] == 1) {
                    continue;
                }

                Vector2 tileWorld = Box2dWorldGenerator.tileIndexToWorld(x, y);
                if (!zone.contains(tileWorld.x * PPM, tileWorld.y * PPM)) {
                    continue;
                }

                candidateTiles.add(new Vector2(x, y));
            }
        }

        candidateTiles.sort(Comparator.comparingDouble(tile -> {
            Vector2 tileWorld = Box2dWorldGenerator.tileIndexToWorld((int) tile.x, (int) tile.y);
            return tileWorld.dst2(referenceWorldPos);
        }));
        return candidateTiles;
    }

    /**
     * Returns the nearest walkable world point inside the zone as a last-resort movement target.
     */
    private Vector2 findClosestZoneWorldPoint(CaptureZone zone, Vector2 botPos, int[][] collisionGrid) {
        List<Vector2> candidateTiles = findZoneTiles(zone, botPos, collisionGrid);
        if (candidateTiles.isEmpty()) {
            return null;
        }

        Vector2 closestTile = candidateTiles.getFirst();
        return Box2dWorldGenerator.tileIndexToWorld((int) closestTile.x, (int) closestTile.y);
    }

    /**
     * Allows bots to shoot only if the selected target is close enough.
     */
    private void handleShooting(Vector2 botPos, Vector2 targetPos) {
        if (targetPos == null) {
            return;
        }

        if (botPos.dst(targetPos) < 15f) {
            Vector2 shootDir = new Vector2(targetPos).sub(botPos).nor();
            this.shoot(shootDir);
        }
    }

    /**
     * Gives bot money for killing the Master.
     */
    @Override
    public void earnFromKill() {
        setMoney(getMoney() + OFFICER_KILL_PROFIT);
    }

    /**
     * Gives bot money for destroying a building built by the Master.
     */
    @Override
    public void earnFromDestroying() {
        setMoney(getMoney() + OFFICER_DESTROY_PROFIT);
    }

    /**
     * Gives bot income for every zone he holds.
     */
    @Override
    public void passiveIncome(int zoneCount) {
        setMoney(getMoney() + OFFICER_ZONE_PROFIT * zoneCount);
    }

    @Override
    public void dispose() {
        // Bot doesn't have listeners to remove.
    }
}
