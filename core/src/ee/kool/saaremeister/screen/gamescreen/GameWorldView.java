package ee.kool.saaremeister.screen.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ee.kool.saaremeister.game.BuildManager;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PlacedBuildingRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PreviewRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.StaticStructureMap;
import lombok.Getter;

/**
 * Controls rendering of the game world.
 */
public class GameWorldView {

    @Getter
    private final OrthographicCamera camera;
    @Getter
    private final FitViewport viewport;
    @Getter
    private final TiledMap map;
    @Getter
    private final StaticStructureMap staticStructureMap;
    private final IsometricTiledMapRenderer renderer;
    @Getter
    private final PlacedBuildingRenderSource placedBuildingRenderSource;
    @Getter
    private PreviewRenderSource previewRenderSource;

    /**
     * Generates the Box2D world.
     */
    public GameWorldView() {
        // Set up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Viewport
        this.viewport = new FitViewport(800, 450, camera);

        // Load tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        map = mapLoader.load("TMXAssets/map.tmx");
        renderer = new IsometricTiledMapRenderer(map, 1f);

        staticStructureMap = new StaticStructureMap(
                map,
                "Overlay",
                tileId -> tileId >= 20 && tileId < 275
        );
        placedBuildingRenderSource = new PlacedBuildingRenderSource(map);
    }

    public IsometricTiledMapRenderer getRenderer() {
        return renderer;
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void initPreview(BuildManager buildManager) {
        previewRenderSource = new PreviewRenderSource(map, buildManager);
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
