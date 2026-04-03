package constant;

import java.util.Map;

public final class ShopToTile {
    private static final Map<Integer, Integer> SHOP_ID_TO_TILE_ID = Map.of(
            8, 148
    );

    private ShopToTile() {}

    public static Integer getTileId(int shopId) {
        return SHOP_ID_TO_TILE_ID.get(shopId);
    }
}
