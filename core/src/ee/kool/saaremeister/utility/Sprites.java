package ee.kool.saaremeister.utility;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import message.dto.Direction;
import message.dto.PlayerRole;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for creating different types of sprites.
 */
public class Sprites {
    public static final Texture playerTexture = new Texture("sprites/player.png");
    public static final Texture masterTexture = new Texture("sprites/characters/master.png");
    public static final Texture officerTexture = new Texture("sprites/characters/officer.png");
    public static final Texture botTexture = new Texture("sprites/characters/bot.png");
    public static final Texture bulletTexture = new Texture("sprites/bullet.png");
    public static final Texture rocketTexture = new Texture("sprites/rocket.png");
    public static final Texture rocketDownTexture = new Texture("sprites/rocket_down.png");
    public static final Texture deadTexture = new Texture("sprites/characters/dead.png");

    private static final int FRAME_SIZE = 16;
    private static final float FRAME_DURATION = 0.1f;
    private static final int IDLE_COLUMN = 0;

    private static final int DEAD_ROW = 0;
    private static final int DEAD_MASTER_COL = 0;
    private static final int DEAD_OFFICER_COL = 1;
    private static final int DEAD_BOT_COL = 2;

    // Player grid for the default player texture.
    private static final TextureRegion[][] defaultGrid = getPlayerGrid(playerTexture);
    // Player grid for the bot texture.
    private static final TextureRegion[][] botGrid = getPlayerGrid(botTexture);
    // Player grid for the dead player textures.
    private static final TextureRegion[][] deadGrid = TextureRegion.split(deadTexture, FRAME_SIZE, FRAME_SIZE);;

    // Mapping of the player's direction to the row in the grid.
    private static final Map<Direction, Integer> PLAYER_ROW_MAP = Map.of(
            Direction.UP, 0,
            Direction.DOWN, 1,
            Direction.RIGHT, 2,
            Direction.LEFT, 3
    );

    // Animations for the default player texture.
    public static final EnumMap<Direction, Animation<TextureRegion>> defaultAnimations = buildDirectionalAnimations(
            playerTexture, FRAME_SIZE, FRAME_SIZE, FRAME_DURATION, PLAYER_ROW_MAP
    );

    // Animations for the bot texture.
    public static final EnumMap<Direction, Animation<TextureRegion>> botAnimations = buildDirectionalAnimations(
            botTexture, FRAME_SIZE, FRAME_SIZE, FRAME_DURATION, PLAYER_ROW_MAP
    );

    // Grids for the different player roles.
    private static final Map<PlayerRole, TextureRegion[][]> roleGrids = Map.of(
            PlayerRole.MASTER, getPlayerGrid(masterTexture),
            PlayerRole.OFFICER, getPlayerGrid(officerTexture)
    );

    // Animations for the different player roles.
    private static final Map<PlayerRole, EnumMap<Direction, Animation<TextureRegion>>> roleAnimations = Map.of(
            PlayerRole.MASTER, buildDirectionalAnimations(
                    masterTexture, FRAME_SIZE, FRAME_SIZE, FRAME_DURATION, PLAYER_ROW_MAP),
            PlayerRole.OFFICER, buildDirectionalAnimations(
                    officerTexture, FRAME_SIZE, FRAME_SIZE, FRAME_DURATION, PLAYER_ROW_MAP
            )
    );

    /**
     * Returns the player grid for the given player texture. Splits the texture into 4x4 frames.
     */
    public static TextureRegion[][] getPlayerGrid(Texture playerTexture) {
        return TextureRegion.split(playerTexture, 16, 16);
    }

    /**
     * Returns the row for the given direction.
     */
    private static int rowFor(Direction direction, Map<Direction, Integer> rowMap) {
        return rowMap.get(direction);
    }

    /**
     * Builds animations for the given texture.
     */
    public static EnumMap<Direction, Animation<TextureRegion>> buildDirectionalAnimations(
            Texture texture,
            int frameW,
            int frameH,
            float frameDuration,
            Map<Direction, Integer> rowMap
    ) {
        TextureRegion[][] grid = TextureRegion.split(texture, frameW, frameH);
        EnumMap<Direction, Animation<TextureRegion>> result = new EnumMap<>(Direction.class);

        for (Direction d : Direction.values()) {
            result.put(d, new Animation<>(frameDuration, grid[rowFor(d, rowMap)]));
        }

        return result;
    }

    /**
     * Returns the grid for the given player role.
     */
    private static TextureRegion[][] gridFor(PlayerRole role) {
        return roleGrids.getOrDefault(role, defaultGrid);
    }

    /**
     * Returns the animations for the given player role.
     */
    private static EnumMap<Direction, Animation<TextureRegion>> animationsFor(PlayerRole role) {
        return roleAnimations.getOrDefault(role, defaultAnimations);
    }

    /**
     * Returns the idle frame for the given direction.
     */
    public static TextureRegion getIdleFrame(
            TextureRegion[][] grid,
            Direction direction,
            Map<Direction, Integer> rowMap,
            int idleCol
    ) {
        return grid[rowFor(direction, rowMap)][idleCol];
    }

    /**
     * Returns the animation for the given player role and direction.
     */
    public static Animation<TextureRegion> getPlayerAnimation(PlayerRole role, Direction direction, boolean isBot) {
        if (isBot) return botAnimations.get(direction);
        return animationsFor(role).get(direction);
    }

    /**
     * Returns the idle frame for the given player role and direction.
     */
    public static TextureRegion getPlayerIdleFrame(PlayerRole role, Direction direction, boolean isBot) {
        if (isBot) return getIdleFrame(botGrid, direction, PLAYER_ROW_MAP, IDLE_COLUMN);
        return getIdleFrame(gridFor(role), direction, PLAYER_ROW_MAP, IDLE_COLUMN);
    }

    /**
     * Returns the dead frame for the given column.
     */
    private static TextureRegion deadAt(int col) {
        if (deadGrid.length <= DEAD_ROW || deadGrid[DEAD_ROW] == null || deadGrid[DEAD_ROW].length <= col) return null;
        return deadGrid[DEAD_ROW][col];
    }

    /**
     * Returns the dead frame for the given player role and direction.
     */
    public static TextureRegion getPlayerDeadFrame(PlayerRole role, Direction facing, boolean isBot) {
        TextureRegion region;
        if (isBot) {
            region = deadAt(DEAD_BOT_COL);
        } else if (role == PlayerRole.MASTER) {
            region = deadAt(DEAD_MASTER_COL);
        } else if (role == PlayerRole.OFFICER) {
            region = deadAt(DEAD_OFFICER_COL);
        } else {
            region = null;
        }
        return region != null ? region : getPlayerIdleFrame(role, facing, isBot);
    }
}
