package ee.kool.saaremeister.screen.gamescreen.render.renderables;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.screen.gamescreen.render.utility.RenderSortKey;

/**
 * Represents a renderable world object that participates in depth sorting.
 */
public interface DepthRenderable {
    /**
     * Returns the sort key used to order this object in the depth render queue.
     */
    RenderSortKey getSortKey();

    /**
     * Draws this object using the provided sprite batch.
     */
    void render(SpriteBatch batch);
}
