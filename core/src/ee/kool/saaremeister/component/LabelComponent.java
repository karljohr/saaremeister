package ee.kool.saaremeister.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import static ee.kool.saaremeister.utility.Font.getPixelFont;

/**
 * Utility class for creating different types of labels.
 */
public class LabelComponent {
    public static Label createLabel(String text, Color textColor, int fontSize) {
        BitmapFont font = getPixelFont(fontSize);
        LabelStyle labelStyle = new Label.LabelStyle(font, textColor);
        return new Label(text, labelStyle);
    }
}
