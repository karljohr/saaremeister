package constant;

/**
 * Constants used throughout the game, to avoid hard-coded values.
 */
public class Constants {

    // Box2D
    public static final float PPM = 100f;
    public static final int TILE_WIDTH = 32;
    public static final int TILE_HEIGHT = 16;
    public static final int RENDER_OFFSETX = -4160;

    // Networking
    public static final int TCP_PORT = 8080;
    public static final int UDP_PORT = 8081;
    public static final String SERVER_IP = "localhost";     // TalTech serveri IP: 193.40.157.179

    // Game
    public static final int GAME_TICK_RATE = 60;
    public static final int PLAYER_COUNT_INGAME_LIMIT = 5;
    public static final int MATCH_DURATION_SECONDS = 120;
    public static final int MAX_SLOTS = 15;

    // Player
    public static final float PLAYER_SPEED = 500.0f;
    public static final int PLAYER_LIVES = 1;
    public static final int PLAYER_PIXEL_HEIGHT = 32;
    public static final int PLAYER_PIXEL_WIDTH = 32;
    public static final int PLAYER_MONEY = 500;

    // Bullet
    public static final float BULLET_SPEED = 5.0f;
    public static final long BULLET_TIMEOUT_MILLISECONDS = 100;
    public static final float BULLET_WIDTH_IN_PIXELS = 50;
    public static final float BULLET_HEIGHT_IN_PIXELS = 50;
    public static final float BULLET_DAMAGE = 1;

    // Lobby
    public static final int LOBBY_MAX_PLAYERS = 5;

    // Capture Zone
    public static final int CAPTURE_SECONDS_REQUIRED = 5;
    public static final int NEUTRAL_OWNER_ID = -1;

}
