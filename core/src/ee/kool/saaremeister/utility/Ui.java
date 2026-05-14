package ee.kool.saaremeister.utility;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Ui {

    public static Viewport createViewport() {
        ScreenViewport viewport = new ScreenViewport();

        viewport.setUnitsPerPixel(
                1f / SettingsManager.get().uiScale
        );

        return viewport;
    }
}