package ee.kool.saaremeister.server.chat;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.listener.ServerListener;
import ee.kool.saaremeister.server.lobby.Lobby;
import lombok.Data;
import message.ChatMessage;

@Data
public class ChatManager {

    private final ServerListener serverListener;

    public ChatManager(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    /**
     * Sends the ChatMessage to the correct chat.
     */
    public void handleNewMessage(Connection connection, ChatMessage message) {
        Lobby lobby = serverListener.getLobbyManager().getLobby(message.getLobbyId());
        if (lobby == null) {
            return;
        }
        if (!lobby.hasPlayer(connection)) {
            return;
        }
        lobby.getChat().addMessage(message);
    }

}
