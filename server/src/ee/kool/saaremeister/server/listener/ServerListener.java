package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import message.GameJoinMessage;

/**
 * Listens to messages sent by clients and determines what to do with them.
 */
public class ServerListener extends Listener {

    public ServerListener() { }

    @Override
    public void connected(Connection connection) {
        Log.info("Client connected: " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
        super.connected(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        Log.info("Client disconnected: " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
        super.disconnected(connection);
    }

    /**
     * Determines what to do with the received message based on the message type.
     */
    @Override
    public void received(Connection connection, Object object) {
        Log.debug("Received message from client: " + connection.getRemoteAddressTCP().getAddress().getHostAddress() + ": " + object.toString());
        // switch
        super.received(connection, object);
    }
}
