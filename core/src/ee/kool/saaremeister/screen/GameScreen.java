package ee.kool.saaremeister.screen;

import com.badlogic.gdx.*;
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
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.screen.overlay.ShopOverlay;
import message.GameStateMessage;

import static constant.Constants.PPM;

public class GameScreen extends ScreenAdapter {

    private final Game game;
    private final GameStateManager gameStateManager;
    private final PlayerInputManager playerInputManager;

    private final SpriteBatch spriteBatch;
    private final Arena arena;
    private final int lobbyId;
    private final String username;
    private final Hud hud;
    private final ShopOverlay shopOverlay;
    private boolean shopOpen;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap map;
    private IsometricTiledMapRenderer renderer;
    /**
     * Initialize a new GameScreen, where the main gameplay happens.
     *
     * @param game Current game instance, used for navigating between screens, for example, GameScreen -> TitleScreen
     */
    public GameScreen(Game game, int lobbyId, String username) {
        this.game = game;
        this.lobbyId = lobbyId;
        this.username = username;
        this.gameStateManager = new GameStateManager();  // receives game state from the server
        this.playerInputManager = new PlayerInputManager();  // listens for user input and sends it to the server

        this.spriteBatch = new SpriteBatch();  // where all the content is rendered
        this.arena = new Arena();  // manages players and bullets
        this.hud = new Hud(spriteBatch, lobbyId, username);  // info overlay
        this.shopOverlay = new ShopOverlay(spriteBatch);

        box2dWorldGenerator();
    }

    private void toggleShop() {
        shopOpen = !shopOpen;
        shopOverlay.setOpen(shopOpen);
    }

    // Handles B before Scene2D so the shop toggles reliably (and chat does not eat the key).
    private final InputProcessor shopHotkeyProcessor = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.B) {
                toggleShop();
                return true;
            }
            return false;
        }
    };

    public void box2dWorldGenerator() {
        // Set up camera
        camera = new OrthographicCamera();

        // Viewport
        this.viewport = new FitViewport(30, 20, camera);

        // Load tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("shared/src/TMXAssets/map.tmx");
        this.renderer = new IsometricTiledMapRenderer(map, 1f / PPM);
    }

    private void update(float delta) {
        if (!shopOpen) {
            playerInputManager.handleMovementInput();
        }
        handleScreenNavigation(game);

        GameStateMessage currentGameState = gameStateManager.getLatestGameStateMessage();

        if (currentGameState == null) return;
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

        hud.update(currentGameState, myId);  // update info overlay time, lives etc
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

        hud.render(delta);
        shopOverlay.render(delta);
    }

    /**
     * Allows the chat to be accessed in the HUD.
     */
    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(shopHotkeyProcessor);
        multiplexer.addProcessor(shopOverlay.getStage());
        multiplexer.addProcessor(hud.getStage());
        Gdx.input.setInputProcessor(multiplexer);
        resize(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        ServerConnection.getInstance().setChatUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    hud.updateChat(msg.getMessages());
                });
            }
        });
    }

    @Override
    public void hide() {
        ServerConnection.getInstance().setChatUpdateListener(null);
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hud.getStage().getViewport().update(width, height, true);
        shopOverlay.resize(width, height);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        map.dispose();
        renderer.dispose();
        hud.dispose();
        shopOverlay.dispose();
    }

    private void handleScreenNavigation(Game game) {
        // navigate to TitleScreen
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
        }
    }
}
