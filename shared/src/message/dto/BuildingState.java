package message.dto;

import common.HasHealth;
import lombok.Data;

@Data
public class BuildingState implements HasHealth {
    public int id;
    public float x;
    public float y;
    public int worldX;
    public int worldY;
    public int uniqueId;
    public int health;
    public int maxHealth;

    public BuildingState(int id, float x, float y, int health, int maxHealth) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public BuildingState(int id, float x, float y) {
        this(id, x, y, 0, 0);
    }

    public BuildingState() {
        this(0, 0, 0, 0 ,0);
    }

    public int getTileId() {return id;}

    @Override
    public float getWorldX() {
        return worldX;
    }

    @Override
    public float getWorldY() {
        return worldY;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public Integer getId() {
        return uniqueId;
    }


}
