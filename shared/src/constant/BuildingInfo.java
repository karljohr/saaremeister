package constant;

import message.dto.BuildingState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Information about the buildings in the game.
 */
public final class BuildingInfo {
    private static final Map<Integer, Integer> SHOP_ID_TO_TILE_ID = Map.of(
            8, 148,
            9, 156
    );

    public enum BuildingType {
        DEFENCE,
        OFFENCE
    }

    private final static Map<BuildingType, List<Integer>> ID_TO_BUILDING_TYPE = Map.of(
            BuildingType.DEFENCE, List.of(156, 157),
            BuildingType.OFFENCE, List.of(148)
    );

    private final static Map<Integer, List<Integer>> BUILDING_VARIATIONS = Map.of(
            156, List.of(156, 157),
            148, List.of(148)
    );

    private static final Map<Integer, Integer> BUILDING_TO_HEALTH = Map.of(
            156, 20,
            148, 10
    );

    private static final Map<Integer, Integer> BUILDING_TO_RANGE = Map.of(
            148, 400
    );

    private BuildingInfo() {
    }

    /**
     * Converts shop id to building tile id.
     *
     * @param shopId Buildings id in shop.
     * @return Building tile id.
     */
    public static Integer getTileId(int shopId) {
        return SHOP_ID_TO_TILE_ID.get(shopId);
    }

    /**
     * Converts tile id to building shop id.
     * @param tileId building tile id
     * @return shop id
     */
    public static Integer getShopId(int tileId) {
        return SHOP_ID_TO_TILE_ID
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == tileId)
                .findFirst()
                .get()
                .getKey();
    }

    /**
     * Returns a list of BuildingStates for each piece of a wall.
     *
     * @param message Wall middle piece BuildingState.
     * @return List of BuildingStates.
     */
    public static List<BuildingState> getWallTileLocations(BuildingState message) {

        BuildingState middleTile = message;

        int middleTileI = 157;
        int extraX = 0;
        int extraY = 1;
        if (message.id == 157) {
            middleTileI = 158;
            extraX = 1;
            extraY = 0;
        }
        middleTile.setId(middleTileI);
        return List.of(
                new BuildingState(156, message.getX() + (extraX * 2), message.getY() + (extraY * 2)),
                new BuildingState(middleTileI, message.getX() + (extraX), message.getY() + (extraY)),
                middleTile,
                new BuildingState(middleTileI, message.getX() + (extraX * (-1)), message.getY() + (extraY * (-1))),
                new BuildingState(156, message.getX() + (extraX * (-2)), message.getY() + (extraY * (-2)))
        );
    }

    /**
     * Returns the type of building using the tile id.
     *
     * @param id Tile id of the building.
     * @return BuildingType enum
     */
    public static BuildingType getBuildingType(int id) {
        return ID_TO_BUILDING_TYPE.get(BuildingType.OFFENCE).contains(id) ? BuildingType.OFFENCE : BuildingType.DEFENCE;
    }

    /**
     * Return different variations of requested building.
     *
     * @param buildingId Tile id of the building.
     * @return List of different variation tile ids.
     */
    public static List<Integer> getBuildingVariations(int buildingId) {
        return BUILDING_VARIATIONS.get(buildingId);
    }

    /**
     * Returns the main id of the building from variationId.
     *
     * @param variationId variation id of the building.
     * @return main (Integer) id of the building.
     */
    public static Integer getBuildingFromVariation(int variationId) {
        return BUILDING_VARIATIONS.keySet()
                .stream()
                .filter(key -> BUILDING_VARIATIONS.get(key).contains(variationId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Return the health of the building by its buildingId.
     * @param buildingId tileId of the building.
     * @return building health Integer.
     */
    public static Integer getBuildingHealth(int buildingId) {
        return BUILDING_TO_HEALTH.get(buildingId);
    }


    /**
     * Return the range of the building.
     * @param buildingId tile id of th building.
     * @return range.
     */
    public static Integer getRange(int buildingId) {
        return BUILDING_TO_RANGE.get(buildingId);
    }
}