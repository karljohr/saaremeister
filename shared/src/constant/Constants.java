package constant;

public class Constants {

    // Box2D
    public static final float PPM = 100f;

    // Networking
    public static final int TCP_PORT = 8080;
    public static final int UDP_PORT = 8081;
    public static final String SERVER_IP = "193.40.157.179";

    // Game
    public static final int GAME_TICK_RATE = 60;
    public static final int PLAYER_COUNT_INGAME_LIMIT = 10;

    // Arena size
    public static final int ARENA_LOWER_BOUND_X = 0;
    public static final int ARENA_LOWER_BOUND_Y = 0;
    public static final int ARENA_UPPER_BOUND_X = 1920;
    public static final int ARENA_UPPER_BOUND_Y = 1080;

    // Player
    public static final float PLAYER_SPEED = 5.0f;
    public static final int PLAYER_LIVES = 1;
    public static final int PLAYER_PIXEL_HEIGHT = 16;
    public static final int PLAYER_PIXEL_WIDTH = 16;

    // Bullet
    public static final float BULLET_SPEED = 10.0f;

    // Lobby
    public static final int LOBBY_MAX_PLAYERS = 10;
}
