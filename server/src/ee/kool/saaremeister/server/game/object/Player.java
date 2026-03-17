package ee.kool.saaremeister.server.game.object;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.listener.PlayerMovementListener;
import ee.kool.saaremeister.server.listener.PlayerShootingListener;
import lombok.Getter;
import lombok.Setter;

import message.dto.Direction;
import message.dto.PlayerState;

import static constant.Constants.*;

/**
 * The server-side representation of a player. Listens client input
 * and updates the players state accordingly.
 */
@Getter
@Setter
public class Player {

    private final Connection connection;
    private final PlayerMovementListener movementListener = new PlayerMovementListener(this);
    private final PlayerShootingListener shootingListener = new PlayerShootingListener(this);

    private final int id;
    private final GameInstance gameInstance;
    private float x, y = 0f;
    private int lives = PLAYER_LIVES;
    @Getter @Setter
    private String username = "Player";

    /**
     * Initializes a new server/side representation of a player.
     */
    public Player(Connection connection, GameInstance gameInstance) {
        this.connection = connection;
        this.id = connection.getID();
        this.gameInstance = gameInstance;
        this.connection.addListener(movementListener);
        this.connection.addListener(shootingListener);
    }

    /**
     * Moves the player in the given direction.
     */
    public void move(Direction directionX, Direction directionY) {
        if (directionX == null && directionY == null) {
            return;
        }


        switch (directionX) {
            case LEFT -> body.setLinearVelocity(-PLAYER_SPEED / PPM, body.getLinearVelocity().y);
            case RIGHT ->  body.setLinearVelocity(PLAYER_SPEED / PPM, body.getLinearVelocity().y);
            case null, default -> {}
        }
        switch (directionY) {
            case UP -> body.setLinearVelocity(body.getLinearVelocity().x, PLAYER_SPEED / PPM);
            case DOWN -> body.setLinearVelocity(body.getLinearVelocity().x, -PLAYER_SPEED / PPM);
            case null, default -> {}
        }
    }

    /**
     * Returns the player's state.
     */
    public PlayerState getPlayerState() {
        PlayerState playerState = new PlayerState();
        playerState.setId(connection.getID());
        playerState.setX(x);
        playerState.setY(y);
        playerState.setLives(lives);
        playerState.setUsername(username);
        return playerState;
    }

    /**
     * Removes the player's listeners from the connection (when the game ends).
     */
    public void dispose() {
        connection.removeListener(movementListener);
        connection.removeListener(shootingListener);
    }
}
