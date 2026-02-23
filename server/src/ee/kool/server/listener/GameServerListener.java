package ee.kool.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.server.GameServer;
import ee.kool.server.manager.GameServerManager;
import messages.DTO.Input;

public class GameServerListener extends Listener {
    private final GameServerManager gameServerManager;

    public GameServerListener(GameServer gameServer) {
        gameServerManager = new GameServerManager(gameServer);
    }

    public void connected(Connection c) {
        gameServerManager.playerConnected(c);
    }

    public void disconnected(Connection c) {
        gameServerManager.playerDisconnected(c);
    }

    public void received(Connection c, Object object) {
        switch (object) {
            case Input input -> gameServerManager.playerInput(c, input);
            case FrameworkMessage.KeepAlive ignored -> Log.debug("Received KeepAlive message");
            default -> {}
        }
    }
}
