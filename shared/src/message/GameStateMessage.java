package message;

import lombok.Data;
import message.dto.BulletState;
import message.dto.PlayerState;

import java.util.List;

@Data
public class GameStateMessage {
    private List<PlayerState> playerStates;
    private List<BulletState> bulletStates;
    private int gameTime;
    private boolean allPlayersHaveJoined;
}
