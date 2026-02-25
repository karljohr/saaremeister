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
    private final GameServer gameServer;

    public GameServerListener(GameServer gameServer) {
        this.gameServer = gameServer;
        this.gameServerManager = new GameServerManager(gameServer);
    }

    public void connected(Connection c) {
        gameServer.onConnected(c);
        gameServerManager.playerConnected(c);
    }

    public void disconnected(Connection c) {
        gameServer.onDisconnected(c);
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
