package ee.kool.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.server.ChatServer;
import ee.kool.server.manager.ChatServerManager;
import messages.PeopleCountMsg;
import messages.TextMsg;

public class ChatServerListener extends Listener{
    private final ChatServerManager chatServerManager;

    public ChatServerListener(ChatServer chatServer) {
        chatServerManager = new ChatServerManager(chatServer);
    }

    public void connected(Connection c) {
        chatServerManager.playerConnected(c);
    }

    public void disconnected(Connection c) {
        chatServerManager.playerDisconnected(c);
    }

    @Override
    public void received(Connection c, Object object) {
        switch (object) {
            case TextMsg message -> chatServerManager.playerSentText(c, message);
            case PeopleCountMsg message -> chatServerManager.numberOfPeopleRequest(c, message);
            case FrameworkMessage.KeepAlive ignored ->
                    Log.debug("Received KeepAlive message"); // Handles KryoNet's heartbeat packet to keep the connection alive
            default -> {
                // Can be ignored
            }
        }
    }
}
