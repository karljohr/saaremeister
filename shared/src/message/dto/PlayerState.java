package message.dto;

import lombok.Data;

@Data
public class PlayerState {
    private int id;
    private float x;
    private float y;
    private int lives;
    private int maxHealth;
    private int defense;
    private int speedPotionRemainingMs;
    private int strengthPotionRemainingMs;
    private String username;
    private int money;
    private PlayerRole role;
    private boolean isBot;

    /**
     * Resolves the armor tier from the current defense percentage.
     */
    public common.ArmorType getArmorType() {
        return common.ArmorType.fromDefense(this.defense);
    }
}
