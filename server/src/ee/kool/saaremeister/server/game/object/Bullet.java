package ee.kool.saaremeister.server.game.object;

import lombok.Getter;
import lombok.Setter;
import message.dto.BulletState;
import message.dto.Direction;

import static constant.Constants.BULLET_SPEED;

@Getter
@Setter
public class Bullet {
    private final Direction direction;
    private float x;
    private float y;
    private int shotById;

    public Bullet(float x, float y, Direction direction, int shotById) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.shotById = shotById;
    }

    public void update() {
        switch (direction) {
            case UP -> y += BULLET_SPEED;
            case DOWN -> y -= BULLET_SPEED;
            case LEFT -> x -= BULLET_SPEED;
            case RIGHT -> x += BULLET_SPEED;
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
