package message;

import lombok.Data;
import message.dto.PlayerRole;

import java.util.HashMap;

@Data
public class RoundEndMessage {
    private int lobbyId;
    private int currentRound;
    private int totalRounds;
    private boolean finalRound;
    private PlayerRole winnerRole;
    private HashMap<Integer, Integer> winsByPlayerId = new HashMap<>();
    private HashMap<String, Integer> winsByUsername = new HashMap<>();
}
