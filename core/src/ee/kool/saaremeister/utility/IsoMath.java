package ee.kool.saaremeister.utility;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import static constant.Constants.RENDER_OFFSETX;
import static constant.Constants.TILE_HEIGHT;
import static constant.Constants.TILE_WIDTH;

/**
 * Utility methods for converting between tile-space and world-space in the isometric map.
 */
public final class IsoMath {

    private IsoMath() {}

    /**
     * Converts a tile coordinate to the bottom-left world position used for tile rendering.
     */
    public static Vector2 tileToWorldBottomLeft(int tileX, int tileY) {
        float halfW = TILE_WIDTH / 2f;
        float halfH = TILE_HEIGHT / 2f;

        float worldX = (tileX + tileY) * halfW + RENDER_OFFSETX;
        float worldY = (tileY - tileX) * halfH;
        return new Vector2(worldX, worldY);
    }

    /**
     * Converts a tile coordinate to the center point of the tile in world space.
     */
    public static Vector2 tileToWorldCenter(int tileX, int tileY) {
        float halfW = TILE_WIDTH / 2f;
        float halfH = TILE_HEIGHT / 2f;

        Vector2 bottomLeft = tileToWorldBottomLeft(tileX, tileY);
        return new Vector2(bottomLeft.x + halfW, bottomLeft.y + halfH);
    }

    /**
     * Converts a world position to the corresponding isometric tile coordinate.
     */
    public static GridPoint2 worldToTile(float worldX, float worldY) {
        float halfW = TILE_WIDTH / 2f;
        float halfH = TILE_HEIGHT / 2f;

        float localX = worldX - RENDER_OFFSETX;
        float localY = worldY;

        int tileX = (int) Math.floor((localX / halfW - localY / halfH) / 2f);
        int tileY = (int) Math.floor((localX / halfW + localY / halfH) / 2f);
        return new GridPoint2(tileX, tileY);
    }

    /**
     * Converts a character feet position to the corresponding tile coordinate for depth sorting.
     */
    public static GridPoint2 worldToTileFeet(float feetWorldX, float feetWorldY) {
        return worldToTile(feetWorldX, feetWorldY);
    }

    /**
     * Converts a cell position inside a tile layer to the world position used for manual tile rendering.
     */
    public static Vector2 tileLayerCellToWorldBottomLeft(TiledMapTileLayer layer, int tileX, int tileY) {
        float halfW = layer.getTileWidth() / 2f;
        float halfH = layer.getTileHeight() / 2f;

        float worldX = (tileX + tileY) * halfW + layer.getRenderOffsetX();
        float worldY = (tileY - tileX) * halfH + layer.getRenderOffsetY();
        return new Vector2(worldX, worldY);
    }
}
