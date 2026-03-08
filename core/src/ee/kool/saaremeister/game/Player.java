package ee.kool.saaremeister.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.utility.Sprites;
import lombok.Getter;
import lombok.Setter;

import static constant.Constants.PPM;
import static constant.Constants.PLAYER_PIXEL_HEIGHT;
import static constant.Constants.PLAYER_PIXEL_WIDTH;

public class Player {

    private final int id;
    @Setter @Getter
    private float x;
    @Setter @Getter
    private float y;

    public Player(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void render(SpriteBatch spriteBatch) {
        float w = 1.0f;
        float h = 1.0f;
        spriteBatch.draw(
                Sprites.playerTexture,
                x - w / 2f,
                y - h / 2f,
                w,
                h
        );

//        // x/y are in world units (pixels/PPM). Draw sprite centered with size also in world units.
//        float w = PLAYER_PIXEL_WIDTH / PPM;
//        float h = PLAYER_PIXEL_HEIGHT / PPM;
    }

}
