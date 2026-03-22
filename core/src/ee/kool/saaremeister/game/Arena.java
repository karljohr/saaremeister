package ee.kool.saaremeister.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;
import message.GameStateMessage;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

/**
 * Render the current arena of the game, based on the info from the server.
 */
public class Arena {

    @Getter
    private final List<Player> players = new ArrayList<>();
    private List<BulletState> bullets = new ArrayList<>();
    private List<CaptureZoneState> captureZones = new ArrayList<>();

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

    /**
     * Render map, players and bullets. This makes them visible on the screen.
     *
     * @param spriteBatch used for rendering (and scaling/resizing) all visual elements
     */
    public void render(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        renderCaptureZones(shapeRenderer);
        renderPlayers(spriteBatch);
        // TODO: renderBullets(spriteBatch);
    }

    // TODO: private void renderMap()

    public void renderPlayers(SpriteBatch spriteBatch) {
        players.forEach(player -> player.render(spriteBatch));
    }

    public void renderCaptureZones(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (CaptureZoneState captureZone : captureZones) {
            shapeRenderer.setColor(getZoneColor(captureZone));
            shapeRenderer.rect(captureZone.getX(), captureZone.getY(), captureZone.getWidth(), captureZone.getHeight());
        }
        shapeRenderer.end();
    }

    private Color getZoneColor(CaptureZoneState captureZoneState) {
        if (captureZoneState.isContested()) {
            return new Color(1f, 1f, 0f, 0.35f);
        }
        if (captureZoneState.getOwnerPlayerId() == -1) {
            return new Color(0.6f, 0.6f, 0.6f, 0.25f);
        }
        if (captureZoneState.getOwnerPlayerId() == 1) {
            return new Color(1f, 0f, 0f, 0.30f);
        }
        if (captureZoneState.getOwnerPlayerId() == 2) {
            return new Color(0f, 0.4f, 1f, 0.30f);
        }
        return new Color(0f, 1f, 0f, 0.30f);
    }


    // TODO: private void renderBullets()
}
