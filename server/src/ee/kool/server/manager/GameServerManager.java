package ee.kool.server.manager;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.server.GameServer;
import messages.DTO.Input;

import java.util.ArrayList;
import java.util.List;

public class GameServerManager {
    private final GameServer gameServer;
    private final List<Integer> players = new ArrayList<>();

    public GameServerManager(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public void playerConnected(Connection c) {
        int clientId = c.getID();
        players.add(clientId);
    }

    public void playerDisconnected(Connection c) {
        int clientId = c.getID();
        players.removeIf(id -> id == clientId);
    }

    public void playerInput(Connection c, Input input) {
        gameServer.onInput(c, input);
    }
}
