package ee.kool.saaremeister.server.chat;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.server.listener.ServerListener;
import lombok.Data;
import message.ChatMessage;
import message.ChatUpdateMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class ChatManager {

    private final Map<Integer, Chat> chats = new HashMap<>();
    private final ServerListener serverListener;

    public ChatManager(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    /**
     * Sends the ChatMessage to the correct chat.
     */
    public void handleNewMessage(ChatMessage message) {
        int lobbyId = message.getLobbyId();
        Chat chat = chats.get(lobbyId);
        if (chat != null) {
            chat.addMessage(message);
            System.out.println("[SERVER] Broadcasting update to " + chat.getPlayers().size() + " players.");
            ChatUpdateMessage chatUpdateMessage = new ChatUpdateMessage();
            chatUpdateMessage.setLobbyId(lobbyId);
            chatUpdateMessage.setMessages(new ArrayList<>(chat.getMessages()));

            for (Connection connection : chat.getPlayers()) {
                connection.sendTCP(chatUpdateMessage);
            }
        }
    }

    public Chat getChat(int lobbyId) {
        return chats.get(lobbyId);
    }
}
