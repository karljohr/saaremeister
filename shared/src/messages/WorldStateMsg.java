package messages;

import messages.DTO.PlayerState;

import java.util.ArrayList;
import java.util.List;

public class WorldStateMsg {
    public List<PlayerState> players = new ArrayList<>();
    public WorldStateMsg() {}
    public WorldStateMsg(List<PlayerState> players) {
        this.players = players;
    }
}
