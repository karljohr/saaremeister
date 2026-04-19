package ee.kool.saaremeister.server.game.object.projectiles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import message.dto.Direction;
import message.dto.RocketState;

public class Rocket extends Bullet{

    private final float startX;
    private final float startY;
    private final float targetX;
    private final float targetY;
    private final int flightTicks;
    private int ticksAlive;
    private float height;

    @Getter
    private final static float BLAST_RADIUS = 0.5f;
    private final static int DAMAGE = 2;
    private static final float ARC_HEIGHT = 120f;

    public Rocket(float x, float y, Vector2 target,  int shotById) {
        super(x, y , (Direction) null, shotById);
        targetX = target.x;
        targetY = target.y;
        startX = x;
        startY = y;
        ticksAlive = 0;
        height = 0;

        float distance = new Vector2(targetX - startX, targetY - startY).len();
        flightTicks = Math.clamp((int) (distance), 100, 200);
    }

    /**
     * Moves the rocket in the direction of the target.
     */
    @Override
    public void update() {
        float t = Math.min(1f, ticksAlive / (float) flightTicks);
        x = MathUtils.lerp(startX, targetX, t);
        y = MathUtils.lerp(startY, targetY, t);
        height = ARC_HEIGHT * 4f * t * (1f - t);
        ticksAlive++;

    }

    /**
     * Returns true if the rocket has reached the ground.
     * @return true if the rocket has touched ground.
     */
    public boolean hasLanded() { return ticksAlive >= flightTicks; }

    private boolean isFalling() { return (ticksAlive / (float) flightTicks) > 0.5; }

    public void explode() {

    }


    public RocketState getRocketState() {
        RocketState rocketState = new RocketState();
        rocketState.setX(x);
        rocketState.setY(y);
        rocketState.setHeight(height);
        rocketState.setFalling(isFalling());
        rocketState.setHasLanded(hasLanded());
        return rocketState;
    }

    public int getDamage() { return DAMAGE; }

}
