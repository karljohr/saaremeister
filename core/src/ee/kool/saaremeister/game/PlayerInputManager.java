package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import ee.kool.saaremeister.component.Hotbar;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.utility.SoundEffectManager;
import message.ItemUsedMessage;
import message.PlayerMovementMessage;
import message.PlayerShootingMessage;
import message.dto.Direction;

import static constant.Constants.BULLET_TIMEOUT_MILLISECONDS;

/**
 * Listens for the user input and forwards update messages to the server.
 */
public class PlayerInputManager {

    private Direction facing = Direction.DOWN;
    private long nextShotAtNs = 0L;
    private static final long SHOOT_INTERVAL_NS = BULLET_TIMEOUT_MILLISECONDS * 1_000_000L;
    private static final long MOVEMENT_TCP_FALLBACK_INTERVAL_NS = 100_000_000L;     // 100ms
    private Direction lastSentDirX = null;
    private Direction lastSentDirY = null;
    private long nextMovementTcpAtNs = 0L;

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

        var client = ServerConnection.getInstance().getClient();
        client.sendUDP(movementMessage);    // fast path

        // Reliable fallback over TCP
        long now = System.nanoTime();
        Direction dirX = movementMessage.getDirectionX();
        Direction dirY = movementMessage.getDirectionY();
        boolean changed = dirX != lastSentDirX || dirY != lastSentDirY;
        boolean periodic = now > nextMovementTcpAtNs;
        if (changed || periodic) {
            client.sendTCP(movementMessage);
            lastSentDirX = dirX;
            lastSentDirY = dirY;
            nextMovementTcpAtNs = now + MOVEMENT_TCP_FALLBACK_INTERVAL_NS;
        }
    }

    /**
     * Stops the player from moving when something else is focused on.
     */
    public void stopMovement() {
        var movementMessage = new PlayerMovementMessage();
        var client = ServerConnection.getInstance().getClient();
        client.sendUDP(movementMessage);
        client.sendTCP(movementMessage);    // ensure stop is always delivered
        lastSentDirX = null;
        lastSentDirY = null;
        nextMovementTcpAtNs = 0L;
    }

    /**
     * Handles the player's shooting input.
     */
    public void handleShootingInput(float px, float py, float mx, float my, int selectedSlot) {
        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE)) return;

        // Delay for shooting when space is held down
        long now = System.nanoTime();
        if (now < nextShotAtNs) return;

        Vector2 aim = new Vector2(mx - px, my - py);
        if (aim.isZero(0.001f)) return;

        PlayerShootingMessage msg = new PlayerShootingMessage();
        msg.setAim(aim.nor());
        msg.setSelectedSlot(selectedSlot);
        ServerConnection.getInstance().getClient().sendTCP(msg);

        nextShotAtNs = now + SHOOT_INTERVAL_NS;
    }

    public void handleActionInput(Hotbar hotbar, float px, float py, float mx, float my) {
        String selected = hotbar.getSelectedItem();
        int selectedIndex = hotbar.getSelectedIndex();

        if (selectedIndex == 0 && "Püstol".equals(selected)) {
            handleShootingInput(px, py, mx, my, selectedIndex);
        } else if (selected != null && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (selectedIndex > 0) {
                SoundEffectManager.playDrink();
            }
            hotbar.clearSlot(selectedIndex);
            ItemUsedMessage msg = new ItemUsedMessage();
            msg.setSelectedSlot(selectedIndex);
            ServerConnection.getInstance().getClient().sendTCP(msg);
        }
    }

    /**
     * Stops sending shooting messages when something else is focused on.
     */
    public void stopShooting() {
        // Shooting is sent only on key press; there is no continuous fire state to clear.
    }
}
