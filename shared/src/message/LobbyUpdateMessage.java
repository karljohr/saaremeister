package message;

import lombok.Data;

@Data
public class LobbyUpdateMessage {
    private int lobbyId;
    private int playerCount;
    private boolean started;

    public LobbyUpdateMessage() {
    }
}
