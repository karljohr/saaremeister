package ee.kool.saaremeister.screen.gamescreen.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PlacedBuildingRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PlayerRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PreviewRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.ProjectileRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.utility.DepthRenderQueue;
import ee.kool.saaremeister.screen.gamescreen.render.sources.StaticStructureMap;

/**
 * Builds and renders the depth-sorted world layer for a frame.
 */
public class DepthWorldRenderer {

    private final StaticStructureMap staticStructureMap;
    private final PlacedBuildingRenderSource placedBuildingRenderSource;
    private final PlayerRenderSource playerRenderSource;
    private final ProjectileRenderSource projectileRenderSource;
    private final PreviewRenderSource previewRenderSource;
    private final DepthRenderQueue queue = new DepthRenderQueue();

    public DepthWorldRenderer(
            StaticStructureMap staticStructureMap,
            PlacedBuildingRenderSource placedBuildingRenderSource,
            PlayerRenderSource playerRenderSource,
            ProjectileRenderSource projectileRenderSource,
            PreviewRenderSource previewRenderSource
    ) {
        this.staticStructureMap = staticStructureMap;
        this.placedBuildingRenderSource = placedBuildingRenderSource;
        this.playerRenderSource = playerRenderSource;
        this.projectileRenderSource = projectileRenderSource;
        this.previewRenderSource = previewRenderSource;
    }

    /**
     * Collects static structures, placed buildings, players, and projectiles into one sorted draw queue.
     */
    public void render(SpriteBatch batch, Arena arena) {
        queue.clear();
        queue.addAll(staticStructureMap.getRenderables());
        queue.addAll(placedBuildingRenderSource.getRenderables());
        queue.addAll(playerRenderSource.build(arena.getPlayers()));
        queue.addAll(projectileRenderSource.build(arena.getBullets(), arena.getRockets()));
        queue.addAll(previewRenderSource.getRenderables());
        queue.sort();
        queue.render(batch);
    }
}
