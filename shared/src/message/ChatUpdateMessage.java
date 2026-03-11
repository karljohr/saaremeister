package message;

import lombok.Data;

import java.util.List;

@Data
public class ChatUpdateMessage {
    private int lobbyId;
    private List<String> messages;

    public ChatUpdateMessage() {
    }
}
