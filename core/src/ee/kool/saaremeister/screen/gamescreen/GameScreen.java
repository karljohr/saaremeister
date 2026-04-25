package ee.kool.saaremeister.screen.gamescreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.game.*;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.TitleScreen;
import ee.kool.saaremeister.screen.gamescreen.render.DepthWorldRenderer;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PlayerRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PreviewRenderSource;
import ee.kool.saaremeister.screen.gamescreen.render.sources.ProjectileRenderSource;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.screen.overlay.PauseOverlay;
import ee.kool.saaremeister.screen.overlay.ShopOverlay;
import ee.kool.saaremeister.utility.DebugRenderer;
import lombok.Getter;
import message.LobbyExitMessage;
import message.dto.ShopItem;

import java.util.Arrays;

public class GameScreen extends ScreenAdapter {

    @Getter
    private final int myId;

    @Getter
    private final GameStateManager gameStateManager;
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    @Getter
    private final Hud hud;
    private final ShopOverlay shopOverlay;
    private final BuildManager buildManager;
    private final DebugRenderer debugRenderer;
    private final PauseOverlay pauseOverlay;

    private final GameScreenState screenState;
    private final GameWorldView worldView;
    private final GameScreenInputController inputController;
    private final GameScreenUpdateController updateController;
    private final GameScreenRenderController renderController;
    private final DepthWorldRenderer depthWorldRenderer;

    /**
     * Initialize a new GameScreen, where the main gameplay happens.
     *
     * @param game Current game instance, used for navigating between screens, for example, GameScreen -> TitleScreen
     */
    public GameScreen(Game game, int lobbyId, String username) {
        this.myId = ServerConnection.getInstance().getClient().getID();

        Skin skin = ((Main) game).getSkin();

        this.gameStateManager = new GameStateManager();  // receives game state from the server
        PlayerInputManager playerInputManager = new PlayerInputManager();  // listens for user input and sends it to the server
        this.spriteBatch = new SpriteBatch();  // where all the content is rendered
        this.shapeRenderer = new ShapeRenderer();
        Arena arena = new Arena();  // manages players and bullets
        this.hud = new Hud(spriteBatch, lobbyId, username, skin);  // info overlay
        this.shopOverlay = new ShopOverlay(spriteBatch, this);

        this.screenState = new GameScreenState();
        this.worldView = new GameWorldView();
        this.buildManager = new BuildManager(worldView.getMap(), this);
        this.worldView.initPreview(this.buildManager);

        this.pauseOverlay = new PauseOverlay(
                spriteBatch,
                () -> {
                    screenState.setPaused(false);
                },
                () -> {
                    leaveMatch(game, lobbyId);
                },
                ((Main) game).getSkin()
        );
        depthWorldRenderer = new DepthWorldRenderer(
                worldView.getStaticStructureMap(),
                worldView.getPlacedBuildingRenderSource(),
                new PlayerRenderSource(),
                new ProjectileRenderSource(),
                worldView.getPreviewRenderSource()
        );

        this.debugRenderer = new DebugRenderer();

        this.inputController = new GameScreenInputController(hud, shopOverlay, screenState, lobbyId, pauseOverlay);
        this.updateController = new GameScreenUpdateController(
                game,
                myId,
                gameStateManager,
                playerInputManager,
                arena,
                hud,
                buildManager,
                worldView,
                screenState
        );
        this.renderController = new GameScreenRenderController(
                spriteBatch,
                shapeRenderer,
                arena,
                hud,
                shopOverlay,
                debugRenderer,
                worldView,
                screenState,
                buildManager,
                pauseOverlay,
                depthWorldRenderer
        );

        ServerConnection.getInstance().setStaticBodiesListener(bodies -> Gdx.app.postRunnable(() ->
                debugRenderer.loadStaticBodies(Arrays.asList(bodies))));
    }

    @Override
    public void render(float delta) {
        updateController.update(delta);
        renderController.render(delta);
    }

    @Override
    public void show() {
        inputController.show();
    }

    @Override
    public void hide() {
        inputController.hide();
    }

    /**
     * Resizes the viewport when the window is resized.
     */
    @Override
    public void resize(int width, int height) {
        // It is necessary so that the FitViewport knows the size of the window.
        worldView.resize(width, height);
        hud.getStage().getViewport().update(width, height, true);
        shopOverlay.resize(width, height);
        pauseOverlay.resize(width, height);
    }

    /**
     * Disposes of the resources used by the game.
     */
    @Override
    public void dispose() {
        gameStateManager.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
        worldView.dispose();
        hud.dispose();
        shopOverlay.dispose();
        debugRenderer.dispose();
        pauseOverlay.dispose();
    }

    /**
     * Handles the process of entering build mode after purchase.
     *
     * @param item The building player wishes to place.
     */
    public void enterBuildMode(ShopItem item) {
        buildManager.setSelectedBuilding(item.getId());
        screenState.setBuildMode(true);
        screenState.setShopOpen(false);
        shopOverlay.setOpen(false);
        hud.changeBuildLegendVisibility();
    }

    /**
     * Disables build mode.
     */
    public void leaveBuildMode() {
        screenState.setBuildMode(false);
        hud.changeBuildLegendVisibility();
    }

    /**
     * Handles player leaving a match.
     */
    public void leaveMatch(Game game, int lobbyId) {
        LobbyExitMessage exitMessage = new LobbyExitMessage();
        exitMessage.setLobbyId(lobbyId);
        ServerConnection.getInstance().getClient().sendTCP(exitMessage);

        ServerConnection.getInstance().setStaticBodiesListener(null);

        game.setScreen(new TitleScreen(game));
        dispose();
    }
}
