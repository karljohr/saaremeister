package ee.kool.saaremeister.server.game.object;

import com.badlogic.gdx.math.Intersector;
import ee.kool.saaremeister.server.game.object.player.Player;
import lombok.Getter;
import lombok.Setter;
import message.dto.CaptureZoneState;
import message.dto.PlayerRole;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static constant.Constants.CAPTURE_SECONDS_REQUIRED;

/**
 * Represents a capture zone that a class must capture to get closer to winning the game. The class must capture 4
 * zones to win the game, each zone must be stood alone in for 5 seconds to capture the zone. Once the zone is captured,
 * other classes can retake the zone for themselves. When multiple classes are standing in a zone, no progress is made.
 */
@Getter
public class CaptureZone {

    private final int zoneId;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float[] polygonVertices;

    // Mutable game state.
    @Setter
    private ZoneOwner ownerEnum = ZoneOwner.MASTER;
    private ZoneOwner capturingEnum = ZoneOwner.NEUTRAL;
    private float captureProgressSeconds = 0f;
    private boolean contested = false;

    public enum ZoneOwner {
        MASTER, OFFICER, NEUTRAL
    }

    /**
     * Creates a new capture zone with the given polygon vertices.
     */
    public CaptureZone(int zoneId, float[] polygonVertices) {
        this.zoneId = zoneId;
        this.polygonVertices = polygonVertices.clone();

        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < polygonVertices.length; i += 2) {
            float vx = polygonVertices[i], vy = polygonVertices[i + 1];
            minX = Math.min(minX, vx);
            maxX = Math.max(maxX, vx);
            minY = Math.min(minY, vy);
            maxY = Math.max(maxY, vy);
        }
        this.x = minX;
        this.y = minY;
        this.width = maxX - minX;
        this.height = maxY - minY;
    }

    /**
     * Returns true if the player is standing in the capture zone.
     */
    public boolean contains(Player player) {
        return contains(player.getX(), player.getY());
    }

    /**
     * Returns true if the given world-pixel position is standing in the capture zone.
     */
    public boolean contains(float px, float py) {
        // fast reject using AABB
        if (px < x || px > x + width || py < y || py > y + height) return false;

        // exact isometric quad check
        return Intersector.isPointInPolygon(polygonVertices, 0, polygonVertices.length, px, py);
    }

    /**
     * Updates the capture zone's state based on the players in the zone.
     */
    public void update(List<Player> allPlayers, float deltaTime) {

        // Finds what classes of players are in the zone. Removes the enum from the set if there is not such a class in
        // the zone.
        Set<ZoneOwner> classesInZone = EnumSet.noneOf(ZoneOwner.class);
        for (Player player : allPlayers) {
            if (!player.isAlive()) continue;
            if (!this.contains(player)) continue;
            if (player.getRole() == PlayerRole.MASTER) {
                classesInZone.add(ZoneOwner.MASTER);
            } else if (player.getRole() == PlayerRole.OFFICER) {
                classesInZone.add(ZoneOwner.OFFICER);
            }
        }


        // Sets the zone as contested if there are multiple players in it.
        contested = classesInZone.size() > 1;

        // Resets the capture progress when no player is inside the zone.
        if (classesInZone.isEmpty()) {
            capturingEnum = ZoneOwner.NEUTRAL;
            captureProgressSeconds = 0f;
            contested = false;
            return;
        }

        // Pauses capture progress when the zone is contested.
        if (contested) {
            return;
        }

        capturingEnum = classesInZone.iterator().next();

        // Doesn't advance capture progress if the player already owns the zone.
        if (ownerEnum == capturingEnum) {
            capturingEnum = ZoneOwner.NEUTRAL;
            captureProgressSeconds = 0f;
            contested = false;
            return;
        }

        // Advances capture progress.
        captureProgressSeconds += deltaTime;

        // If one player has reached the capture time, then they are the new owner of the zone.
        if (captureProgressSeconds >= CAPTURE_SECONDS_REQUIRED) {
            ownerEnum = capturingEnum;
            capturingEnum = ZoneOwner.NEUTRAL;
            captureProgressSeconds = 0f;
            contested = false;
        }
    }

    /**
     * Clears ongoing capture state for a disconnecting player's class. Ownership is class-based, so we do not clear
     * ownerEnum here.
     */
    public void clearPlayer(PlayerRole role) {
        ZoneOwner leavingClass = switch (role) {
            case MASTER -> ZoneOwner.MASTER;
            case OFFICER -> ZoneOwner.OFFICER;
        };

        if (capturingEnum == leavingClass) {
            capturingEnum = ZoneOwner.NEUTRAL;
            captureProgressSeconds = 0f;
            contested = false;
        }
    }

    /**
     * Converts the zone owner to a player role, or null if the zone is neutral.
     */
    private PlayerRole toPlayerRoleOrNull(ZoneOwner zoneOwner) {
        return switch (zoneOwner) {
            case MASTER -> PlayerRole.MASTER;
            case OFFICER -> PlayerRole.OFFICER;
            case NEUTRAL -> null;
        };
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
        state.setOwnerRole(toPlayerRoleOrNull(ownerEnum));
        state.setCapturingRole(toPlayerRoleOrNull(capturingEnum));
        state.setCaptureProgressSeconds(captureProgressSeconds);
        state.setContested(contested);
        return state;
    }

}
