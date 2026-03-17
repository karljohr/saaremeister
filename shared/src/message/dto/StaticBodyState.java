package message.dto;

import com.badlogic.gdx.math.Vector2;

public class StaticBodyState {
    public Vector2 worldPosition;
    public float[] vertices;

    public StaticBodyState() {}

    public StaticBodyState(Vector2 worldPosition, float[] vertices) {
        this.worldPosition = worldPosition;
        this.vertices = vertices;
    }
}
