package ee.kool.saaremeister;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import ee.kool.saaremeister.game.Player;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.TitleScreen;
import ee.kool.saaremeister.utility.Settings;
import ee.kool.saaremeister.utility.SettingsManager;
import ee.kool.saaremeister.utility.Ui;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Main class of the game on the client side.
 */
public class
Main extends Game {
    @Getter
    @Setter
    private Player player;

    @Getter
    private Skin skin;

    private static Main instance;

    @Override
    public void create() {
        instance = this;

        Settings s = SettingsManager.get();
        s.load();

        if (s.fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(s.width, s.height);
        }

        skin = new Skin(Gdx.files.internal("ui/skin/craftacular/craftacular-ui.json"));

        ServerConnection.getInstance().connect();
        setScreen(new TitleScreen(this));
    }

    /**
     * Sets the current screen of the game.
     */
    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
    }

    /**
     * Disposes of the skin when the game is disposed.
     */
    @Override
    public void dispose() {
        super.dispose();

        SettingsManager.get().save();

        if (skin != null) {
            skin.dispose();
        }
    }
}
