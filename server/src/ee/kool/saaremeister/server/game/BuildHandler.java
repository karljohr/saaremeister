package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.object.buildings.Building;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.OffenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.RocketLauncher;
import ee.kool.saaremeister.server.game.object.player.Player;
import lombok.Getter;
import message.PlayerBuildMessage;
import message.dto.BuildingState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles the building process in the game.
 */
public class BuildHandler {

    @Getter
    private List<Building> buildings;
    private final List<BuildingState> buildMessageQueue;
    private final World world;
    private final GameInstance gameInstance;

    public BuildHandler(World world, GameInstance gameInstance) {
        this.world = world;
        this.gameInstance = gameInstance;
        this.buildings = new ArrayList<>();
        this.buildMessageQueue = new ArrayList<>();
    }

    /**
     * Checks if the player can build at the given tile.
     */
    public boolean validate(int x, int y, int tileId) {
        TiledMap map = Box2dWorldGenerator.getMap();
        TiledMapTileSet groundSet = map.getTileSets().getTileSet("blocks");
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("ground");

        if (layer == null || groundSet == null) return false;

        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
        if (cell == null || cell.getTile() == null) return false;

        // Ground check (canBuildOn)
        var tile = groundSet.getTile(cell.getTile().getId());
        if (tile == null) return false;

        Object prop = tile.getProperties().get("canBuildOn");
        if (!(prop instanceof Boolean && (Boolean) prop)) {
            return false;
        }

        if (isAtStaticBuilding(x, y)) {
            return false;
        }

        // Remaining buildings check
        for (Building build : buildings) {
            Integer buildingId = BuildingInfo.getBuildingFromVariation(tileId);
            if (buildingId != null && buildingId.equals(BuildingInfo.getBuildingId(BuildingInfo.BuildingName.WALLS))) {
                for (BuildingState state : BuildingInfo.getWallTileLocations(new BuildingState(tileId, x, y))) {
                    if (tileCoordinatesMatch(state.x, state.y, build)) {
                        return false;
                    }
                }
            } else if (tileCoordinatesMatch(x, y, build)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a unique key for a tile.
     */
    private static long tileKey(int x, int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    /**
     * Static buildings are not allowed to be built on.
     */
    private static final Set<Long> STATIC_BUILDING_TILES = Set.of(
            tileKey(141,123), tileKey(141,122), tileKey(142,123), tileKey(142,122),
            tileKey(158,102), tileKey(158,101), tileKey(159,102), tileKey(159,101),
            tileKey(142,111), tileKey(141,111), tileKey(140,111), tileKey(149,111),
            tileKey(142,112), tileKey(141,112), tileKey(140,112), tileKey(139,112),
            tileKey(134,95),  tileKey(134,96),  tileKey(133,95),  tileKey(133,96)
    );

    /**
     * Checks if the given tile is a static building.
     */
    private boolean isAtStaticBuilding(int x, int y) {
        return STATIC_BUILDING_TILES.contains(tileKey(x, y));
    }

    /**
     * Checks if the given building is at the given tile coordinates.
     */
    private boolean tileCoordinatesMatch(int x, int y, Building build) {
        if (build instanceof DefenceBuilding) {
            return ((DefenceBuilding) build).getTiles().stream()
                    .anyMatch(tile -> (int) tile.getX() == x && (int) tile.getY() == y);
        }
        if (build instanceof RocketLauncher) {
            return Math.abs(build.getTileX() - x) <= 1 && Math.abs(build.getTileY() - y) <= 1;
        }
        return build.getTileX() == x && build.getTileY() == y;
    }

    /**
     * Builds a building at the given coordinates.
     */
    public void build(PlayerBuildMessage message, Player player) {
        if (!validate(message.getTileX(), message.getTileY(), message.id)) {
            player.refundBuildingPermit(message.id);
            return;
        }
        if (player.getBuildingPermitById().contains(BuildingInfo.getBuildingFromVariation(message.id))) {
            buildMessageQueue.add(new BuildingState(message.id, message.tileX, message.tileY));
            player.removeBuildingPermit(message.id);
        } else {
            Log.info("[BuildHandler] Player doesn't have the right to build this building: "
                    + message.id + " not in " + player.getBuildingPermitById());
        }
    }

    /**
     * Builds a building from the buildMessageQueue.
     */
    public void buildFromQueue() {
        if (buildMessageQueue.isEmpty()) return;
        BuildingState state = buildMessageQueue.removeFirst();

        Building newBuilding = null;
        var type = BuildingInfo.getBuildingType(state.id);

        // Health for all buildings
        int baseId = BuildingInfo.getBuildingFromVariation(state.id);
        int health = BuildingInfo.getBuildingHealth(baseId);

        if (type == BuildingInfo.BuildingType.OFFENCE) {
            int range = BuildingInfo.getRange(baseId);
            newBuilding = new OffenceBuilding(state.id, state.x, state.y, world, gameInstance, health, range);
        } else if (type == BuildingInfo.BuildingType.DEFENCE) {
            newBuilding = new DefenceBuilding(state.id, state.x, state.y, world, health);
        } else if (type == BuildingInfo.BuildingType.ROCKET_LAUNCHER) {
            int range = BuildingInfo.getRange(baseId);
            newBuilding = new RocketLauncher(state.id, state.x, state.y, world, gameInstance, health, range);
        }

        if (newBuilding != null) {
            buildings.add(newBuilding);
            Log.info("[BuildHandler] " + type + " placed at " + state.x + ";" + state.y);
        }
    }

    /**
     * Makes an offense building shoot.
     */
    public void shoot() {
        for (Building b : buildings) {
            if (b instanceof OffenceBuilding ob) ob.shoot();
        }
    }

    /**
     * Generates a collision grid for the buildings.
     */
    public int[][] generateCollisionGrid() {
        TiledMap map = Box2dWorldGenerator.getMap();
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);
        int[][] grid = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isAtStaticBuilding(x, y)) {
                    grid[y][x] = 1;
                }
            }
        }

        for (Building build : buildings) {
            if (build instanceof DefenceBuilding db) {
                for (BuildingState tile : db.getTiles()) {
                    int tx = tile.getX();
                    int ty = tile.getY();
                    if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                        grid[ty][tx] = 1;
                    }
                }
                continue;
            }

            int tx = build.getTileX();
            int ty = build.getTileY();
            if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                grid[ty][tx] = 1;
            }
        }
        return grid;
    }
}
