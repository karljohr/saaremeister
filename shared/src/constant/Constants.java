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
    public static final int TCP_PORT = 8082;
    public static final int UDP_PORT = 8083;
    public static final String SERVER_IP = "localhost";     // TalTech serveri IP: 193.40.157.179

    // Game
    public static final int GAME_TICK_RATE = 60;
    public static final int PLAYER_COUNT_INGAME_LIMIT = 5;
    public static final int MATCH_DURATION_SECONDS = 158;
    public static final int MAX_SLOTS = 15;

    public static final float ZONE_PROFIT_INTERVAL = 6f;

    // Player
    public static final float PLAYER_SPEED = 200.0f;
    public static final int PLAYER_PIXEL_HEIGHT = 32;
    public static final int PLAYER_PIXEL_WIDTH = 32;

    // Island master
    public static final int MASTER_LIVES = 10;
    public static final int MASTER_MONEY = 1000;
    public static final int MASTER_KILL_PROFIT = 200;
    public static final int MASTER_ZONE_PROFIT = 100;

    // Tax officer
    public static final int OFFICER_LIVES = 5;
    public static final int OFFICER_MONEY = 500;
    public static final int OFFICER_KILL_PROFIT = 200;
    public static final int OFFICER_DESTROY_PROFIT = 100;
    public static final int OFFICER_ZONE_PROFIT = 50;

    // Bot
    public static final float BOT_SPEED = 100f;
    public static final float BOT_ENGAGEMENT_RANGE = 7f;

    // Bullet
    public static final float BULLET_SPEED = 8.0f;
    public static final long BULLET_TIMEOUT_MILLISECONDS = 500;
    public static final float BULLET_WIDTH_IN_PIXELS = 8;
    public static final float BULLET_HEIGHT_IN_PIXELS = 8;
    public static final float BULLET_DAMAGE = 1;

    // Rocket
    public static final float ROCKET_WIDTH_IN_PIXELS = 16;
    public static final float ROCKET_HEIGHT_IN_PIXELS = 16;

    // Lobby
    public static final int LOBBY_MAX_PLAYERS = 5;

    // Capture Zone
    public static final int CAPTURE_SECONDS_REQUIRED = 5;
    public static final int NEUTRAL_OWNER_ID = -1;

}
