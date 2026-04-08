package ee.kool.saaremeister.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.kool.saaremeister.utility.Sprites;
import lombok.Getter;
import lombok.Setter;

import static constant.Constants.PLAYER_PIXEL_HEIGHT;
import static constant.Constants.PLAYER_PIXEL_WIDTH;

/**
 * Client side representation of a player.
 */
public class Player {

    private final int id;
    @Setter @Getter
    private float x;
    @Setter @Getter
    private float y;

    /**
     * Creates a new Player instance.
     */
    public Player(int id) {
        this.id = id;
    }

    /**
     * Returns the player's id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Renders the player on the screen.
     */
    public void render(SpriteBatch spriteBatch) {
        // x/y are in world units (pixels/PPM). Draw sprite centered with size also in world units.
        float w = PLAYER_PIXEL_WIDTH;
        float h = PLAYER_PIXEL_HEIGHT;
        spriteBatch.draw(
                Sprites.playerTexture,
                x - w / 2f,
                y - h / 2f,
                w,
                h
        );
    }
}
