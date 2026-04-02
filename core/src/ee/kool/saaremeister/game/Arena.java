package ee.kool.saaremeister.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.utility.Sprites;
import lombok.Getter;
import message.GameStateMessage;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.*;

/**
 * Render the current arena of the game, based on the info from the server.
 */
public class Arena {

    @Getter
    private final List<Player> players = new ArrayList<>();
    private List<BulletState> bullets = new ArrayList<>();
    private List<CaptureZoneState> captureZones = new ArrayList<>();
    private final int myId = ServerConnection.getInstance().getClient().getID();

    /**
     * Update the game state in the client based on the server's messages.
     */
    public void update(GameStateMessage gameStateMessage) {
        var currentPlayerIds = gameStateMessage.getPlayerStates()
                .stream()
                .map(PlayerState::getId)
                .toList();

        players.removeIf(player -> !currentPlayerIds.contains(player.getId()));

        gameStateMessage.getPlayerStates().forEach(playerState -> {
            var player = players.stream()
                    .filter(p -> p.getId().equals(playerState.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        var newPlayer = new Player(playerState.getId(), playerState.getUsername());
                        players.add(newPlayer);
                        return newPlayer;
                    });
            player.setX(playerState.getX());
            player.setY(playerState.getY());
        });
        this.bullets = gameStateMessage.getBulletStates();
        this.captureZones = gameStateMessage.getCaptureZones() != null
                ? gameStateMessage.getCaptureZones()
                : new ArrayList<>();
    }

    // TODO: private void renderMap()

    /**
     * Render all players.
     */
    public void renderPlayers(SpriteBatch spriteBatch) {
        players.forEach(player -> player.render(spriteBatch));
    }

    /**
     * Render all capture zones.
     */
    public void renderCaptureZones(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (CaptureZoneState captureZone : captureZones) {
            shapeRenderer.setColor(getZoneColor(captureZone));
            shapeRenderer.rect(captureZone.getX(), captureZone.getY(), captureZone.getWidth(), captureZone.getHeight());
        }
        shapeRenderer.end();
    }

    /**
     * Render all bullets.
     */
    public void renderBullets(SpriteBatch spriteBatch) {
        bullets.forEach(bullet -> {
            spriteBatch.draw(
                    Sprites.bulletTexture,
                    bullet.getX(),
                    bullet.getY(),
                    BULLET_WIDTH_IN_PIXELS,
                    BULLET_HEIGHT_IN_PIXELS
            );
        });
    }

    /**
     * Returns the color of the capture zone based on the capture zone owner.
     * Neutral zones are gray, owned zones are green, captured zones are red.
     */
    private Color getZoneColor(CaptureZoneState captureZoneState) {
        if (captureZoneState.isContested()) {
            return new Color(1f, 1f, 0f, 0.35f);
        }
        if (captureZoneState.getOwnerPlayerId() == NEUTRAL_OWNER_ID) {
            return new Color(0.6f, 0.6f, 0.6f, 0.25f);
        }
        if (captureZoneState.getOwnerPlayerId() == myId) {
            return new Color(0f, 1f, 0f, 0.30f);
        }
        return new Color(1f, 0f, 0f, 0.30f);
    }
}
