package ee.kool.saaremeister.screen.gamescreen;

import lombok.Setter;

/**
 * Contains methods for managing the state of the game screen.
 */
public class GameScreenState {
    private boolean shopOpen;
    private boolean wasChatFocused;
    private boolean buildMode;
    private boolean debugMode;
    @Setter
    private boolean paused;

    boolean isShopOpen() {
        return shopOpen;
    }

    void setShopOpen(boolean shopOpen) {
        this.shopOpen = shopOpen;
    }

    boolean wasChatFocused() {
        return wasChatFocused;
    }

    void setWasChatFocused(boolean wasChatFocused) {
        this.wasChatFocused = wasChatFocused;
    }

    boolean isBuildMode() {
        return buildMode;
    }

    void setBuildMode(boolean buildMode) {
        this.buildMode = buildMode;
    }

    boolean isDebugMode() {
        return debugMode;
    }

    void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    boolean isPaused() {return paused;}

}
