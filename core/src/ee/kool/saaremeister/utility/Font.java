package ee.kool.saaremeister.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Font {
    private static final FreeTypeFontGenerator pixelFontGenerator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/PixelifySans-VariableFont_wght.ttf"));
    private static final FreeTypeFontGenerator arialFontGenerator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));


    public static BitmapFont getPixelFont(int fontSize) {
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;

        return pixelFontGenerator.generateFont(parameter);
    }

    public static BitmapFont getArialFont(int fontSize) {
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;

        return arialFontGenerator.generateFont(parameter);
    }

}
