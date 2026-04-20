package message.dto;

import lombok.Data;

@Data
public class PlayerState {
    private int id;
    private float x;
    private float y;
    private int lives;
    private int defense;
    private String username;
    private int money;
    private PlayerRole role;
    private boolean isBot;

    public common.ArmorType getArmorType() {
        return common.ArmorType.fromDefense(this.defense);
    }
}
