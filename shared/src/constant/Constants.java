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
    public static final int MATCH_DURATION_SECONDS = 160;
    public static final int MAX_SLOTS = 15;

    public static final float ZONE_PROFIT_INTERVAL = 10f;

    // Player
    public static final float PLAYER_SPEED = 250.0f;
    public static final int PLAYER_PIXEL_HEIGHT = 32;
    public static final int PLAYER_PIXEL_WIDTH = 32;

    // Island master
    public static final int MASTER_LIVES = 100;
    public static final int MASTER_MONEY = 700;
    public static final int MASTER_KILL_PROFIT = 100;
    public static final int MASTER_ZONE_PROFIT = 50;

    // Tax officer
    public static final int OFFICER_LIVES = 50;
    public static final int OFFICER_MONEY = 500;
    public static final int OFFICER_KILL_PROFIT = 100;
    public static final int OFFICER_DESTROY_PROFIT = 50;
    public static final int OFFICER_ZONE_PROFIT = 50;

    // Bot
    public static final int BOT_LIVES = 50;
    public static final float BOT_SPEED = 150f;
    public static final float BOT_ENGAGEMENT_RANGE = 5.5f;

    // Bullet
    public static final float BULLET_SPEED = 12f;
    public static final long BULLET_TIMEOUT_MILLISECONDS = 400;
    public static final float BULLET_WIDTH_IN_PIXELS = 8;
    public static final float BULLET_HEIGHT_IN_PIXELS = 8;
    public static final float BULLET_DAMAGE = 10;

    // Consumables
    public static final int HEALTH_POTION_HEAL = 20;
    public static final float SPEED_POTION_MULTIPLIER = 1.5f;
    public static final float STRENGTH_POTION_MULTIPLIER = 1.5f;
    public static final long SPEED_POTION_DURATION_MILLISECONDS = 10_000L;
    public static final long STRENGTH_POTION_DURATION_MILLISECONDS = 10_000L;

    // Rocket
    public static final float ROCKET_WIDTH_IN_PIXELS = 16;
    public static final float ROCKET_HEIGHT_IN_PIXELS = 16;

    // Lobby
    public static final int LOBBY_MAX_PLAYERS = 2;
    public static final int PLAYER_COUNT_INGAME_LIMIT = 2;

    // Capture Zone
    public static final int CAPTURE_SECONDS_REQUIRED = 5;

    // World
    public static final float WATER_SLOW_MULTIPLIER = 0.5f;
    public static final String GROUND_LAYER_NAME = "ground";
    public static final String IS_WATER_PROPERTY = "isWater";
}
