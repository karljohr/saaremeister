package ee.kool.saaremeister.screen.gamescreen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.Hud;
import ee.kool.saaremeister.screen.overlay.PauseOverlay;
import ee.kool.saaremeister.screen.overlay.ShopOverlay;

/**
 * Handles input for the game screen.
 */
public class GameScreenInputController {

    private final Hud hud;
    private final ShopOverlay shopOverlay;
    private final GameScreenState state;
    private final int lobbyId;
    private final PauseOverlay pauseOverlay;

    /**
     * Handles B before Scene2D so the shop toggles reliably (and chat does not eat the key).
     */
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

    private final InputProcessor pauseHotKeyProcessor = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                togglePause();
                return true;
            }
            return false;
        }
    };

    public GameScreenInputController(Hud hud,
                                     ShopOverlay shopOverlay,
                                     GameScreenState state,
                                     int lobbyId,
                                     PauseOverlay pauseOverlay)
    {
        this.hud = hud;
        this.shopOverlay = shopOverlay;
        this.state = state;
        this.lobbyId = lobbyId;
        this.pauseOverlay = pauseOverlay;
    }

    private void toggleShop() {
        if (state.isPaused()) return;
        boolean open = !state.isShopOpen();
        state.setShopOpen(open);
        shopOverlay.setOpen(open);
    }

    private void togglePause() {
        boolean paused = !state.isPaused();
        state.setPaused(paused);
        pauseOverlay.setOpen(paused);

        if (paused) {
            state.setShopOpen(false);
            shopOverlay.setOpen(false);
            hud.clearChatInputFocus();
        }
    }

    /**
     * Allows the chat to be accessed in the HUD.
     */
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        // Allow mousewheel to work everywhere
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // amountY is 1.0 (down) or -1.0 (up)
                if (amountY != 0 && !hud.isChatInputFocused() && !state.isShopOpen() && !state.isPaused()) {
                    hud.scrollHotbar((int) amountY);
                    return true;
                }
                return false;
            }
        });

        multiplexer.addProcessor(pauseHotKeyProcessor);
        multiplexer.addProcessor(shopHotkeyProcessor);
        multiplexer.addProcessor(pauseOverlay.getStage());
        multiplexer.addProcessor(shopOverlay.getStage());
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
                Gdx.app.postRunnable(() -> hud.updateChat(msg.getMessages()));
            }
        });

        connection.replayCachedChatUpdates(lobbyId);
    }

    /**
     * Removes the chat from the HUD.
     */
    public void hide() {
        ServerConnection.getInstance().setChatUpdateListener(null);
        Gdx.input.setInputProcessor(null);
    }
}
