package ee.kool.server.objects;

import messages.DTO.Input;

public class ServerPlayer {
    // Default values
    public float x = 0;
    public float y = 0;
    public float speed = 100;

    // Movement through input
    public void applyInput(Input input, float delta) {
        float dx = 0f;
        float dy = 0f;
        if (input.left) dx -= speed * delta;
        if (input.right) dx += speed * delta;
        if (input.up) dy -= speed * delta;
        if (input.down) dy += speed * delta;
        x += dx;
        y += dy;
    }
}
