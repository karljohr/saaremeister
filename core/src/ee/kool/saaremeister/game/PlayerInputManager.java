package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import ee.kool.saaremeister.component.Hotbar;
import ee.kool.saaremeister.network.ServerConnection;
import message.PlayerMovementMessage;
import message.PlayerShootingMessage;
import message.dto.Direction;

/**
 * Listens for the user input and forwards update messages to the server.
 */
public class PlayerInputManager {

    private Direction facing = Direction.DOWN;

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

        // Set facing direction for shooting
        if (movementMessage.getDirectionY() != null) {
            facing = movementMessage.getDirectionY();
        } else if (movementMessage.getDirectionX() != null) {
            facing = movementMessage.getDirectionX();
        }

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
    public void handleShootingInput(float px, float py, float mx, float my) {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) return;

        Vector2 aim = new Vector2(mx - px, my - py);
        if (aim.isZero(0.001f)) return;

        PlayerShootingMessage msg = new PlayerShootingMessage();
        msg.setAim(aim.nor());
        ServerConnection.getInstance().getClient().sendTCP(msg);
    }

    public void handleShootingInput() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) return;

        PlayerShootingMessage msg = new PlayerShootingMessage();
        ServerConnection.getInstance().getClient().sendTCP(msg);
    }

    public void handleActionInput(Hotbar hotbar) {
        String selected = hotbar.getSelectedItem();
        if (selected == null) return;
        if (hotbar.isGunSelected()) {
            handleShootingInput();
        }

        int selectedIndex = hotbar.getSelectedIndex();

        // Check if selected slot 0 and has gun
        if (selectedIndex == 0 && "Püstol".equals(selected)) {
            handleShootingInput();
        }
        // For potions left-click
        else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            System.out.println("Kasutati: " + selected);
            hotbar.clearSlot(hotbar.getSelectedIndex());
            ServerConnection.getInstance().getClient().sendTCP(new message.ItemUsedMessage());
        }

    }

    /**
     * Stops sending shooting messages when something else is focused on.
     */
    public void stopShooting() {
        var shootingMessage = new PlayerShootingMessage();
        ServerConnection.getInstance().getClient().sendTCP(shootingMessage);
    }
}
