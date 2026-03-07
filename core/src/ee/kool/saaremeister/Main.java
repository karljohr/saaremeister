package ee.kool.saaremeister;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.TitleScreen;

public class Main extends Game {

    @Override
    public void create() {
        ServerConnection.getInstance().connect();
        setScreen(new TitleScreen(this));
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
    }
}
