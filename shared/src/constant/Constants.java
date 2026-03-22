package constant;

public class Constants {

    // Box2D
    public static final float PPM = 100f;
    public static final int TILE_WIDTH = 32;
    public static final int TILE_HEIGHT = 16;

    // Networking
    public static final int TCP_PORT = 8080;
    public static final int UDP_PORT = 8081;
    public static final String SERVER_IP = "localhost";

    // Game
    public static final int GAME_TICK_RATE = 60;
    public static final int PLAYER_COUNT_INGAME_LIMIT = 10;
    public static final int MATCH_DURATION_SECONDS = 120;

    // Player
    public static final float PLAYER_SPEED = 500.0f;
    public static final int PLAYER_LIVES = 1;
    public static final int PLAYER_PIXEL_HEIGHT = 32;
    public static final int PLAYER_PIXEL_WIDTH = 32;

    // Bullet
    public static final float BULLET_SPEED = 10.0f;

    // Lobby
    public static final int LOBBY_MAX_PLAYERS = 4;

    // Capture Zone
    public static final int CAPTURE_SECONDS_REQUIRED = 5;
    public static final int ZONE_COUNT = 4;
    public static final int NEUTRAL_OWNER_ID = -1;

}
