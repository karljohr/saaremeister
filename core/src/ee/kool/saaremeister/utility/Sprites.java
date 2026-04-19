package ee.kool.saaremeister.utility;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import message.dto.Direction;

/**
 * Utility class for creating different types of sprites.
 */
public class Sprites {
    public static final Texture playerTexture = new Texture("sprites/player.png");
    public static final Texture bulletTexture = new Texture("sprites/bullet.png");
    public static final Texture rocketTexture = new Texture("sprites/rocket.png");
    public static final Texture rocketDownTexture = new Texture("sprites/rocket_down.png");

    // Split player.png into 4x4 frames
    private static final TextureRegion[][] playerGrid = TextureRegion.split(playerTexture, 16, 16);

    // Row mapping for player.png (64x64 = 4x4 frames of 16x16)
    public static final Animation<TextureRegion> moveDown = new Animation<>(0.2f, playerGrid[1]);
    public static final Animation<TextureRegion> moveUp = new Animation<>(0.2f, playerGrid[0]);
    public static final Animation<TextureRegion> moveRight = new Animation<>(0.2f, playerGrid[2]);
    public static final Animation<TextureRegion> moveLeft = new Animation<>(0.2f, playerGrid[3]);

    /**
     * Returns the animation for the player based on the direction.
     */
    public static Animation<TextureRegion> getPlayerAnimation(Direction direction) {
        return switch (direction) {
            case DOWN -> moveDown;
            case UP -> moveUp;
            case RIGHT -> moveRight;
            case LEFT -> moveLeft;
        };
    }

    /**
     * Returns the idle frame of the player based on the direction.
     */
    public static TextureRegion getPlayerIdleFrame(Direction direction) {
        int row = switch (direction) {
            case DOWN -> 1;
            case UP -> 0;
            case RIGHT -> 2;
            case LEFT -> 3;
        };

        // Get the first frame of the row
        return playerGrid[row][0];
    }
}
