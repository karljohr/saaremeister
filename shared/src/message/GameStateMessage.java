package message;

import lombok.Data;
import message.dto.BuildingState;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.PlayerState;

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
    private int winnerPlayerId;
}
