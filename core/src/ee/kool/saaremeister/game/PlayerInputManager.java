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
            movementMessage.setDirection(Direction.LEFT);
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
            movementMessage.setDirection(Direction.RIGHT);
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
            movementMessage.setDirection(Direction.UP);
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
            movementMessage.setDirection(Direction.DOWN);
        }

        if (movementMessage.getDirection() == null) return;

        ServerConnection
                .getInstance()
                .getClient()
                .sendUDP(movementMessage);
    }

    // public void handleShootingInput
}
