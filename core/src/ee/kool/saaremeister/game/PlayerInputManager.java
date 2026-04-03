package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import ee.kool.saaremeister.network.ServerConnection;
import message.PlayerMovementMessage;
import message.PlayerShootingMessage;
import message.dto.Direction;

/**
 * Listens for the user input and forwards update messages to the server.
 */
public class PlayerInputManager {

    /**
     * Handles the player movement input.
     */
    public void handleMovementInput() {
        var movementMessage = new PlayerMovementMessage();

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) {
            movementMessage.setDirectionX(Direction.LEFT);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
            if (movementMessage.getDirectionX() == Direction.LEFT) {
                movementMessage.setDirectionX(null);
            } else {
                movementMessage.setDirectionX(Direction.RIGHT);
            }
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
            movementMessage.setDirectionY(Direction.UP);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
            if (movementMessage.getDirectionY() == Direction.UP) {
                movementMessage.setDirectionY(null);
            } else {
                movementMessage.setDirectionY(Direction.DOWN);
            }
        }

        if (movementMessage.getDirectionX() == null && movementMessage.getDirectionY() == null) return;

        ServerConnection
                .getInstance()
                .getClient()
                .sendUDP(movementMessage);
    }

    /**
     * Stops the player from moving when something else is focused on.
     */
    public void stopMovement() {
        var movementMessage = new PlayerMovementMessage();
        ServerConnection.getInstance().getClient().sendUDP(movementMessage);
    }

    /**
     * Handles the player's shooting input.
     */
    public void handleShootingInput() {
        var shootingMessage = new PlayerShootingMessage();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            shootingMessage.setDirection(Direction.LEFT);
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            shootingMessage.setDirection(Direction.RIGHT);
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            shootingMessage.setDirection(Direction.UP);
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            shootingMessage.setDirection(Direction.DOWN);
        }

        if (shootingMessage.getDirection() == null) return;

        ServerConnection.getInstance().getClient().sendTCP(shootingMessage);
    }

    /**
     * Stops sending shooting messages when something else is focused on.
     */
    public void stopShooting() {
        var shootingMessage = new PlayerShootingMessage();
        ServerConnection.getInstance().getClient().sendTCP(shootingMessage);
    }
}
