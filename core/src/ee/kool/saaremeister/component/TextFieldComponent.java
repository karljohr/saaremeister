package ee.kool.saaremeister.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import static ee.kool.saaremeister.utility.Font.getPixelFont;

/**
 * Utility class for creating different types of text fields.
 */
public class TextFieldComponent {

    public static TextField getTextField(int fontSize) {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = getPixelFont(fontSize);
        style.fontColor = Color.BLACK;
        style.background = new TextureRegionDrawable(new TextureRegion(new Texture("button/up.png")));
        style.cursor = new TextureRegionDrawable(new TextureRegion(new Texture("button/up.png")));
        return new TextField("", style);
    }
}
