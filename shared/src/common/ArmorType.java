package common;

import lombok.Getter;

@Getter
public enum ArmorType {
    NONE("Puudub", 0, 0),
    LEATHER("Nahk rüü", 1, 10),
    SILVER("Hõbe rüü", 2, 20),
    DIAMOND("Teemant rüü", 3, 40);

    private final String displayName;
    private final int level;
    private final int defenseValue;

    ArmorType(String displayName, int level, int defenseValue) {
        this.displayName = displayName;
        this.level = level;
        this.defenseValue = defenseValue;
    }

    // Finds out what armor the player is wearing
    public static ArmorType fromDefense(int defense) {
        if (defense >= 40) return DIAMOND;
        if (defense >= 20) return SILVER;
        if (defense >= 10) return LEATHER;
        return NONE;
    }

    // Finds by name
    public static ArmorType fromName(String name) {
        for (ArmorType type : values()) {
            if (name.equals(type.getDisplayName())) return type;
        }
        return NONE;
    }
}