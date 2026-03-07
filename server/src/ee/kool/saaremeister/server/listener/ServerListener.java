package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.GameInstance;
import message.GameJoinMessage;

/**
 * Listens to messages sent by clients and determines what to do with them.
 */
public class ServerListener extends Listener {
    private GameInstance game;

    public ServerListener() { }

    @Override
    public void connected(Connection connection) {
        Log.info("Client connected: " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
        super.connected(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        Log.info("Client disconnected: " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
        if (game != null) {
            game.removeConnection(connection);
        }
        super.disconnected(connection);
    }

    /**
     * Determines what to do with the received message based on the message type.
     */
    @Override
    public void received(Connection connection, Object object) {
        Log.debug("Received message from client: " + connection.getRemoteAddressTCP().getAddress().getHostAddress() + ": " + object.toString());

        // when a GameJoinMessage is received, the server will add the connection to the game instance
        // if there is no active instance, a new one is created
        if (object instanceof GameJoinMessage) {
            if (game == null) {
                game = new GameInstance(this, connection); // Create a new game instance for the first player (connection)
                game.start(); // Start the Thread, which contains the main game loop
            } else {
                game.addConnection(connection);
            }

            super.received(connection, object);
        }
    }

    /**
     * Disposes of the current game instance by setting the game reference to null.
     */
    public void disposeGame() {
        this.game = null;
    }
}
