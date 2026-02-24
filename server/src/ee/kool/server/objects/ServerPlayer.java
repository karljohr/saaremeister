package ee.kool.server.objects;

import com.badlogic.gdx.math.Rectangle;
import messages.DTO.Input;

public class ServerPlayer {
    // Default values
    public float x = 0;
    public float y = 0;
    public float speed = 100;
    private final float size = 20f;

    private final Rectangle goal = new Rectangle(500, 300, 100, 50);
    public boolean won = false;

    // Movement through input
    public void applyInput(Input input, float delta) {
        if (won) return;

        float dx = 0f;
        float dy = 0f;

        if (input.left) dx -= 1f;
        if (input.right) dx += 1f;
        if (input.up) dy += 1f;
        if (input.down) dy -= 1f;

        x += dx * speed * delta;
        y += dy * speed * delta;

        Rectangle playerRect = new Rectangle(x, y, size, size);
        if (goal.overlaps(playerRect)) {
            this.won = true;
            return;
        }

        // Screen borders.
        if (x < 0) x = 0;
        if (x > 600 - size) x = 600 - size;
        if (y < 0) y = 0;
        if (y > 400 - size) y = 400 - size;
    }
}