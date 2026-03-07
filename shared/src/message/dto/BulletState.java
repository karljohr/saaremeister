package message.dto;

import lombok.Data;

@Data
public class BulletState {
    private float x;
    private float y;
    private Direction direction;
}
