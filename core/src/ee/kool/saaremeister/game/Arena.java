package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.utility.ExplosionEffect;
import ee.kool.saaremeister.utility.Sprites;
import lombok.Getter;
import message.GameStateMessage;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.ExplosionState;
import message.dto.PlayerRole;
import message.dto.PlayerState;
import message.dto.RocketState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.*;

/**
 * Render the current arena of the game, based on the info from the server.
 */
public class Arena {

    @Getter
    private final List<Player> players = new ArrayList<>();
    @Getter
    private List<BulletState> bullets = new ArrayList<>();
    @Getter
    private List<RocketState> rockets = new ArrayList<>();
    private List<CaptureZoneState> captureZones = new ArrayList<>();
    private final int myId = ServerConnection.getInstance().getClient().getID();
    private final List<ExplosionEffect> explosions = new ArrayList<>();
    private PlayerRole myRole = null;

    public enum ZoneVisualState {
        CONTESTED,
        MINE,
        ENEMY,
        NEUTRAL
    }

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
                        var newPlayer = new Player(playerState.getId());
                        players.add(newPlayer);
                        return newPlayer;
                    });
            // Update username, necessary for differentiating bot from player
            player.setUsername(playerState.getUsername());

            player.applyServerState(
                    playerState.getX(),
                    playerState.getY(),
                    playerState.getRole(),
                    playerState.isBot()
            );
        });

        if (myRole == null) {
            myRole = gameStateMessage.getPlayerStates().stream()
                    .filter(p -> p.getId() == myId)
                    .map(PlayerState::getRole)
                    .findFirst()
                    .orElse(null);
        }


        var explosionStates = gameStateMessage.getExplosionStates();
        if (explosionStates != null) {
            for (ExplosionState explosionState : explosionStates) {
                explosions.add(new ExplosionEffect(explosionState.getX(), explosionState.getY()));
            }
        }

        this.bullets = gameStateMessage.getBulletStates() != null
                ? gameStateMessage.getBulletStates()
                : new ArrayList<>();
        this.rockets = gameStateMessage.getRocketStates() != null
                ? gameStateMessage.getRocketStates()
                : new ArrayList<>();
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
     * Render all projectiles.
     */
    public void renderProjectiles(SpriteBatch spriteBatch) {
        bullets.forEach(bullet -> {
            spriteBatch.draw(
                    Sprites.bulletTexture,
                    bullet.getX(),
                    bullet.getY(),
                    BULLET_WIDTH_IN_PIXELS,
                    BULLET_HEIGHT_IN_PIXELS
            );
        });
        if (rockets != null) {
            rockets.forEach(rocket -> {
                spriteBatch.draw(
                        rocket.isFalling() ? Sprites.rocketDownTexture : Sprites.rocketTexture,
                        rocket.getX(),
                        rocket.getY() + rocket.getHeight(),
                        ROCKET_WIDTH_IN_PIXELS,
                        ROCKET_HEIGHT_IN_PIXELS
                );
            });
        }
    }

    /**
     * Returns the visual state of a capture zone based on the capture zone owner.
     */
    public ZoneVisualState getZoneVisualState(int zoneId) {
        CaptureZoneState zoneState = captureZones.stream()
                .filter(z -> z.getZoneId() == zoneId)
                .findFirst()
                .orElse(null);

        if (zoneState == null || zoneState.getOwnerRole() == null) return ZoneVisualState.NEUTRAL;
        if (zoneState.isContested()) return ZoneVisualState.CONTESTED;
        if (zoneState.getOwnerRole() == myRole) return ZoneVisualState.MINE;
        return ZoneVisualState.ENEMY;
    }

    public void renderExplosions(ShapeRenderer shapeRenderer) {
        float delta = Gdx.graphics.getDeltaTime();
        explosions.removeIf(explosion -> {
            explosion.setTimeAlive(explosion.getTimeAlive() + delta);
            return explosion.getTimeAlive() >= 0.6f;
        });

        for (ExplosionEffect explosion : explosions) {
            float t = explosion.getTimeAlive() / 0.6f;
            float radius = 8f + 28f * t;

            shapeRenderer.setColor(1f, 0.9f, 0.4f, 1f - t);
            shapeRenderer.circle(explosion.getX(), explosion.getY(), radius);

            shapeRenderer.setColor(1f, 0.45f, 0.1f, 0.8f * (1f - t));
            shapeRenderer.circle(explosion.getX(), explosion.getY(), radius * 0.65f);
        }
    }
}
