package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.utility.ExplosionEffect;
import ee.kool.saaremeister.utility.SoundEffectManager;
import lombok.Getter;
import message.GameStateMessage;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.ExplosionState;
import message.dto.PlayerRole;
import message.dto.PlayerState;
import message.dto.RocketState;

import java.util.*;

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
    private final Map<Integer, Boolean> previousAliveByPlayer = new HashMap<>();
    private boolean bulletsSeen = false;
    private int previousBulletCount = 0;
    private boolean rocketsSeen = false;
    private int previousRocketCount = 0;
    private boolean zonesSeen = false;
    private final Map<Integer, PlayerRole> previousZoneOwners = new HashMap<>();

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
                    playerState.isBot(),
                    playerState.getMaxHealth(),
                    playerState.getLives(),
                    playerState.isAlive(),
                    playerState.getRespawnRemainingMs()
            );

            // Update role if not already set
            if (myRole == null) {
                myRole = gameStateMessage.getPlayerStates().stream()
                        .filter(p -> p.getId() == myId)
                        .map(PlayerState::getRole)
                        .findFirst()
                        .orElse(null);
            }

            // Sound effects for spawning (only for the player themselves)
            Boolean wasAlive = previousAliveByPlayer.put(playerState.getId(), playerState.isAlive());
            if (wasAlive != null && !wasAlive && playerState.isAlive() && playerState.getId() == myId) {
                SoundEffectManager.playSpawn();
            }
        });

        // Sound effects for explosions
        var explosionStates = gameStateMessage.getExplosionStates();
        if (explosionStates != null) {
            for (ExplosionState explosionState : explosionStates) {
                explosions.add(new ExplosionEffect(explosionState.getX(), explosionState.getY()));
                SoundEffectManager.playRocketExplosion();
            }
        }

        // Sound effects for shooting
        this.bullets = gameStateMessage.getBulletStates() != null
                ? gameStateMessage.getBulletStates()
                : new ArrayList<>();
        if (!bulletsSeen) {
            previousBulletCount = this.bullets.size();
            bulletsSeen = true;
        } else {
            int newBullets = Math.max(0, this.bullets.size() - previousBulletCount);
            for (int i = 0; i < newBullets; i++) {
                SoundEffectManager.playBulletShot();
            }
            previousBulletCount = this.bullets.size();
        }

        // Sound effects for rocket launching
        this.rockets = gameStateMessage.getRocketStates() != null
                ? gameStateMessage.getRocketStates()
                : new ArrayList<>();
        if (!rocketsSeen) {
            previousRocketCount = this.rockets.size();
            rocketsSeen = true;
        } else {
            int newRockets = Math.max(0, this.rockets.size() - previousRocketCount);
            for (int i = 0; i < newRockets; i++) {
                SoundEffectManager.playRocketShot();
            }
            previousRocketCount = this.rockets.size();
        }

        // Sound effects for zone capturing
        this.captureZones = gameStateMessage.getCaptureZones() != null
                ? gameStateMessage.getCaptureZones()
                : new ArrayList<>();
        if (!zonesSeen) {
            for (CaptureZoneState zone : this.captureZones) {
                previousZoneOwners.put(zone.getZoneId(), zone.getOwnerRole());
            }
            zonesSeen = true;
        } else if (myRole != null) {
            Set<Integer> currentIds = new HashSet<>();
            for (CaptureZoneState zone : this.captureZones) {
                currentIds.add(zone.getZoneId());
                PlayerRole oldOwner = previousZoneOwners.get(zone.getZoneId());
                PlayerRole newOwner = zone.getOwnerRole();

                if (oldOwner != newOwner) {
                    if (newOwner == myRole) SoundEffectManager.playCapture();
                    else if (oldOwner == myRole) SoundEffectManager.playLose();
                }
                previousZoneOwners.put(zone.getZoneId(), newOwner);
            }
            previousZoneOwners.keySet().removeIf(id -> !currentIds.contains(id));
        }

        previousAliveByPlayer.keySet().removeIf(id -> !currentPlayerIds.contains(id));
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
