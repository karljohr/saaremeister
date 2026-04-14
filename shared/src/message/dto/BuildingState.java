package message.dto;

import lombok.Data;

@Data
public class BuildingState {
    public int id;
    public int x;
    public int y;
    public int worldX;
    public int worldY;
    public int uniqueId;
    public int health;
    public int maxHealth;

    public BuildingState(int id, int x, int y, int health, int maxHealth) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public BuildingState(int id, int x, int y) {
        this(id, x, y, 0, 0);
    }

    public BuildingState() {
        this(0, 0, 0, 0 ,0);
    }


}
