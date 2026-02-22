package ee.kool.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.server.ChatServer;
import ee.kool.server.manager.ServerManager;
import messages.PeopleCountMsg;
import messages.TextMsg;

public class ServerListener extends Listener{
    private final ServerManager serverManager;

    public ServerListener(ChatServer chatServer) {
        serverManager = new ServerManager(chatServer);
    }

    public void connected(Connection c) {
        serverManager.playerConnected(c);
    }

    public void disconnected(Connection c) {
        serverManager.playerDisconnected(c);
    }

    @Override
    public void received(Connection c, Object object) {
        switch (object) {
            case TextMsg message -> serverManager.playerSentText(c, message);
            case PeopleCountMsg message -> serverManager.numberOfPeopleRequest(c, message);
            case FrameworkMessage.KeepAlive ignored ->
                    Log.debug("Received KeepAlive message"); // Handles KryoNet's heartbeat packet to keep the connection alive
            default -> {
                // Can be ignored
            }
        }
    }
}
