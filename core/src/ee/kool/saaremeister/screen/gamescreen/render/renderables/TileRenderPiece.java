package ee.kool.saaremeister.screen.gamescreen.render.renderables;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ee.kool.saaremeister.screen.gamescreen.render.utility.RenderSortKey;

/**
 * Draws one tile or sprite region at a fixed world position with a shared depth sort key.
 */
public class TileRenderPiece implements DepthRenderable {

    private final TextureRegion region;
    private final float drawX;
    private final float drawY;
    private final float width;
    private final float height;
    private final RenderSortKey sortKey;

    public TileRenderPiece(
            TextureRegion region,
            float drawX,
            float drawY,
            float width,
            float height,
            int baseTileX,
            int baseTileY,
            int priority
    ) {
        this.region = region;
        this.drawX = drawX;
        this.drawY = drawY;
        this.width = width;
        this.height = height;
        this.sortKey = new RenderSortKey(baseTileX, baseTileY, priority);
    }

    /**
     * Returns the depth sort key shared by this render piece.
     */
    @Override
    public RenderSortKey getSortKey() {
        return sortKey;
    }

    /**
     * Draws the tile region at its cached world coordinates.
     */
    @Override
    public void render(SpriteBatch batch) {
        batch.draw(region, drawX, drawY, width, height);
    }
}
