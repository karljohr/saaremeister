package ee.kool.saaremeister.game;

import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.network.listener.GameStateMessageListener;
import lombok.Getter;
import lombok.Setter;
import message.GameStateMessage;

import java.util.ArrayList;

@Setter
@Getter
public class GameStateManager {

    private GameStateMessage latestGameStateMessage;

    public GameStateManager() {
        ServerConnection
                .getInstance()
                .getClient()
                .addListener(new GameStateMessageListener(this));

        latestGameStateMessage = new GameStateMessage();
        latestGameStateMessage.setPlayerStates(new ArrayList<>());
        latestGameStateMessage.setBulletStates(new ArrayList<>());
        latestGameStateMessage.setGameTime(0);
        latestGameStateMessage.setAllPlayersHaveJoined(false);
    }

}
