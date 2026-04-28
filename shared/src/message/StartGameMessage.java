package message;

import lombok.Data;

@Data
public class StartGameMessage {

    private int lobbyId;
    private int currentRound;
    private int totalRounds;

    public StartGameMessage() {
    }
}
