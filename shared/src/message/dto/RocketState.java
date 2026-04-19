package message.dto;

import lombok.Data;

@Data
public class RocketState {
    private float x;
    private float y;
    private float height;
    private boolean isFalling;
    private boolean hasLanded;
}
