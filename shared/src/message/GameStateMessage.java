package message;

import lombok.Data;
import message.dto.*;

import java.util.List;

@Data
public class GameStateMessage {
    private List<PlayerState> playerStates;
    private List<BulletState> bulletStates;
    private List<CaptureZoneState> captureZones;
    private float gameTime;
    private boolean allPlayersHaveJoined;
    private List<BuildingState>  buildingStates;
    private boolean gameOver;
    private PlayerRole winnerPlayerRole;
}
