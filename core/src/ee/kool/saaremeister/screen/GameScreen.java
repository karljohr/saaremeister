package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import constant.Category;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.game.GameStateManager;
import ee.kool.saaremeister.game.PlayerInputManager;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.screen.overlay.ShopOverlay;
import ee.kool.saaremeister.utility.DebugRenderer;
import lombok.Getter;
import message.GameStateMessage;
import com.badlogic.gdx.InputMultiplexer;
import message.LobbyExitMessage;
import ee.kool.saaremeister.game.BuildManager;
import message.dto.ShopItem;

import java.util.Arrays;

public class GameScreen extends ScreenAdapter {

    private final Game game;
    @Getter
    private final GameStateManager gameStateManager;
    private final PlayerInputManager playerInputManager;

    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final Arena arena;
    @Getter
    private final int myId;
    private final int lobbyId;
    @Getter
    private final Hud hud;
    private final ShopOverlay shopOverlay;
    private boolean shopOpen;

    private boolean wasChatFocused;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private TiledMap map;
    private IsometricTiledMapRenderer renderer;

    private BuildManager buildManager;
    private boolean buildMode;

    private boolean debugMode;
    private final DebugRenderer debugRenderer;

    /**
     * Initialize a new GameScreen, where the main gameplay happens.
     *
     * @param game Current game instance, used for navigating between screens, for example, GameScreen -> TitleScreen
     */
    public GameScreen(Game game, int lobbyId, String username) {
        this.game = game;
        this.myId = ServerConnection.getInstance().getClient().getID();
        this.lobbyId = lobbyId;
        Skin skin = ((Main) game).getSkin();
        this.gameStateManager = new GameStateManager();  // receives game state from the server
        this.playerInputManager = new PlayerInputManager();  // listens for user input and sends it to the server

        this.spriteBatch = new SpriteBatch();  // where all the content is rendered
        this.shapeRenderer = new ShapeRenderer();
        this.arena = new Arena();  // manages players and bullets
        this.hud = new Hud(spriteBatch, lobbyId, username, skin);  // info overlay
        this.shopOverlay = new ShopOverlay(spriteBatch, this);

        box2dWorldGenerator();
        this.buildManager = new BuildManager(map, this);
        buildMode = false;

        debugRenderer = new DebugRenderer();
        debugMode = false;

        ServerConnection.getInstance().setStaticBodiesListener(bodies -> Gdx.app.postRunnable(() ->
                debugRenderer.loadStaticBodies(Arrays.asList(bodies))));
    }

    /**
     * Generates the Box2D world.
     */
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
        this.map = mapLoader.load("TMXAssets/map.tmx");
        this.renderer = new IsometricTiledMapRenderer(map, 1f);
    }

    /**
     * Update the game state.
     */
    private void update(float delta) {

        // Stops movement and shooting if the chat is focused and vice versa.
        boolean chatFocused = hud.isChatInputFocused();
        if (!chatFocused && !shopOpen) {
            playerInputManager.handleMovementInput();
            playerInputManager.handleActionInput(hud.getHotbar());

            // Hotbar keys
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) hud.selectHotbarSlot(0);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) hud.selectHotbarSlot(1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) hud.selectHotbarSlot(2);
        } else if (!wasChatFocused) {
            playerInputManager.stopMovement();
            playerInputManager.stopShooting();
        }
        wasChatFocused = chatFocused;

        handleScreenNavigation(game);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
            arena.getPlayers().stream()
                    .filter(p -> p.getId() == ServerConnection.getInstance().getClient().getID())
                    .findFirst()
                    .ifPresent(me -> {
                        System.out.println(me.getX() + ":" + me.getY());
                    });
        }
        if (buildMode) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouse);
            buildManager.renderProjection(mouse.x, mouse.y);
            buildManager.handleInput(mouse.x, mouse.y);
        }


        GameStateMessage currentGameState = gameStateManager.getLatestGameStateMessage();
        if (currentGameState == null) return;

        if (currentGameState.isGameOver()) {
            int gameWinnerId = currentGameState.getWinnerPlayerId();
            game.setScreen(new EndScreen(game, gameWinnerId, myId));
            return;
        }

        arena.update(currentGameState);

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

        buildManager.renderBatch(currentGameState.getBuildingStates());

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
        int overLay2Index = map.getLayers().getIndex("Overlay2");
        int groundIndex = map.getLayers().getIndex("ground");
        int tempIndex = map.getLayers().getIndex("temp");
        renderer.render(new int[]{groundIndex});

        // Render players and items
        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);
        renderer.render(new int[]{groundIndex});
        //arena.renderCaptureZones(shapeRenderer);
        spriteBatch.begin();
        arena.renderPlayers(spriteBatch);
        arena.renderBullets(spriteBatch);
        spriteBatch.end();

        // Render the isometric map overlay layers
        renderer.render(new int[]{overLayIndex, tempIndex});

        shopOverlay.render(delta);

        hud.render(delta); // render the HUD last to ensure it appears on top of all other content

        // Render debug
        if (debugMode) {
            debugRenderer.render(camera);
        }
    }

    /**
     * Allows the chat to be accessed in the HUD.
     */
    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        ServerConnection connection = ServerConnection.getInstance();

        // Allow mousewheel to work everywhere
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // amountY is 1.0 (down) or -1.0 (up)
                if (amountY != 0 && !hud.isChatInputFocused() && !shopOpen) {
                    hud.scrollHotbar((int) amountY);
                    return true;
                }
                return false;
            }
        });

        multiplexer.addProcessor(shopHotkeyProcessor);
        multiplexer.addProcessor(shopOverlay.getStage());
        multiplexer.addProcessor(hud.getStage());

        connection.getClient().addListener(new com.esotericsoftware.kryonet.Listener() {
            @Override
            public void received(com.esotericsoftware.kryonet.Connection conn, Object object) {
                if (object instanceof message.ShopPurchaseMessage msg) {
                    Gdx.app.postRunnable(() -> {
                        ShopItem item = common.ShopRegistry.getItemById(msg.getItemId());
                        if (item != null) {
                            if (item.getCategory() == Category.EHITISED) {
                                enterBuildMode(item);
                            } else {
                                // Add item to hotbar
                                boolean added = hud.getHotbar().addItem(item);
                                Gdx.app.log("UI", "Ese lisatud hotbari: " + added);
                            }
                        }
                        if (shopOverlay != null && shopOverlay.isOpen()) {
                            shopOverlay.refreshGrid();
                        }
                    });
                }
            }
        });

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

        connection.setChatUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> hud.updateChat(msg.getMessages()));
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
    }

    /**
     * Resizes the viewport when the window is resized.
     */
    @Override
    public void resize(int width, int height) {
        // It is necessary so that the FitViewport knows the size of the window.
        viewport.update(width, height);
        hud.getStage().getViewport().update(width, height, true);
        shopOverlay.resize(width, height);
    }

    /**
     * Disposes of the resources used by the game.
     */
    @Override
    public void dispose() {
        gameStateManager.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
        map.dispose();
        renderer.dispose();
        hud.dispose();
        shopOverlay.dispose();
        debugRenderer.dispose();
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

    /**
     * Handles the process of entering build mode after purchase.
     * @param item The building player wishes to place.
     */
    public void enterBuildMode(ShopItem item) {
        buildManager.setSelectedBuilding(item.getId());
        buildMode = true;
        shopOpen = false;
        shopOverlay.setOpen(false);
    }

    /**
     * Disables build mode.
     */
    public void leaveBuildMode() {
        buildMode = false;
    }

}
