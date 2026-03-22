package message;

import lombok.Data;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.PlayerState;

import java.util.List;

@Data
public class GameStateMessage {
    private List<PlayerState> playerStates;
    private List<BulletState> bulletStates;
    private List<CaptureZoneState> captureZones;
    private int gameTime;
    private int remainingTimeSeconds;
    private boolean allPlayersHaveJoined;
    private boolean gameOver;
    private boolean draw;
    private int winnerPlayerId;
    private String winningReason;
}
