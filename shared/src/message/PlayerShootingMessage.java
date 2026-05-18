package message;

import com.badlogic.gdx.math.Vector2;
import lombok.Data;

@Data
public class PlayerShootingMessage {
    private Vector2 aim;
    private int selectedSlot;
}
