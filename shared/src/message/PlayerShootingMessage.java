package message;

import lombok.Data;
import message.dto.Direction;

@Data
public class PlayerShootingMessage {
    private Direction direction;
}
