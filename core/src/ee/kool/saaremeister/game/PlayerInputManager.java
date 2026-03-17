package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import ee.kool.saaremeister.network.ServerConnection;
import message.PlayerMovementMessage;
import message.dto.Direction;

/**
 * Listens for the user input and forwards update messages to the server.
 */
public class PlayerInputManager {

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

    // public void handleShootingInput
}
