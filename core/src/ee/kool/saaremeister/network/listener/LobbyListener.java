package ee.kool.saaremeister.network.listener;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.network.ServerConnection;
import message.ChatUpdateMessage;
import message.LobbyExitMessage;
import message.LobbyUpdateMessage;
import message.StartGameMessage;

public class LobbyListener extends Listener {

    private final ServerConnection serverConnection;

    public LobbyListener(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object == null) {
            return;
        }

        Gdx.app.postRunnable(() -> {
            switch (object) {
                case LobbyUpdateMessage lobbyUpdateMessage -> serverConnection.handleLobbyUpdate(lobbyUpdateMessage);
                case StartGameMessage startGameMessage -> serverConnection.handleStartGame(startGameMessage);
                case LobbyExitMessage lobbyExitMessage -> serverConnection.handleLobbyExit(lobbyExitMessage);
                default -> {
                }
            }
        });
    }
}
