package message;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class LobbyUpdateMessage {
    private int lobbyId;
    private int playerCount;
    private boolean started;
    private List<String> usernames;
    private int currentRound;
    private int totalRounds;
    private boolean seriesActive;
    private HashMap<Integer, Integer> winsByPlayerId = new HashMap<>();
    private HashMap<String, Integer> winsByUsername = new HashMap<>();

    public LobbyUpdateMessage() {
    }
}
