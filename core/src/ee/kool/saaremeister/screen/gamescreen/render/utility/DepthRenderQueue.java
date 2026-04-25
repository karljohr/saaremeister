package ee.kool.saaremeister.screen.gamescreen.render.utility;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.DepthRenderable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores, sorts, and renders depth-sorted world objects for the current frame.
 */
public class DepthRenderQueue {

    private final List<DepthRenderable> items = new ArrayList<>();

    /**
     * Removes all queued renderables from the current frame.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Adds a single renderable to the queue.
     */
    public void add(DepthRenderable item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Adds a collection of renderables to the queue.
     */
    public void addAll(Collection<? extends DepthRenderable> renderables) {
        if (renderables != null) {
            items.addAll(renderables);
        }
    }

    /**
     * Sorts queued renderables using their depth sort keys.
     */
    public void sort() {
        items.sort((a, b) -> RenderSortKey.COMPARATOR.compare(a.getSortKey(), b.getSortKey()));
    }

    /**
     * Renders all queued items in sorted order.
     */
    public void render(SpriteBatch batch) {
        for (DepthRenderable item : items) {
            item.render(batch);
        }
    }
}
