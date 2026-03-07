package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.game.GameStateManager;
import ee.kool.saaremeister.game.PlayerInputManager;
import ee.kool.saaremeister.screen.overlay.Hud;
import message.GameStateMessage;

import static constant.Constants.PPM;

public class GameScreen extends ScreenAdapter {

    private final Game game;
    private final GameStateManager gameStateManager;
    private final PlayerInputManager playerInputManager;

    private final SpriteBatch spriteBatch;
    private final Arena arena;
    private final int lobbyId;
    // TODO: private final Hud hud;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap map;
    private IsometricTiledMapRenderer renderer;

    /**
     * Initialize new GameScreen, where the main gameplay happens.
     *
     * @param game Current game instance, used for navigating between screens, for example GameScreen -> TitleScreen
     */
    public GameScreen(Game game, int lobbyId) {
        this.game = game;
        this.lobbyId = lobbyId;
        gameStateManager = new GameStateManager();  // receives game state from the server
        playerInputManager = new PlayerInputManager();  // listens for user input and sends it to the server

        spriteBatch = new SpriteBatch();  // where all the content is rendered
        arena = new Arena();  // manages players and bullets
        // TODO: hud = new Hud(spriteBatch);  // info overlay
        box2dWorldGenerator();
    }

    public void box2dWorldGenerator() {
        // set up camera
        camera = new OrthographicCamera(); // create 2D camera
        camera.setToOrtho(false, Gdx.graphics.getWidth() / PPM, Gdx.graphics.getHeight() / PPM); // set Y-up and viewport size in meters

        // viewport
        this.viewport = new FitViewport(800, 400, camera); // maintain aspect ratio with fixed world size

        // load tiled map
        TmxMapLoader mapLoader = new TmxMapLoader(); // loader for TMX tile maps
        this.map = mapLoader.load("shared/src/TMXAssets/map.tmx"); // load map file from shared. change as necessary
        this.renderer = new IsometricTiledMapRenderer(map, 1f / PPM); // render map scaled to meters
    }

    /**
     * Render a new frame.
     *
     * @param delta time since rendering the previous frame
     */
    @Override
    public void render(float delta) {
        super.render(delta);
        // clear the screen
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1); // to get that win 95 look
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update(); // Update the camera matrices
        renderer.setView(camera); // Set the renderer to use the current camera
        renderer.render(); // Draw the tiled map

        playerInputManager.handleMovementInput(); // send player movement info to server
        // TODO: playerInputManager.handleShootingInput(); // send player shooting info to server
        handleScreenNavigation(game); // ESC - navigate back to Title screen

        GameStateMessage currentGameState = gameStateManager.getLatestGameStateMessage();
        arena.update(currentGameState);  // update players' and bullets' positions
        // TODO: hud.update(currentGameState);  // update info overlay time, lives etc

        spriteBatch.setProjectionMatrix(camera.combined);

        // render players and bullets onto the screen
        spriteBatch.begin();
        arena.render(spriteBatch);  // all spriteBatch rendering should occur between .begin() and .end()
        spriteBatch.end();

        // TODO: hud.render(); // render the HUD last to ensure it appears on top of all other content
    }

    private void handleScreenNavigation(Game game) {
        // navigate to TitleScreen
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
        }
    }
}
