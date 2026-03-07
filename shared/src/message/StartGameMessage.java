package message;

import lombok.Data;

@Data
public class StartGameMessage {

    private int lobbyId;

    public StartGameMessage() {
    }
}
