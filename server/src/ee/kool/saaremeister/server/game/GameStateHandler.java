package ee.kool.saaremeister.server.game;

import ee.kool.saaremeister.server.game.object.buildings.Building;
import ee.kool.saaremeister.server.game.object.projectiles.Bullet;
import ee.kool.saaremeister.server.game.object.player.Player;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import ee.kool.saaremeister.server.game.object.projectiles.Rocket;
import lombok.Getter;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.*;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.GAME_TICK_RATE;

/**
 * Handles the game state and creates game state messages.
 */
@Getter
@Setter
public class GameStateHandler {

    private float gameTime = 0;
    private boolean allPlayersHaveJoined = false;
    private boolean gameOver = false;
    private List<CaptureZoneState> captureZoneStates = new ArrayList<>();
    private PlayerRole winnerPlayerRole = null;
    private long matchStartMs = -1L;

    /**
     * Increments the game time if all players have joined using the current time.
     */
    public void incrementGameTimeIfPlayersPresent() {
        if (!allPlayersHaveJoined) return;

        long now = System.currentTimeMillis();
        if (matchStartMs < 0L) {
            matchStartMs = now;
        }
        gameTime = (now - matchStartMs) / 1000f;
    }

    /**
     * Creates a game state message with the given player and bullet states.
     */
    public Object getGameStateMessage(
            List<Player> players,
            List<Bullet> bullets,
            List<Building> buildings,
            List<ExplosionState> explosionStates
    ) {
        // Create player states for the game state message.
        var playerStates = new ArrayList<PlayerState>();
        players.forEach(player -> playerStates.add(player.getPlayerState()));

        // Create bullet & rocket states for the game state message.
        var bulletStates = new ArrayList<BulletState>();
        var rocketStates = new ArrayList<RocketState>();
        bullets.forEach(bullet -> {
            if (bullet instanceof Rocket rocket) {
                rocketStates.add(rocket.getRocketState());
            } else {
                bulletStates.add(bullet.getBulletState());
            }
        });

        // Create building states for the game state message.
        var buildingStates = new ArrayList<BuildingState>();
        for (Building building : buildings) {
            if (building instanceof DefenceBuilding defenceBuilding) {
                buildingStates.addAll(defenceBuilding.getTiles());
            } else {
                buildingStates.add(building.getBuildingState());
            }
        }

        // Create a new game state message.
        var gameStateMessage = new GameStateMessage();
        gameStateMessage.setPlayerStates(playerStates);
        gameStateMessage.setBulletStates(bulletStates);
        gameStateMessage.setRocketStates(rocketStates);
        gameStateMessage.setBuildingStates(buildingStates);
        gameStateMessage.setGameTime(gameTime);
        gameStateMessage.setAllPlayersHaveJoined(allPlayersHaveJoined);
        gameStateMessage.setGameOver(gameOver);
        gameStateMessage.setCaptureZones(captureZoneStates);
        gameStateMessage.setExplosionStates(explosionStates);
        gameStateMessage.setWinnerPlayerRole(winnerPlayerRole);

        return gameStateMessage;
    }

}
