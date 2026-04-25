package ee.kool.saaremeister.screen.gamescreen.render.sources;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import ee.kool.saaremeister.game.Player;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.DepthRenderable;
import ee.kool.saaremeister.screen.gamescreen.render.utility.RenderSortKey;
import ee.kool.saaremeister.utility.IsoMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static constant.Constants.PLAYER_PIXEL_HEIGHT;

/**
 * Converts player world state into depth-sortable renderables.
 */
public class PlayerRenderSource {

    /**
     * Builds renderables for all currently visible players.
     */
    public List<DepthRenderable> build(List<Player> players) {
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        }

        List<DepthRenderable> renderables = new ArrayList<>(players.size());

        for (Player player : players) {
            float feetX = player.getX();
            float feetY = player.getY() - PLAYER_PIXEL_HEIGHT / 2f;
            GridPoint2 baseTile = IsoMath.worldToTileFeet(feetX, feetY);

            renderables.add(new DepthRenderable() {
                private final RenderSortKey sortKey = new RenderSortKey(baseTile.x, baseTile.y, 2);

                @Override
                public RenderSortKey getSortKey() {
                    return sortKey;
                }

                @Override
                public void render(SpriteBatch batch) {
                    player.render(batch);
                }
            });
        }

        return renderables;
    }
}
