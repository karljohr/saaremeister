package message;

import lombok.Data;

import java.util.List;

@Data
public class LobbyUpdateMessage {
    private int lobbyId;
    private int playerCount;
    private boolean started;
    private List<String> usernames;

    public LobbyUpdateMessage() {
    }
}
