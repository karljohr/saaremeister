package ee.kool.saaremeister.server.game;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.game.object.CaptureZone;

import java.util.Set;
import java.util.List;

import static constant.Constants.MATCH_DURATION_SECONDS;

public class MatchRules {

    public enum MatchEndReason {
        NONE,
        ALL_ZONES_CAPTURED,
        TIME_LIMIT_REACHED_WITH_RESULT,
        TIME_LIMIT_REACHED_WITH_DRAW,
        NO_PLAYERS_CONNECTED
    }

    public MatchEndReason determineEndReason(
            List<CaptureZone> captureZones,
            Set<Connection> players,
            float gameTime) {
        if (areAllZonesCapturedByOnePlayer(captureZones)) {
            return MatchEndReason.ALL_ZONES_CAPTURED;
        }
        if (isGameTimeOver(gameTime)) {
            return MatchEndReason.TIME_LIMIT_REACHED_WITH_DRAW;
        }
        if (isLobbyEmpty(players)) {
            return MatchEndReason.NO_PLAYERS_CONNECTED;
        }
        return MatchEndReason.NONE;
    }

    /**
     * Checks if one player captures all capture zones.
     */
    private boolean areAllZonesCapturedByOnePlayer(List<CaptureZone> captureZones) {
        int check = 0;
        for (int i = 0; i < captureZones.size() - 1; i++) {
            if (captureZones.get(i).getOwnerPlayerId() == captureZones.get(i + 1).getOwnerPlayerId()
                    && captureZones.get(i).getOwnerPlayerId() != -1) {
                check++;
            }
        }
        return check == captureZones.size() - 1;
    }

    /**
     * Checks if the game time is over.
     */
    private boolean isGameTimeOver(float gameTime) {
        return gameTime >= MATCH_DURATION_SECONDS;
    }

    /**
     * Checks if the lobby is empty.
     */
    private boolean isLobbyEmpty(Set<Connection> players) {
        return players.isEmpty();
    }
}
