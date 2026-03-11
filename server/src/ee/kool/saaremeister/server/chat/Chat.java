package ee.kool.saaremeister.server.chat;

import lombok.Data;
import message.ChatMessage;
import message.ChatUpdateMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.esotericsoftware.kryonet.Connection;

/**
 * Represents a chat instance in a single lobby or a game instance.
 */
@Data
public class Chat {

    private final int lobbyId;
    private final Set<Connection> players;
    private List<String> messages = new ArrayList<>();

    public Chat(int lobbyId, Set<Connection> players) {
        this.lobbyId = lobbyId;
        this.players = players;
    }

    /**
     * If the player who sent the message is in the same lobby as this chat, add the message to the chat.
     */
    public void addMessage(ChatMessage message) {
        if (message.getLobbyId() == lobbyId) {
            messages.add("<" + message.getUsername() + "> " + message.getText());
            broadcastChatUpdate();
        }
    }

    /**
     * Updates the current chat for all players in the lobby or game instance.
     */
    private void broadcastChatUpdate() {
        ChatUpdateMessage chatUpdateMessage = new ChatUpdateMessage();
        chatUpdateMessage.setLobbyId(lobbyId);
        chatUpdateMessage.setMessages(messages);
        players.forEach(connection -> connection.sendTCP(chatUpdateMessage));
    }
}
