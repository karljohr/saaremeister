package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.game.GameStateManager;
import ee.kool.saaremeister.game.PlayerInputManager;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.utility.DebugRenderer;
import message.GameStateMessage;

import java.util.Arrays;

import static constant.Constants.*;

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

    private boolean debugMode;
    private DebugRenderer debugRenderer;

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

        debugRenderer = new DebugRenderer();
        debugMode = false;

        ServerConnection.getInstance().setStaticBodiesListener(bodies -> {
            Gdx.app.postRunnable(() -> debugRenderer.loadStaticBodies(Arrays.asList(bodies)));
        });
    }

    public void box2dWorldGenerator() {
        // Set up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Viewport
        this.viewport = new FitViewport(1600, 900, camera);

        // Load tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("shared/src/TMXAssets/map.tmx");
        this.renderer = new IsometricTiledMapRenderer(map, 1f);
    }

    private void update(float delta) {
        playerInputManager.handleMovementInput();
        handleScreenNavigation(game);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        GameStateMessage currentGameState = gameStateManager.getLatestGameStateMessage();
        arena.update(currentGameState);

        int myId = ServerConnection.getInstance().getClient().getID();

        // Dynamic camera
        arena.getPlayers().stream()
                .filter(p -> p.getId() == myId)
                .findFirst()
                .ifPresent(me -> {
                    float lerp = 5f;
                    camera.position.x += (me.getX() - camera.position.x) * lerp * delta;
                    camera.position.y += (me.getY() - camera.position.y) * lerp * delta;
                });

        camera.update();
        renderer.setView(camera);

        // TODO: playerInputManager.handleShootingInput(); // send player shooting info to server

        // TODO: hud.update(currentGameState);  // update info overlay time, lives etc
    }

    /**
     * Render a new frame.
     *
     * @param delta time since rendering the previous frame
     */
    @Override
    public void render(float delta) {
        // Update locations
        update(delta);

        // Draw
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render isometric map
        renderer.render();

        // Render players and items
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        arena.render(spriteBatch);
        spriteBatch.end();

        if (debugMode) {
            debugRenderer.render(camera);
        }

        // TODO: hud.render(); // render the HUD last to ensure it appears on top of all other content
    }

    @Override
    public void resize(int width, int height) {
        // See on hädavajalik, et FitViewport teaks uusi akna mõõte
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        map.dispose();
        renderer.dispose();
    }

    private void handleScreenNavigation(Game game) {
        // navigate to TitleScreen
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
        }
    }
}
