package message;

import lombok.Data;
import message.dto.Direction;

@Data
public class PlayerMovementMessage {
    private Direction direction;
}
