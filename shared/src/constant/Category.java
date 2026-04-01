package constant;

public enum Category {
    KOIK("Kõik"),
    RELVAD("Relvad"),
    EHITISED("Ehitised"),
    TARVIKUD("Tarvikud");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}