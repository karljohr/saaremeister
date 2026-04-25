package ee.kool.saaremeister.screen.gamescreen.render.utility;

import java.util.Comparator;

/**
 * Immutable sort key for ordering world objects in isometric depth order.
 */
public record RenderSortKey(int baseTileX, int baseTileY, int priority) {

    public static final Comparator<RenderSortKey> COMPARATOR = Comparator
            .comparingInt(RenderSortKey::diagonal)
            .thenComparingInt(RenderSortKey::baseTileX)
            .thenComparing(Comparator.comparingInt(RenderSortKey::baseTileY).reversed())
            .thenComparingInt(RenderSortKey::priority);

    /**
     * Returns the diagonal index used as the primary isometric sort component.
     */
    public int diagonal() {
        return baseTileX - baseTileY;
    }
}
