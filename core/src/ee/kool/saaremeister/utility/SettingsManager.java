package ee.kool.saaremeister.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SettingsManager {
    private static final Settings instance = new Settings();

    public static Settings get() {
        return instance;
    }

}