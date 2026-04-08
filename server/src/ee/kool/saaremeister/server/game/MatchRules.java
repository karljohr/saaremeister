package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.game.object.CaptureZone;
import ee.kool.saaremeister.server.game.object.Player;
import lombok.Getter;
import message.dto.PlayerRole;

import java.util.*;

import static constant.Constants.*;

/**
 * Winning, drawing, losing conditions, and respawning logic.
 */
public class MatchRules {

    @Getter
    private int gameWinnerId = -1;

    public enum MatchEndReason {
        NONE,
        ALL_ZONES_CAPTURED,
        TIME_LIMIT_REACHED_WITH_RESULT,
        TIME_LIMIT_REACHED_WITH_DRAW,
        NO_PLAYERS_CONNECTED
    }

    /**
     * Determines the reason why the game ended, which is used by GameInstance.
     */
    public MatchEndReason determineEndReason(
            List<CaptureZone> captureZones,
            Set<Connection> players,
            float gameTime) {

        if (areAllZonesCapturedByOnePlayer(captureZones)) {
            gameWinnerId = captureZones.getFirst().getOwnerPlayerId();
            return MatchEndReason.ALL_ZONES_CAPTURED;
        }

        if (isGameTimeOver(gameTime)) {
            // If one player has captured more zones than others, then they win.
            int drawResult = isDraw(captureZones);
            if (drawResult != -1) {
                gameWinnerId = drawResult;
                return MatchEndReason.TIME_LIMIT_REACHED_WITH_RESULT;
            }
            // Else, it's a complete draw and everyone loses.
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
     * Checks if the game ended in a complete draw or someone had more captured zones than others.
     * Returns the winner's ID if someone wins, -1 if it's a draw.
     */
    private int isDraw(List<CaptureZone> captureZones) {
        // Counts the number of captured zones per player.
        Map<Integer, Integer> capturedZonesPerPlayer = new HashMap<>();
        for (CaptureZone captureZone : captureZones) {
            if (captureZone.getOwnerPlayerId() != -1) {
                capturedZonesPerPlayer.merge(captureZone.getOwnerPlayerId(), 1, Integer::sum);
            }
        }

        // Sorts the map by captured zones in descending order.
        List<Map.Entry<Integer, Integer>> sorted = capturedZonesPerPlayer.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .toList();
        if (sorted.size() == 1) {
            return sorted.getFirst().getKey();
        }
        if (sorted.size() > 1) {
            if (Objects.equals(sorted.get(0).getValue(), sorted.get(1).getValue())) {
                return -1;
            }
            return sorted.getFirst().getKey();
        }
        return -1;
    }

    /**
     * Checks if the lobby is empty.
     */
    private boolean isLobbyEmpty(Set<Connection> players) {
        return players.isEmpty();
    }

    /**
     * When a player dies (lives go to 0), their character disappears and their inputs freeze for 5 seconds. After that,
     * they respawn with max lives (depending on their role) and default spawn position. Respawns have no limit.
     */
    public void checkRespawn(List<Player> players) {
        players.forEach(player -> {
            if (player.getLives() <= 0 && player.isAlive()) {
                player.setAlive(false);
                player.setRespawnAtMs(System.currentTimeMillis() + 5000);
                player.getBody().setActive(false);
            }
            if (!player.isAlive() && player.getRespawnAtMs() <= System.currentTimeMillis()) {
                if (player.getRole().equals(PlayerRole.MASTER)) {
                    player.setLives(MASTER_LIVES);
                } else if (player.getRole().equals(PlayerRole.OFFICER)) {
                    player.setLives(OFFICER_LIVES);
                }

                Body playerBody = player.getBody();
                playerBody.setActive(true);
                playerBody.setTransform(0f / PPM, 0f / PPM, 0f);
                playerBody.setLinearVelocity(0f, 0f);
                player.setX(0f);
                player.setY(0f);
                player.setAlive(true);
            }
        });
    }
}
