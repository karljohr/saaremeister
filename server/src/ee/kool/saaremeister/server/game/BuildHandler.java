package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.object.buildings.Building;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.OffenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.RocketLauncher;
import lombok.Getter;
import message.PlayerBuildMessage;
import message.dto.BuildingState;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the building process in the game.
 */
public class BuildHandler {

    @Getter
    private List<Building> buildings;
    private List<BuildingState> buildMessageQueue;
    private final World world;
    private final GameInstance gameInstance;

    public BuildHandler(World world, GameInstance gameInstance) {
        this.world = world;
        this.gameInstance = gameInstance;
        buildings = new ArrayList<>();
        buildMessageQueue = new ArrayList<>();
    }

    /**
     * Checks if the player can build on the given tile. The player must be the Island Master and the tile must be
     * buildable.
     */
    public boolean validate(int x, int y, int tileId) {
        TiledMap map = Box2dWorldGenerator.getMap();
        TiledMapTileSet ground = map.getTileSets().getTileSet("blocks");
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("ground");
        TiledMapTileLayer.Cell cell = layer.getCell(x, y);

        int id = 0;
        if (cell != null && cell.getTile() != null) {
            id = cell.getTile().getId();
        }

        // TODO: only continue if player is "saaremeister".

        // Check ground tile
        if (!(boolean) ground.getTile(id).getProperties().get("canBuildOn")) {
            return false;
        }

        // Check static buildings
        if (
                new ArrayList<Vector2>(List.of(
                        // Brown house
                        new Vector2(141, 123),
                        new Vector2(141, 122),
                        new Vector2(142, 123),
                        new Vector2(142, 122),
                        // White house
                        new Vector2(158, 102),
                        new Vector2(158, 101),
                        new Vector2(159, 102),
                        new Vector2(159, 101),
                        // Blue house
                        new Vector2(142, 111),
                        new Vector2(141, 111),
                        new Vector2(140, 111),
                        new Vector2(149, 111),
                        new Vector2(142, 112),
                        new Vector2(141, 112),
                        new Vector2(140, 112),
                        new Vector2(139, 112),
                        // Lighthouse
                        new Vector2(134, 95),
                        new Vector2(134, 96),
                        new Vector2(133, 95),
                        new Vector2(133, 96)
                ))
                        .contains(new Vector2(x, y))) {
            return false;
        }

        // Check existing buildings
        for (Building build : buildings) {
            if (BuildingInfo.getBuildingFromVariation(tileId) != null && BuildingInfo.getBuildingFromVariation(tileId) == 156) {
                for (BuildingState state : BuildingInfo.getWallTileLocations(new BuildingState(tileId, x, y))) {
                    if (tileCoordinatesMatch(state.x, state.y, build)) return false;
                }
            } else {
                if (tileCoordinatesMatch(x, y, build)) return false;
            }
        }
        return true;
    }

    /**
     * Checks if given coordinates are at the same place as the given building.
     * @param x coordinate.
     * @param y coordinate.
     * @param build existing building.
     * @return true if locations match.
     */
    private boolean tileCoordinatesMatch(int x, int y, Building build) {
        if (build instanceof DefenceBuilding) {
            return ((DefenceBuilding) build).getTiles().stream()
                    .map(tile -> new Vector2(tile.getX(), tile.getY()))
                    .toList()
                    .contains(new Vector2(x, y));
        } else {
            return build.getTileX() == x || build.getTileY() == y;
        }
    }

    /**
     * Adds a building to the building list.
     */
    public void build(PlayerBuildMessage message, Player player) {
        if (!validate(message.getTileX(), message.getTileY(), message.id)) {
            Log.info("[BuildHandler] Player(" + player.getId() + ") tried to build on an invalid tile (" + message.getTileX() + "; " + message.getTileY() + ")");
            return;
        }
        if (player.getBuildingPermitById().contains(BuildingInfo.getBuildingFromVariation(message.id))) {
            buildMessageQueue.add(new BuildingState(message.id, message.tileX, message.tileY));
            player.removeBuildingPermit(message.id);
            Log.info("[BuildHandler] Building added to the queue");
        } else {
            Log.info("[BuildHandler] Player doesn't have the right to build this building: " + message.id + " not in " + player.getBuildingPermitById());
        }
    }

    /**
     * Builds the building from the queue.
     */
    public void buildFromQueue() {
        if (buildMessageQueue.isEmpty()) return;
        BuildingState buildingState = buildMessageQueue.removeFirst();

        switch (BuildingInfo.getBuildingType(buildingState.id)) {
            case OFFENCE -> {
                buildings.add(
                        new OffenceBuilding(
                                buildingState.id,
                                buildingState.x,
                                buildingState.y,
                                world,
                                gameInstance,
                                BuildingInfo.getBuildingHealth(
                                        BuildingInfo.getBuildingFromVariation(buildingState.id)
                                ),
                                BuildingInfo.getRange(
                                        BuildingInfo.getBuildingFromVariation(buildingState.id)
                                )
                        )
                );
            }
            case DEFENCE -> {
                buildings.add(
                        new DefenceBuilding(
                                buildingState.id,
                                buildingState.x,
                                buildingState.y,
                                world,
                                BuildingInfo.getBuildingHealth(
                                        BuildingInfo.getBuildingFromVariation(buildingState.id)
                                )
                        )
                );
            }
            case ROCKET_LAUNCHER -> {
                buildings.add(
                        new RocketLauncher(
                                buildingState.id,
                                buildingState.x,
                                buildingState.y,
                                world,
                                gameInstance,
                                BuildingInfo.getBuildingHealth(
                                        BuildingInfo.getBuildingFromVariation(buildingState.id)
                                ),
                                BuildingInfo.getRange(
                                        BuildingInfo.getBuildingFromVariation(buildingState.id)
                                )
                        )
                );
            }
        }

        Log.info("[BuildHandler] Building " + BuildingInfo.getBuildingType(buildingState.id) + " placed at " + buildingState.x + ";" + buildingState.y);
    }

    /**
     * Makes all offense buildings shoot.
     */
    public void shoot() {
        for (Building building : buildings) {
            if (building instanceof OffenceBuilding offenceBuilding) {
                offenceBuilding.shoot();
            }
        }
    }
}
