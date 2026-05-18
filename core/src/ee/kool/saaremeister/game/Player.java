package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import common.HasHealth;
import ee.kool.saaremeister.utility.Sprites;
import lombok.Getter;
import lombok.Setter;
import message.dto.Direction;
import message.dto.PlayerRole;

import static constant.Constants.PLAYER_PIXEL_HEIGHT;
import static constant.Constants.PLAYER_PIXEL_WIDTH;

/**
 * Client side representation of a player.
 */
public class Player implements HasHealth {

    private final int id;
    @Setter
    @Getter
    private float x;
    @Setter
    @Getter
    private float y;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private int health;
    @Setter
    @Getter
    private int maxHealth;
    @Getter
    private boolean alive = true;
    @Getter
    private int respawnRemainingMs = 0;

    private float stateTime = 0f;
    private boolean moving = false;
    private Direction facing = Direction.DOWN;
    private PlayerRole role;
    private boolean isBot;

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
     * Applies the server state to the player (position, current movement, and facing).
     */
    public void applyServerState(float nx, float ny, PlayerRole role, boolean isBot, int maxHealth, int health,
                                 boolean alive, int respawnRemainingMs) {
        this.role = role;
        this.isBot = isBot;
        this.alive = alive;
        this.respawnRemainingMs = Math.max(0, respawnRemainingMs);

        float dx = nx - x;
        float dy = ny - y;

        float eps = 0.1f;
        boolean movedX = Math.abs(dx) > eps;
        boolean movedY = Math.abs(dy) > eps;
        moving = alive && (movedX || movedY);

        if (moving) {
            if (movedY) {
                // Diagonal or vertical: always use vertical animation
                facing = dy > 0 ? Direction.DOWN : Direction.UP;
            } else if (movedX) {
                // Horizontal only
                facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
            }
        }

        x = nx;
        y = ny;
        this.maxHealth = maxHealth;
        this.health = health;
    }

    /**
     * Renders the player on the screen. Picks the correct animation frame based on the player's state.
     */
    public void render(SpriteBatch spriteBatch) {
        // x/y are in world units (pixels/PPM). Draw sprite centered with size also in world units.
        float w = PLAYER_PIXEL_WIDTH;
        float h = PLAYER_PIXEL_HEIGHT;

        if (moving) {
            stateTime += Gdx.graphics.getDeltaTime();
        } else {
            stateTime = 0f;
        }

        TextureRegion frame = !alive
                ? Sprites.getPlayerDeadFrame(role, facing, isBot)
                : (moving
                   ? Sprites.getPlayerAnimation(role, facing, isBot).getKeyFrame(stateTime, true)
                   : Sprites.getPlayerIdleFrame(role, facing, isBot));

        spriteBatch.draw(frame, x - w / 2f, y - h / 2f, w, h);
    }


    @Override
    public float getWorldX() {
        return x;
    }

    @Override
    public float getWorldY() {
        return y;
    }
}
