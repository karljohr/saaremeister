package message;

import lombok.Data;

@Data
public class GameJoinMessage {
    private int lobbyId;
    private String username;
}
