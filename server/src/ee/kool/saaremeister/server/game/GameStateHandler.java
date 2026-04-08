package ee.kool.saaremeister.server.game;

import ee.kool.saaremeister.server.game.object.Building;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import lombok.Getter;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.BuildingState;
import message.dto.BulletState;
import message.dto.CaptureZoneState;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.GAME_TICK_RATE;

@Getter
@Setter
public class GameStateHandler {

    private float gameTime = 0;
    private boolean allPlayersHaveJoined = false;
    private boolean gameOver = false;
    private List<CaptureZoneState> captureZoneStates = new ArrayList<>();
    private int winnerPlayerId = -1;

    /**
     * Increments the game time if all players have joined.
     */
    public void incrementGameTimeIfPlayersPresent() {
        if (allPlayersHaveJoined) {
            gameTime += 1f / GAME_TICK_RATE;
        }
    }

    /**
     * Creates a game state message with the given player and bullet states.
     */
    public Object getGameStateMessage(List<Player> players, List<Bullet> bullets, List<Building> buildings) {
        // Create player states for the game state message.
        var playerStates = new ArrayList<PlayerState>();
        players.forEach(player -> {
            // Only include alive players in the game state.
            if (player.isAlive()) {
                playerStates.add(player.getPlayerState());
            }
        });

        // Create bullet states for the game state message.
        var bulletStates = new ArrayList<BulletState>();
        bullets.forEach(bullet -> bulletStates.add(bullet.getBulletState()));

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
        gameStateMessage.setBuildingStates(buildingStates);
        gameStateMessage.setGameTime(Math.round(gameTime));
        gameStateMessage.setAllPlayersHaveJoined(allPlayersHaveJoined);
        gameStateMessage.setGameOver(gameOver);
        gameStateMessage.setCaptureZones(captureZoneStates);
        gameStateMessage.setWinnerPlayerId(winnerPlayerId);

        return gameStateMessage;
    }

}
