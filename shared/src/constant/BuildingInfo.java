package constant;

import message.PlayerBuildMessage;
import message.dto.BuildingState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BuildingInfo {
    private static final Map<Integer, Integer> SHOP_ID_TO_TILE_ID = Map.of(
            8, 148,
                9, 156
    );

    private static final Map<Integer, List<BuildingState>> TILE_ID_TO_WALL_TILE_LOCATION = Map.of(
            156, new ArrayList<>(List.of(

//                    new BuildingState(156, 131, 133),
//                    new BuildingState(158, 132, 133),
//                    new BuildingState(158, 133, 133),
//                    new BuildingState(162, 134, 133),
//                    new BuildingState(157, 131, 132),
//                    new BuildingState(161, 134, 132),
//                    new BuildingState(162, 135, 132),
//                    new BuildingState(157, 131, 131),
//                    new BuildingState(157, 135, 131),
//                    new BuildingState(159, 137, 131),
//                    new BuildingState(158, 138, 131),
//                    new BuildingState(158, 139, 131),
//                    new BuildingState(158, 140, 131),
//                    new BuildingState(158, 141, 131),
//                    new BuildingState(156, 142, 131),
//                    new BuildingState(161, 131, 130),
//                    new BuildingState(162, 132, 130),
//                    new BuildingState(161, 135, 130),
//                    new BuildingState(158, 136, 130),
//                    new BuildingState(160, 137, 130),
//                    new BuildingState(161, 142, 130),
//                    new BuildingState(158, 143, 130),
//                    new BuildingState(162, 144, 130),
//                    new BuildingState(157, 132, 129),
//                    new BuildingState(157, 144, 129),
//                    new BuildingState(161, 132, 128),
//                    new BuildingState(162, 133, 128),
//                    new BuildingState(157, 144, 128),
//                    new BuildingState(161, 133, 127),
//                    new BuildingState(162, 134, 127),
//                    new BuildingState(156, 144, 127),
//                    new BuildingState(161, 134, 126),
//                    new BuildingState(162, 135, 126),
//                    new BuildingState(157, 144, 126),
//                    new BuildingState(156, 135, 125),
//                    new BuildingState(157, 144, 125),
//                    new BuildingState(157, 135, 124),
//                    new BuildingState(161, 144, 124),
//                    new BuildingState(162, 145, 124),
//                    new BuildingState(159, 134, 123),
//                    new BuildingState(160, 135, 123),
//                    new BuildingState(157, 145, 123),
//                    new BuildingState(159, 132, 122),
//                    new BuildingState(158, 133, 122),
//                    new BuildingState(160, 134, 122),
//                    new BuildingState(157, 145, 122),
//                    new BuildingState(157, 132, 121),
//                    new BuildingState(157, 145, 121),
//                    new BuildingState(157, 132, 120),
//                    new BuildingState(161, 145, 120),
//                    new BuildingState(162, 146, 120),
//                    new BuildingState(157, 132, 119),
//                    new BuildingState(157, 146, 119),
//                    new BuildingState(156, 132, 118),
//                    new BuildingState(156, 146, 118),
//                    new BuildingState(157, 132, 117),
//                    new BuildingState(161, 146, 117),
//                    new BuildingState(162, 147, 117),
//                    new BuildingState(157, 132, 116),
//                    new BuildingState(157, 147, 116),
//                    new BuildingState(161, 132, 115),
//                    new BuildingState(162, 133, 115),
//                    new BuildingState(161, 147, 115),
//                    new BuildingState(162, 148, 115),
//                    new BuildingState(157, 133, 114),
//                    new BuildingState(161, 148, 114),
//                    new BuildingState(162, 149, 114),
//                    new BuildingState(161, 133, 113),
//                    new BuildingState(162, 134, 113),
//                    new BuildingState(161, 149, 113),
//                    new BuildingState(162, 150, 113),
//                    new BuildingState(161, 134, 112),
//                    new BuildingState(162, 135, 112),
//                    new BuildingState(161, 150, 112),
//                    new BuildingState(162, 151, 112),
//                    new BuildingState(157, 135, 111),
//                    new BuildingState(161, 151, 111),
//                    new BuildingState(156, 152, 111),
//                    new BuildingState(156, 135, 110),
//                    new BuildingState(161, 152, 110),
//                    new BuildingState(158, 153, 110),
//                    new BuildingState(162, 154, 110),
//                    new BuildingState(159, 134, 109),
//                    new BuildingState(160, 135, 109),
//                    new BuildingState(161, 154, 109),
//                    new BuildingState(162, 155, 109),
//                    new BuildingState(157, 134, 108),
//                    new BuildingState(161, 155, 108),
//                    new BuildingState(162, 156, 108),
//                    new BuildingState(157, 134, 107),
//                    new BuildingState(161, 156, 107),
//                    new BuildingState(158, 157, 107),
//                    new BuildingState(162, 158, 107),
//                    new BuildingState(157, 134, 106),
//                    new BuildingState(161, 158, 106),
//                    new BuildingState(162, 159, 106),
//                    new BuildingState(157, 134, 105),
//                    new BuildingState(161, 159, 105),
//                    new BuildingState(158, 160, 105),
//                    new BuildingState(156, 161, 105),
//                    new BuildingState(161, 134, 104),
//                    new BuildingState(162, 135, 104),
//                    new BuildingState(161, 161, 104),
//                    new BuildingState(162, 162, 104),
//                    new BuildingState(157, 135, 103),
//                    new BuildingState(161, 162, 103),
//                    new BuildingState(158, 163, 103),
//                    new BuildingState(162, 164, 103),
//                    new BuildingState(157, 135, 102),
//                    new BuildingState(161, 164, 102),
//                    new BuildingState(158, 165, 102),
//                    new BuildingState(158, 166, 102),
//                    new BuildingState(162, 167, 102),
//                    new BuildingState(157, 135, 101),
//                    new BuildingState(161, 167, 101),
//                    new BuildingState(158, 168, 101),
//                    new BuildingState(158, 169, 101),
//                    new BuildingState(156, 170, 101),
//                    new BuildingState(162, 171, 101),
//                    new BuildingState(156, 133, 100),
//                    new BuildingState(158, 134, 100),
//                    new BuildingState(160, 135, 100),
//                    new BuildingState(161, 171, 100),
//                    new BuildingState(162, 172, 100),
//                    new BuildingState(157, 133, 99),
//                    new BuildingState(159, 145, 99),
//                    new BuildingState(158, 146, 99),
//                    new BuildingState(158, 147, 99),
//                    new BuildingState(156, 148, 99),
//                    new BuildingState(158, 149, 99),
//                    new BuildingState(158, 150, 99),
//                    new BuildingState(158, 151, 99),
//                    new BuildingState(162, 152, 99),
//                    new BuildingState(159, 170, 99),
//                    new BuildingState(158, 171, 99),
//                    new BuildingState(160, 172, 99),
//                    new BuildingState(159, 132, 98),
//                    new BuildingState(160, 133, 98),
//                    new BuildingState(159, 144, 98),
//                    new BuildingState(160, 145, 98),
//                    new BuildingState(161, 152, 98),
//                    new BuildingState(158, 153, 98),
//                    new BuildingState(162, 154, 98),
//                    new BuildingState(159, 169, 98),
//                    new BuildingState(160, 170, 98),
//                    new BuildingState(159, 131, 97),
//                    new BuildingState(160, 132, 97),
//                    new BuildingState(159, 140, 97),
//                    new BuildingState(158, 141, 97),
//                    new BuildingState(158, 142, 97),
//                    new BuildingState(158, 143, 97),
//                    new BuildingState(160, 144, 97),
//                    new BuildingState(161, 154, 97),
//                    new BuildingState(158, 155, 97),
//                    new BuildingState(162, 156, 97),
//                    new BuildingState(157, 169, 97),
//                    new BuildingState(159, 130, 96),
//                    new BuildingState(160, 131, 96),
//                    new BuildingState(159, 139, 96),
//                    new BuildingState(160, 140, 96),
//                    new BuildingState(161, 156, 96),
//                    new BuildingState(162, 157, 96),
//                    new BuildingState(159, 168, 96),
//                    new BuildingState(160, 169, 96),
//                    new BuildingState(159, 129, 95),
//                    new BuildingState(160, 130, 95),
//                    new BuildingState(159, 137, 95),
//                    new BuildingState(158, 138, 95),
//                    new BuildingState(156, 139, 95),
//                    new BuildingState(161, 157, 95),
//                    new BuildingState(162, 158, 95),
//                    new BuildingState(159, 166, 95),
//                    new BuildingState(158, 167, 95),
//                    new BuildingState(156, 168, 95),
//                    new BuildingState(156, 129, 94),
//                    new BuildingState(159, 134, 94),
//                    new BuildingState(158, 135, 94),
//                    new BuildingState(158, 136, 94),
//                    new BuildingState(160, 137, 94),
//                    new BuildingState(156, 158, 94),
//                    new BuildingState(162, 159, 94),
//                    new BuildingState(159, 165, 94),
//                    new BuildingState(160, 166, 94),
//                    new BuildingState(161, 129, 93),
//                    new BuildingState(162, 130, 93),
//                    new BuildingState(159, 133, 93),
//                    new BuildingState(160, 134, 93),
//                    new BuildingState(161, 159, 93),
//                    new BuildingState(162, 160, 93),
//                    new BuildingState(159, 163, 93),
//                    new BuildingState(158, 164, 93),
//                    new BuildingState(160, 165, 93),
//                    new BuildingState(161, 130, 92),
//                    new BuildingState(162, 131, 92),
//                    new BuildingState(159, 132, 92),
//                    new BuildingState(160, 133, 92),
//                    new BuildingState(161, 160, 92),
//                    new BuildingState(158, 161, 92),
//                    new BuildingState(158, 162, 92),
//                    new BuildingState(160, 163, 92),
//                    new BuildingState(161, 131, 91),
//                    new BuildingState(160, 132, 91)
            ))
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
            156, List.of(156, 157)
    );

    private BuildingInfo() {}

    /**
     * Converts shop id to building tile id.
     * @param shopId Buildings id in shop.
     * @return Building tile id.
     */
    public static Integer getTileId(int shopId) {
        return SHOP_ID_TO_TILE_ID.get(shopId);
    }

    /**
     * Returns a list of BuildingStates for each piece of a wall.
     * @param message Wall middle piece BuildingState.
     * @return List of BuildingStates.
     */
    public static List<BuildingState> getWallTileLocations(BuildingState message) {
        int middleTile = 157;
        int extraX = 0;
        int extraY = 1;
        if (message.id == 157) {
            middleTile = 158;
            extraX = 1;
            extraY = 0;
        }
        return List.of(
                new BuildingState(156, message.getX() + (extraX * 2), message.getY() + (extraY * 2)),
                new BuildingState(middleTile, message.getX() + (extraX), message.getY()  + (extraY)),
                new BuildingState(middleTile, message.getX(), message.getY()),
                new BuildingState(middleTile, message.getX() + (extraX * (-1)), message.getY() +  (extraY * (-1))),
                new BuildingState(156, message.getX() + (extraX * (-2)), message.getY() + (extraY * (-2)))
        );
    }

    /**
     * Returns the type of building using the tile id.
     * @param id Tile id of the building.
     * @return BuildingType enum
     */
    public static BuildingType getBuildingType(int id) {
        return ID_TO_BUILDING_TYPE.get(BuildingType.OFFENCE).contains(id) ? BuildingType.OFFENCE : BuildingType.DEFENCE;
    }

    /**
     * Return different variations of requested building.
     * @param buildingId Tile id of the building.
     * @return List of different variation tile ids.
     */
    public static List<Integer> getBuildingVariations(int buildingId) {
        return BUILDING_VARIATIONS.get(buildingId);
    }

    /**
     * Returns the main id of the building from variationId.
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
}