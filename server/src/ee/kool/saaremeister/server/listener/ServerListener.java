package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import message.GameJoinMessage;
import message.LobbyExitMessage;
import message.StartGameMessage;

import java.net.InetSocketAddress;

/**
 * Listens to messages sent by clients and determines what to do with them.
 */
public class ServerListener extends Listener {
    private final LobbyManager lobbyManager;

    public ServerListener() {
        this.lobbyManager = new LobbyManager(this);
    }

    @Override
    public void connected(Connection connection) {
        InetSocketAddress address = connection.getRemoteAddressTCP();
        if (address == null) {
            return;
        }
        Log.info("Client connected: " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
        super.connected(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        lobbyManager.removePlayerFromAllLobbies(connection);
        InetSocketAddress address = connection.getRemoteAddressTCP();
        if (address == null) {
            return;
        }
        Log.info("Client disconnected: " + connection.getRemoteAddressTCP().getAddress().getHostAddress());
        super.disconnected(connection);
    }

    /**
     * Determines what to do with the received message based on the message type.
     */
    @Override
    public void received(Connection connection, Object object) {
        InetSocketAddress address = connection.getRemoteAddressTCP();
        if (address == null) {
            return;
        }
        Log.debug("Received message from client: "
                + connection.getRemoteAddressTCP().getAddress().getHostAddress()
                + ": " + object.toString());

        switch (object) {
            case GameJoinMessage gameJoinMessage -> lobbyManager.handleJoinRequest(connection, gameJoinMessage);
            case StartGameMessage startGameMessage -> lobbyManager.handleStartGameRequest(connection, startGameMessage);
            case LobbyExitMessage exitMessage -> lobbyManager.handleExitRequest(connection, exitMessage);
            default -> {
            }
        }

        super.received(connection, object);
    }

}
