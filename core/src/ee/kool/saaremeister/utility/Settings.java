package ee.kool.saaremeister.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Graphics.DisplayMode;

public class Settings {
    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    private static final int MIN_WINDOWED_WIDTH = 800;
    private static final int MIN_WINDOWED_HEIGHT = 600;
    private static final int WINDOW_MARGIN = 20;

    public float musicVolume = 0.2f;
    public int width = DEFAULT_WIDTH;
    public int height = DEFAULT_HEIGHT;
    public boolean fullscreen = false;
    public float uiScale = 1f;

    private Preferences prefs;

    private Preferences getPrefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("settings");
        }
        return prefs;
    }

    public void save() {
        sanitizeForCurrentDisplay();

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
        width = prefs.getInteger("width", DEFAULT_WIDTH);
        height = prefs.getInteger("height", DEFAULT_HEIGHT);
        fullscreen = prefs.getBoolean("fullscreen", false);
        uiScale = prefs.getFloat("uiScale", 1f);
        sanitizeForCurrentDisplay();
    }

    /**
     * Replaces invalid saved windowed settings with a safe resolution for the current display.
     */
    public void sanitizeForCurrentDisplay() {
        if (fullscreen) {
            return;
        }

        DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        int maxWidth = Math.max(MIN_WINDOWED_WIDTH, displayMode.width - WINDOW_MARGIN);
        int maxHeight = Math.max(MIN_WINDOWED_HEIGHT, displayMode.height - WINDOW_MARGIN);

        if (width > maxWidth || height > maxHeight || width < MIN_WINDOWED_WIDTH || height < MIN_WINDOWED_HEIGHT) {
            width = Math.min(DEFAULT_WIDTH, maxWidth);
            height = Math.min(DEFAULT_HEIGHT, maxHeight);
        }
    }
}
