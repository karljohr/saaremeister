package ee.kool.saaremeister.screen.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.game.BuildManager;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.screen.overlay.PauseOverlay;
import ee.kool.saaremeister.screen.overlay.ShopOverlay;
import ee.kool.saaremeister.utility.DebugRenderer;

/**
 * Controls rendering of the game screen.
 */
public class GameScreenRenderController {

    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final Arena arena;
    private final Hud hud;
    private final ShopOverlay shopOverlay;
    private final DebugRenderer debugRenderer;
    private final GameWorldView worldView;
    private final GameScreenState state;
    private final BuildManager buildManager;
    private final PauseOverlay pauseOverlay;

    public GameScreenRenderController(
            SpriteBatch spriteBatch,
            ShapeRenderer shapeRenderer,
            Arena arena,
            Hud hud,
            ShopOverlay shopOverlay,
            DebugRenderer debugRenderer,
            GameWorldView worldView,
            GameScreenState state,
            BuildManager buildManager,
            PauseOverlay pauseOverlay
    ) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
        this.arena = arena;
        this.hud = hud;
        this.shopOverlay = shopOverlay;
        this.debugRenderer = debugRenderer;
        this.worldView = worldView;
        this.state = state;
        this.buildManager = buildManager;
        this.pauseOverlay = pauseOverlay;
    }

    /**
     * Render a new frame.
     *
     * @param delta time since rendering the previous frame
     */
    public void render(float delta) {
        // Draw
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render isometric map
        int overLayIndex = worldView.getMap().getLayers().getIndex("Overlay");
        int groundIndex = worldView.getMap().getLayers().getIndex("ground");
        int tempIndex = worldView.getMap().getLayers().getIndex("temp");

        worldView.getRenderer().render(new int[]{groundIndex});

        // Render players and items
        shapeRenderer.setProjectionMatrix(worldView.getCamera().combined);
        spriteBatch.setProjectionMatrix(worldView.getCamera().combined);

        renderZoneLayer(1);
        renderZoneLayer(2);
        renderZoneLayer(3);
        renderZoneLayer(4);

        spriteBatch.begin();
        arena.renderPlayers(spriteBatch);
        arena.renderProjectiles(spriteBatch);
        spriteBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        arena.renderExplosions(shapeRenderer);
        shapeRenderer.end();

        // Render the isometric map overlay layers
        worldView.getRenderer().render(new int[]{overLayIndex, tempIndex});

        // Render building health bars
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        buildManager.renderHealthBars(shapeRenderer);
        shapeRenderer.end();

        shopOverlay.render(delta);
        hud.render(delta); // render the HUD last to ensure it appears on top of all other content
        pauseOverlay.render(delta);

        // Render debug
        if (state.isDebugMode()) {
            debugRenderer.render(worldView.getCamera());
        }
    }

    /**
     * Render a specific layer.
     */
    private void renderLayer(String name) {
        int index = worldView.getMap().getLayers().getIndex(name);
        if (index >= 0) worldView.getRenderer().render(new int[]{index});
    }

    /**
     * Render the zone layer for a specific zone depending on its state.
     */
    private void renderZoneLayer(int zoneId) {
        switch (arena.getZoneVisualState(zoneId)) {
            case MINE -> renderLayer("cz" + zoneId + "_mine");
            case ENEMY -> renderLayer("cz" + zoneId + "_enemy");
            case CONTESTED -> renderLayer("cz" + zoneId + "_contested");
            case NEUTRAL -> {
            }
        }
    }
}
