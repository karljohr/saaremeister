package message;

import lombok.Data;

@Data
public class ChatMessage {
    public String text;
    public String username;
    public int lobbyId;
}
