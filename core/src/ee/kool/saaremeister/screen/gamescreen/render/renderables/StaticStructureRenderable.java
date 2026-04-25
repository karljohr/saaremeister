package ee.kool.saaremeister.screen.gamescreen.render.renderables;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.screen.gamescreen.render.utility.RenderSortKey;

import java.util.List;

public class StaticStructureRenderable implements DepthRenderable {

    private final List<TileRenderPiece> pieces;
    private final RenderSortKey sortKey;

    public StaticStructureRenderable(List<TileRenderPiece> pieces, int baseX, int baseY) {
        this.pieces = pieces;
        this.sortKey = new RenderSortKey(baseX, baseY, 0);
    }

    @Override
    public RenderSortKey getSortKey() {
        return sortKey;
    }

    @Override
    public void render(SpriteBatch batch) {
        for (TileRenderPiece piece : pieces) {
            piece.render(batch);
        }
    }
}