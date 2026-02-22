package ee.kool.saaremeister.listener;

import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.ChatClient;
import ee.kool.saaremeister.manager.ClientManager;
import messages.PeopleCountMsg;
import messages.TextMsg;

public class ClientListener extends Listener {
    private final ClientManager clientManager;

    public ClientListener(ChatClient chatClient) {
        clientManager = new ClientManager(chatClient);
    }

    public void connected(Connection c) {
        clientManager.playerConnected(c);
    }

    @Override
    public void received(Connection c, Object object) {
        // Called when a message or object is received from the server
        switch (object) {
            case TextMsg message ->
                // Handle incoming chat message from the server
                    clientManager.playerSentText(message);

            case PeopleCountMsg message ->
                // Handle message containing the number of connected players
                    clientManager.numberOfPeople(message);
            case FrameworkMessage.KeepAlive ignored ->
                // Handle KryoNet's KeepAlive heartbeat message
                // (used internally to keep the TCP connection alive)
                    Log.debug("Received KeepAlive message");
            default -> {
                // Can be ignored
            }
        }
    }
}
