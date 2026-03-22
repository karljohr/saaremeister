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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.game.GameStateManager;
import ee.kool.saaremeister.game.PlayerInputManager;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.utility.DebugRenderer;
import message.GameStateMessage;
import com.badlogic.gdx.InputMultiplexer;
import message.PlayerMovementMessage;

import java.util.Arrays;

public class GameScreen extends ScreenAdapter {

    private final Game game;
    private final GameStateManager gameStateManager;
    private final PlayerInputManager playerInputManager;

    private final SpriteBatch spriteBatch;
    private final Arena arena;
    private final int lobbyId;
    private final Skin skin;
    private final String username;
    private final Hud hud;

    private boolean wasChatFocused;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap map;
    private IsometricTiledMapRenderer renderer;

    private boolean debugMode;
    private DebugRenderer debugRenderer;

    /**
     * Initialize a new GameScreen, where the main gameplay happens.
     *
     * @param game Current game instance, used for navigating between screens, for example, GameScreen -> TitleScreen
     */
    public GameScreen(Game game, int lobbyId, String username) {
        this.game = game;
        this.lobbyId = lobbyId;
        this.username = username;
        this.skin = ((Main) game).getSkin();
        this.gameStateManager = new GameStateManager();  // receives game state from the server
        this.playerInputManager = new PlayerInputManager();  // listens for user input and sends it to the server

        this.spriteBatch = new SpriteBatch();  // where all the content is rendered
        this.arena = new Arena();  // manages players and bullets
        this.hud = new Hud(spriteBatch, lobbyId, username, skin);  // info overlay

        box2dWorldGenerator();

        debugRenderer = new DebugRenderer();
        debugMode = false;

        ServerConnection.getInstance().setStaticBodiesListener(bodies -> {
            Gdx.app.postRunnable(() -> debugRenderer.loadStaticBodies(Arrays.asList(bodies)));
        });
    }

    /**
     * Generates the Box2D world.
     */
    public void box2dWorldGenerator() {
        // Set up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Viewport
        this.viewport = new FitViewport(800, 450, camera);

        // Load tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("shared/src/TMXAssets/map.tmx");
        this.renderer = new IsometricTiledMapRenderer(map, 1f);
    }

    /**
     * Update the game state.
     */
    private void update(float delta) {

        // Stops movement if the chat is focused and vice versa.
        boolean chatFocused = hud.isChatInputFocused();
        if (!chatFocused) {
            playerInputManager.handleMovementInput();
        } else if (!wasChatFocused) {
            playerInputManager.stopMovement();
        }
        wasChatFocused = chatFocused;

        handleScreenNavigation(game);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }

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
        int overLayIndex = map.getLayers().getIndex("Overlay");
        int groundIndex = map.getLayers().getIndex("ground");
        renderer.render(new int[]{groundIndex});

        // Render players and items
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        arena.render(spriteBatch);
        spriteBatch.end();

        hud.render(delta); // render the HUD last to ensure it appears on top of all other content

        // Render the isometric map overlay layers
        renderer.render(new int[]{overLayIndex});
    }

    /**
     * Allows the chat to be accessed in the HUD.
     */
    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());

        // Determines should the focus be on chat or movement.
        hud.getStage().addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Actor target = event.getTarget();
                if (!hud.clickedInsideChatInput(target)) {
                    hud.clearChatInputFocus();
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);

        ServerConnection connection = ServerConnection.getInstance();
        connection.setChatUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    hud.updateChat(msg.getMessages());
                });
            }
        });

        connection.replayCachedChatUpdates(lobbyId);
    }

    /**
     * Removes the chat from the HUD.
     */
    @Override
    public void hide() {
        ServerConnection.getInstance().setChatUpdateListener(null);
        Gdx.input.setInputProcessor(null);


        if (debugMode) {
            debugRenderer.render(camera);
        }

        // TODO: hud.render(); // render the HUD last to ensure it appears on top of all other content
    }

    /**
     * Resizes the viewport when the window is resized.
     */
    @Override
    public void resize(int width, int height) {
        // It is necessary so that the FitViewport knows the size of the window.
        viewport.update(width, height);
        hud.getStage().getViewport().update(width, height, true);
    }

    /**
     * Disposes of the resources used by the game.
     */
    @Override
    public void dispose() {
        spriteBatch.dispose();
        map.dispose();
        renderer.dispose();
        hud.dispose();
    }

    /**
     * Handles screen navigation, for example, GameScreen -> TitleScreen.
     */
    private void handleScreenNavigation(Game game) {
        // navigate to TitleScreen and leave the game instance
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
            LobbyExitMessage exitMessage = new LobbyExitMessage();
            exitMessage.setLobbyId(lobbyId);
            ServerConnection.getInstance().getClient().sendTCP(exitMessage);
        }
    }
}
