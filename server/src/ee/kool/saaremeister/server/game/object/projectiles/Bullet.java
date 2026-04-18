package ee.kool.saaremeister.server.game.object.projectiles;

import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import lombok.Setter;
import message.dto.BulletState;
import message.dto.Direction;

import static constant.Constants.BULLET_SPEED;

/**
 * Represents a bullet shot by a player.
 */
@Getter
@Setter
public class Bullet {
    private final Direction direction;
    private final Vector2 aim;
    protected float x;
    protected float y;
    protected int shotById;

    /**
     * Bullets that are shot by a player.
     */
    public Bullet(float x, float y, Direction direction, int shotById) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.aim = null;
        this.shotById = shotById;
    }

    /**
     * Bullets that are shot by the AI.
     */
    public Bullet(float x, float y, Vector2 aim, int shotById) {
        this.x = x;
        this.y = y;
        this.aim = aim;
        this.direction = null;
        this.shotById = shotById;
    }

    /**
     * Moves the bullet in the direction it was shot.
     */
    public void update() {
        if (aim == null) {
            switch (direction) {
                case UP -> y += BULLET_SPEED;
                case DOWN -> y -= BULLET_SPEED;
                case LEFT -> x -= BULLET_SPEED;
                case RIGHT -> x += BULLET_SPEED;
                case null -> {
                }
            }
        } else {
            x += aim.x * BULLET_SPEED;
            y += aim.y * BULLET_SPEED;
        }
    }

    public BulletState getBulletState() {
        BulletState bulletState = new BulletState();
        bulletState.setX(x);
        bulletState.setY(y);
        bulletState.setDirection(direction);
        return bulletState;
    }
}
