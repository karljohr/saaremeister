package ee.kool.saaremeister.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Settings {
    public float musicVolume = 0.2f;
    public int width = 1920;
    public int height = 1080;
    public boolean fullscreen = false;
    public float uiScale = 1f;

    private Preferences prefs;

    private Preferences getPrefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("settings");;
        }
        return prefs;
    }

    public void save() {
        Preferences prefs = getPrefs();
        prefs.putFloat("musicVolume", musicVolume);
        prefs.putInteger("width", width);
        prefs.putInteger("height", height);
        prefs.putBoolean("fullscreen", fullscreen);
        prefs.putFloat("uiScale", uiScale);
        prefs.flush();
    }

    public void load() {
        Preferences prefs = getPrefs();
        musicVolume = prefs.getFloat("musicVolume", 0.2f);
        width = prefs.getInteger("width", 1920);
        height = prefs.getInteger("height", 1080);
        fullscreen = prefs.getBoolean("fullscreen", false);
        uiScale = prefs.getFloat("uiScale", 1f);
    }
}
