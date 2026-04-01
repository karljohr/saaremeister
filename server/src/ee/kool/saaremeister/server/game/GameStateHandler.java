package ee.kool.saaremeister.server.game;

import ee.kool.saaremeister.server.game.object.Building;
import ee.kool.saaremeister.server.game.object.Bullet;
import ee.kool.saaremeister.server.game.object.Player;
import lombok.Getter;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.BuildingState;
import message.dto.BulletState;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.GAME_TICK_RATE;

@Getter
@Setter
public class GameStateHandler {

    private boolean allPlayersHaveJoined = false;
    private int gameTime = 0;

    public void incrementGameTimeIfPlayersPresent() {
        if (allPlayersHaveJoined) {
            gameTime += 1f / GAME_TICK_RATE;
        }
    }

    public Object getGameStateMessage(List<Player> players, List<Bullet> bullets, List<Building> buildings) {
        var playerStates = new ArrayList<PlayerState>();
        players.forEach(player -> playerStates.add(player.getPlayerState()));

        var bulletStates = new ArrayList<BulletState>();
        bullets.forEach(bullet -> bulletStates.add(bullet.getBulletState()));

        var buildingStates = new ArrayList<BuildingState>();
        buildings.forEach(building -> buildingStates.add(building.getBuildingState()));

        var gameStateMessage = new GameStateMessage();
        gameStateMessage.setPlayerStates(playerStates);
        gameStateMessage.setBulletStates(bulletStates);
        gameStateMessage.setBuildingStates(buildingStates);
        gameStateMessage.setGameTime(Math.round(gameTime));
        gameStateMessage.setAllPlayersHaveJoined(allPlayersHaveJoined);

        return gameStateMessage;
    }

}
