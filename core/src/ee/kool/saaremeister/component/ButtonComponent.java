package ee.kool.saaremeister.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import static com.badlogic.gdx.graphics.Color.BLACK;
import static ee.kool.saaremeister.utility.Font.getPixelFont;

/**
 * Utility class for creating different types of buttons.
 * Provides methods to generate standard TextButtons.
 */
public class ButtonComponent {

    /**
     * This allows passing custom click behavior (function) into the getButton method as a lambda or method reference.
     * In short, for handling clicks.
     */
    public interface OnClickHandler {
        void handleClick();  // Defines the action (function) to be triggered when the button is clicked.
    }

    /**
     * Creates a text button with a specified font size and text.
     *
     * @param fontSize       Font size of the button text.
     * @param text           Text displayed on the button.
     * @param onClickHandler The action (function) to perform when the button is clicked.
     */
    public static TextButton getButton(int fontSize, String text, OnClickHandler onClickHandler) {
        // styling the button
        TextButton.TextButtonStyle style = createButtonStyle(fontSize);
        TextButton button = createButtonWithPadding(text, style);

        // for handling clicks - a specific function is triggered with each click
        ClickListener clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClickHandler.handleClick();
                super.clicked(event, x, y);
            }
        };
        button.addListener(clickListener);
        return button;
    }

    private static TextButton.TextButtonStyle createButtonStyle(int fontSize) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.fontColor = BLACK;
        style.font = getPixelFont(fontSize);
        style.up = new TextureRegionDrawable(new TextureRegion(new Texture("button/up.png")));
        style.over = new TextureRegionDrawable(new TextureRegion(new Texture("button/down.png")));
        return style;
    }

    private static TextButton createButtonWithPadding(String text, TextButton.TextButtonStyle style) {
        TextButton button = new TextButton(text, style);
        button.pad(4);
        button.padLeft(8);
        button.padRight(8);
        return button;
    }
}
