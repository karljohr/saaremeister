package ee.kool.saaremeister.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.utility.Sprites;
import lombok.Getter;
import lombok.Setter;

import static constant.Constants.PPM;
import static constant.Constants.PLAYER_PIXEL_HEIGHT;
import static constant.Constants.PLAYER_PIXEL_WIDTH;
import static ee.kool.saaremeister.utility.Font.getArialFont;

public class Player {

    private final int id;
    private String username;
    @Setter @Getter
    private float x;
    @Setter @Getter
    private float y;

    public Player(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void render(SpriteBatch spriteBatch) {
        // x/y are in world units (pixels/PPM). Draw sprite centered with size also in world units.
        float w = PLAYER_PIXEL_WIDTH / PPM;
        float h = PLAYER_PIXEL_HEIGHT / PPM;
        spriteBatch.draw(
                Sprites.playerTexture,
                x - w / 2f,
                y - h / 2f,
                PLAYER_PIXEL_WIDTH / PPM,
                PLAYER_PIXEL_HEIGHT / PPM
        );
    }

}
