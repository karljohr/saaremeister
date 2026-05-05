package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.game.object.CaptureZone;
import ee.kool.saaremeister.server.game.object.player.Master;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.game.object.buildings.Building;
import lombok.Getter;
import message.dto.PlayerRole;

import java.util.*;

import static constant.Constants.*;

/**
 * Winning, drawing, losing conditions, and respawning logic.
 */
public class MatchRules {

    @Getter
    private PlayerRole gameWinnerRole = null;

    public enum MatchEndReason {
        NONE,
        ALL_ZONES_CAPTURED,
        TIME_LIMIT_REACHED,
        NO_PLAYERS_CONNECTED
    }

    /**
     * Determines the reason why the game ended, which is used by GameInstance.
     */
    public MatchEndReason determineEndReason(
            List<CaptureZone> captureZones,
            Set<Connection> connections,
            List<Player> players,
            float gameTime) {

        if (areAllZonesCapturedByOneClass(captureZones, players)) {
            var gameWinnerEnum = captureZones.getFirst().getOwnerEnum();
            if (gameWinnerEnum == CaptureZone.ZoneOwner.MASTER) {
                gameWinnerRole = PlayerRole.MASTER;
            }
            if (gameWinnerEnum == CaptureZone.ZoneOwner.OFFICER) {
                gameWinnerRole = PlayerRole.OFFICER;
            }
            return MatchEndReason.ALL_ZONES_CAPTURED;
        }

        // If the game time is over, Island Master wins.
        if (isGameTimeOver(gameTime)) {
            gameWinnerRole = PlayerRole.MASTER;
            return MatchEndReason.TIME_LIMIT_REACHED;
        }

        if (isLobbyEmpty(connections)) {
            return MatchEndReason.NO_PLAYERS_CONNECTED;
        }

        return MatchEndReason.NONE;
    }

    /**
     * Checks if one player captures all capture zones. The player must not be the Island Master.
     */
    private boolean areAllZonesCapturedByOneClass(List<CaptureZone> captureZones, List<Player> players) {
        if (captureZones.isEmpty()) return false;

        CaptureZone.ZoneOwner ownerEnum = captureZones.getFirst().getOwnerEnum();

        // No winner if neutral, and Island Master should not win by zone control.
        if (ownerEnum != CaptureZone.ZoneOwner.OFFICER) return false;

        return captureZones.stream().allMatch(zone -> zone.getOwnerEnum() == ownerEnum);
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
                SpawnPointLoader.setRandomSpawnPoint(
                        player,
                        player.getRole(),
                        player.getGameInstance().getSpawnPoints()
                );
                playerBody.setLinearVelocity(0f, 0f);
                player.setAlive(true);
            }
        });
    }

    /**
     * Destroys buildings that have no health left.
     */
    public void checkBrokenBuildings(List<Building> buildings) {
        Iterator<Building> iterator = buildings.iterator();

        while (iterator.hasNext()) {
            Building building = iterator.next();

            if (building.getHealth() <= 0) {
                building.destroy();
                iterator.remove();
            }
        }
    }

}
