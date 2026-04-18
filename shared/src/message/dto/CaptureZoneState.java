package message.dto;

import lombok.Data;

@Data
public class CaptureZoneState {
    private int zoneId;
    private float x;
    private float y;
    private float width;
    private float height;
    private PlayerRole ownerRole;
    private PlayerRole capturingRole;
    private float captureProgressSeconds;
    private boolean contested;
}
