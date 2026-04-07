package message.dto;

import lombok.Data;

@Data
public class BuildingState {
    public int id;
    public int x;
    public int y;

    public BuildingState() {
        id = 0;
        x = 0;
        y = 0;
    }

    public BuildingState(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
