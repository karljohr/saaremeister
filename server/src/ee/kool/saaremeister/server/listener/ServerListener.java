package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.chat.ChatManager;
import ee.kool.saaremeister.server.lobby.LobbyManager;
import lombok.Getter;
import message.ChatMessage;
import message.GameJoinMessage;
import message.LobbyExitMessage;
import message.StartGameMessage;

import java.net.InetSocketAddress;

/**
 * Listens to messages sent by clients and determines what to do with them.
 */
public class ServerListener extends Listener {

    @Getter
    private final LobbyManager lobbyManager;
    private final ChatManager chatManager;

    public ServerListener() {
        this.lobbyManager = new LobbyManager(this);
        this.chatManager = new ChatManager(this);
    }

    @Override
    public void connected(Connection connection) {
        InetSocketAddress address = connection.getRemoteAddressTCP();
        if (address != null && address.getAddress() != null) {
            Log.info("Client connected: " + address.getAddress().getHostAddress());
        } else {
            Log.info("Client connected (address not available yet)");
        }
        super.connected(connection);
    }

    @Override
    public void disconnected(Connection connection) {
        lobbyManager.removePlayerFromAllLobbies(connection);
        InetSocketAddress address = connection.getRemoteAddressTCP();
        if (address != null && address.getAddress() != null) {
            Log.info("Client disconnected: " + address.getAddress().getHostAddress());
        } else {
            Log.info("Client disconnected (connection already lost)");
        }
        super.disconnected(connection);
    }

    /**
     * Determines what to do with the received message based on the message type.
     */
    @Override
    public void received(Connection connection, Object object) {
        InetSocketAddress address = connection.getRemoteAddressTCP();
        String hostAddr = (address != null && address.getAddress() != null)
                ? address.getAddress().getHostAddress()
                : "Unknown IP";

        // Logime debug info turvaliselt
        Log.debug("Received " + object.getClass().getSimpleName() + " from " + hostAddr);

        switch (object) {
            case GameJoinMessage gameJoinMessage -> lobbyManager.handleJoinRequest(connection, gameJoinMessage);
            case ChatMessage chatMessage -> {
                chatManager.handleNewMessage(connection, chatMessage);
                System.out.println("[SERVER] Received chat from " + chatMessage.getUsername() + ": " + chatMessage.getText());
            }
            case StartGameMessage startGameMessage -> lobbyManager.handleStartGameRequest(connection, startGameMessage);
            case LobbyExitMessage exitMessage -> {
                lobbyManager.handleExitRequest(connection, exitMessage);

                super.received(connection, object);
            }
            default -> {
            }
        }

    }

    public Object getChatManager() {
        return chatManager;
    }
}
