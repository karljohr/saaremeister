package ee.kool.saaremeister;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import ee.kool.saaremeister.game.Player;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.TitleScreen;
import lombok.Getter;
import lombok.Setter;

public class Main extends Game {
    @Getter @Setter
    private Player player;

    private static Main instance;

    @Override
    public void create() {
        instance = this;
        ServerConnection.getInstance().connect();
        setScreen(new TitleScreen(this));
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
    }
}
