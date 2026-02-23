package ee.kool.saaremeister.game;

public abstract class ClientPlayer {
    // Defaults for the general Player class.
    public static final int DEFAULT_X = 0;
    public static final int DEFAULT_Y = 0;
    public static final int DEFAULT_HEALTH = 100;

    private final String id;
    protected int x;
    protected int y;
    protected int health;

    // Default constructor using the default constants.
    public ClientPlayer(String id) {
        this(id, DEFAULT_X, DEFAULT_Y, DEFAULT_HEALTH);
    }

    // Full constructor for using custom values.
    public ClientPlayer(String id, int x, int y, int health) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.health = health;
    }

    // Getters and setters.
    public String getId() { return id; }

    public int getX() { return x; }
    public void setX() { this.x = x; }

    public int getY() { return y; }
    public void setY() { this.y = y; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public boolean isAlive() { return health > 0; }

    // Basic damage handling.
    public void takeDamage(int damage) {
        setHealth(this.health - damage);
    }
}
