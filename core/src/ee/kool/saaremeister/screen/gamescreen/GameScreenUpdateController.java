package ee.kool.saaremeister.screen.gamescreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import ee.kool.saaremeister.game.Arena;
import ee.kool.saaremeister.game.BuildManager;
import ee.kool.saaremeister.game.GameStateManager;
import ee.kool.saaremeister.game.PlayerInputManager;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.EndScreen;
import ee.kool.saaremeister.screen.overlay.Hud;
import message.GameStateMessage;
import message.dto.PlayerRole;

/**
 * Updates the in-game screen each frame by handling input, build mode, camera movement, screen transitions, and
 * syncing/rendering the latest server game state.
 */
public class GameScreenUpdateController {

    private final Game game;
    private final int myId;
    private PlayerRole myRole;
    private final GameStateManager gameStateManager;
    private final PlayerInputManager playerInputManager;
    private final Arena arena;
    private final Hud hud;
    private final BuildManager buildManager;
    private final GameWorldView worldView;
    private final GameScreenState state;

    public GameScreenUpdateController(
            Game game,
            int myId,
            GameStateManager gameStateManager,
            PlayerInputManager playerInputManager,
            Arena arena,
            Hud hud,
            BuildManager buildManager,
            GameWorldView worldView,
            GameScreenState state
    ) {
        this.game = game;
        this.myId = myId;
        this.gameStateManager = gameStateManager;
        this.playerInputManager = playerInputManager;
        this.arena = arena;
        this.hud = hud;
        this.buildManager = buildManager;
        this.worldView = worldView;
        this.state = state;
    }

    /**
     * Update the game state.
     */
    public void update(float delta) {

        // Stops movement and shooting if the chat is focused and vice versa.
        boolean chatFocused = hud.isChatInputFocused();
        boolean canControlPlayer = !chatFocused && !state.isShopOpen() && !state.isPaused();
        if (canControlPlayer) {
            playerInputManager.handleMovementInput();
            playerInputManager.handleActionInput(hud.getHotbar());
        } else {
            playerInputManager.stopMovement();
            playerInputManager.stopShooting();
        }
        state.setWasChatFocused(chatFocused);

        if (canControlPlayer) {
            // Hotbar keys
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) hud.selectHotbarSlot(0);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) hud.selectHotbarSlot(1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) hud.selectHotbarSlot(2);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            state.setDebugMode(!state.isDebugMode());
            arena.getPlayers().stream()
                    .filter(p -> p.getId() == ServerConnection.getInstance().getClient().getID())
                    .findFirst()
                    .ifPresent(me -> System.out.println(me.getX() + ":" + me.getY()));
        }

        if (state.isBuildMode() && !state.isPaused()) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            worldView.getCamera().unproject(mouse);
            buildManager.renderProjection(mouse.x, mouse.y);
            buildManager.handleInput(mouse.x, mouse.y);
        }

        GameStateMessage currentGameState = gameStateManager.getLatestGameStateMessage();

        if (currentGameState.isGameOver()) {
            var gameWinnerRole = currentGameState.getWinnerPlayerRole();
            game.setScreen(new EndScreen(game, gameWinnerRole, myRole));
            return;
        }

        if (currentGameState == null) return;

        // resolve once (role does not change mid-game)
        if (myRole == null) {
            myRole = gameStateManager.resolveMyRole(currentGameState);
        }

        if (currentGameState.isGameOver()) {
            var gameWinnerRole = currentGameState.getWinnerPlayerRole();
            game.setScreen(new EndScreen(game, gameWinnerRole, myRole));
            return;
        }

        arena.update(currentGameState);

        // Dynamic camera
        arena.getPlayers().stream()
                .filter(p -> p.getId() == myId)
                .findFirst()
                .ifPresent(me -> {
                    float lerp = 5f;
                    worldView.getCamera().position.x += (me.getX() - worldView.getCamera().position.x) * lerp * delta;
                    worldView.getCamera().position.y += (me.getY() - worldView.getCamera().position.y) * lerp * delta;
                });

        worldView.getCamera().update();
        worldView.getRenderer().setView(worldView.getCamera());

        // Shooting direction based on mouse position.
        if (canControlPlayer) {
            arena.getPlayers().stream()
                    .filter(p -> p.getId() == myId)
                    .findFirst()
                    .ifPresent(me -> {
                        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                        worldView.getViewport().unproject(mouse);       // viewport for FitViewport correctness
                        playerInputManager.handleShootingInput(me.getX(), me.getY(), mouse.x, mouse.y);
                    });
        }

        buildManager.renderBatch(currentGameState.getBuildingStates());
        buildManager.compareBuildingOldHealthToNew(currentGameState.getBuildingStates());

        hud.update(currentGameState, myId, myRole);  // update info overlay time, lives, etc.
    }
}
