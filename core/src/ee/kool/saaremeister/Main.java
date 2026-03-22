package ee.kool.saaremeister;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import ee.kool.saaremeister.game.Player;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.TitleScreen;
import lombok.Getter;
import lombok.Setter;

public class Main extends Game {
    @Getter
    @Setter
    private Player player;

    @Getter
    private Skin skin;

    private static Main instance;

    /**
     * Creates a new Main instance.
     */
    @Override
    public void create() {
        instance = this;
        skin = new Skin(Gdx.files.internal("ui/skin/comic-ui.json"));
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
        if (skin != null) {
            skin.dispose();
        }
    }
}
