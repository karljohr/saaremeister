package ee.kool.saaremeister.server.game.object;

import lombok.Getter;
import message.dto.CaptureZoneState;

import java.util.List;

import static constant.Constants.CAPTURE_SECONDS_REQUIRED;
import static constant.Constants.NEUTRAL_OWNER_ID;

/**
 * Represents a capture zone that a player must capture to get closer to winning the game. The player must capture 4
 * zones to win the game, each zone must be stood alone in for 5 seconds to capture the zone. Once the zone is captured,
 * other players can retake the zone for themselves. When multiple people are standing in a zone, nothing happens.
 */
@Getter
public class CaptureZone {

    private final int zoneId;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    // Mutable game state.
    private int ownerPlayerId = NEUTRAL_OWNER_ID;
    private int capturingPlayerId = NEUTRAL_OWNER_ID;
    private float captureProgressSeconds = 0f;
    private boolean contested = false;

    public CaptureZone(int zoneId, float x, float y, float width, float height) {
        this.zoneId = zoneId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns true if the player is standing in the capture zone.
     */
    public boolean contains(Player player) {
        float playerX = player.getX();
        float playerY = player.getY();

        return playerX >= x && playerX <= x + width
                && playerY >= y && playerY <= y + height;
    }

    /**
     * Updates the capture zone's state based on the players in the zone.
     */
    public void update(List<Player> allPlayers, float deltaTime) {
        List<Player> playersInZone = allPlayers.stream().filter(this::contains).toList();

        // Sets the zone as contested if there are multiple players in it.
        contested = playersInZone.size() > 1;

        // Pauses capture progress when no player is inside the zone.
        if (playersInZone.isEmpty()) {
            return;
        }

        // Pauses capture progress when the zone is contested.
        if (contested) {
            return;
        }

        Player capturingPlayer = playersInZone.getFirst();
        int playerId = capturingPlayer.getId();

        // Doesn't advance capture progress if the player already owns the zone.
        if (ownerPlayerId == playerId) {
            capturingPlayerId = NEUTRAL_OWNER_ID;
            captureProgressSeconds = 0f;
            contested = false;
            return;
        }

        // Resets the capture progress if a different player started capturing the zone.
        if (capturingPlayerId != playerId) {
            capturingPlayerId = playerId;
            captureProgressSeconds = 0f;
        }

        captureProgressSeconds += deltaTime;

        // If one player has reached the capture time, then they are the new owner of the zone.
        if (captureProgressSeconds >= CAPTURE_SECONDS_REQUIRED) {
            ownerPlayerId = playerId;
            capturingPlayerId = NEUTRAL_OWNER_ID;
            captureProgressSeconds = 0f;
            contested = false;
        }
    }

    public boolean isCaptured() {
        return ownerPlayerId != NEUTRAL_OWNER_ID;
    }

    /**
     * Clears the capture progress of the player.
     */
    public void clearPlayer(int playerId) {
        if (ownerPlayerId == playerId) {
            ownerPlayerId = NEUTRAL_OWNER_ID;
        }

        if (capturingPlayerId == playerId) {
            capturingPlayerId = NEUTRAL_OWNER_ID;
            captureProgressSeconds = 0f;
            contested = false;
        }
    }

    /**
     * Converts the capture zone to a state message that can be sent to the client.
     */
    public CaptureZoneState toState() {
        CaptureZoneState state = new CaptureZoneState();
        state.setZoneId(zoneId);
        state.setX(x);
        state.setY(y);
        state.setWidth(width);
        state.setHeight(height);
        state.setOwnerPlayerId(ownerPlayerId);
        state.setCapturingPlayerId(capturingPlayerId);
        state.setCaptureProgressSeconds(captureProgressSeconds);
        state.setContested(contested);
        return state;
    }

}
