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

public class BuildHandler {

    @Getter
    private List<Building> buildings;
    private List<BuildingState> buildMessageQueue;
    private final World world;
    private final GameInstance gameInstance;

    public BuildHandler(World world, GameInstance gameInstance) {
        this.world = world;
        this.gameInstance = gameInstance;
        this.buildings = new ArrayList<>();
        this.buildMessageQueue = new ArrayList<>();
    }

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
            if (buildingId != null && buildingId == 156) {
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

    private boolean isAtStaticBuilding(int x, int y) {
        return new ArrayList<>(List.of(
                new Vector2(141, 123), new Vector2(141, 122), new Vector2(142, 123), new Vector2(142, 122),
                new Vector2(158, 102), new Vector2(158, 101), new Vector2(159, 102), new Vector2(159, 101),
                new Vector2(142, 111), new Vector2(141, 111), new Vector2(140, 111), new Vector2(149, 111),
                new Vector2(142, 112), new Vector2(141, 112), new Vector2(140, 112), new Vector2(139, 112),
                new Vector2(134, 95), new Vector2(134, 96), new Vector2(133, 95), new Vector2(133, 96)
        )).contains(new Vector2(x, y));
    }

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

    public void shoot() {
        for (Building b : buildings) {
            if (b instanceof OffenceBuilding ob) ob.shoot();
        }
    }

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
