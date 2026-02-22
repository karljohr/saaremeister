package ee.kool.server.manager;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.server.ChatServer;
import messages.PeopleCountMsg;
import messages.TextMsg;

import java.util.ArrayList;
import java.util.List;

public class ServerManager {
    private final ChatServer chatServer;
    private final List<Integer> players = new ArrayList<>();

    public ServerManager(ChatServer chatServer) {
        this.chatServer = chatServer;
    }

    public void playerConnected(Connection c) {
        int clientId = c.getID();
        players.add(clientId);

        String text = "User #%d has joined the chat.".formatted(clientId);
        sendMessageToAllExcept(clientId, text);
    }

    public void playerDisconnected(Connection c) {
        int clientId = c.getID();
        players.removeIf(id -> id == clientId);
        String text = "User #%d has left the chat.".formatted(clientId);
        sendMessageToAllExcept(clientId, text);
    }

    public void playerSentText(Connection c, TextMsg message) {
        String text = "Received message: '%s' from client with ID='%d'"
                .formatted(message.getText(), c.getID());
        System.out.println(text);
        sendMessageToAll(message.getText(), c.getID());
    }

    public void numberOfPeopleRequest(Connection c, PeopleCountMsg message) {
        message.setNumber(players.size() - 1);
        chatServer.getServer().sendToTCP(c.getID(), message);
    }

    private void sendMessageToAll(String text, int id) {
        TextMsg msg = new TextMsg(text, id);
        chatServer.getServer().sendToAllTCP(msg);
    }

    private void sendMessageToAllExcept(int clientId, String text) {
        TextMsg msg = new TextMsg(text, -1);
        msg.setSentByServer();
        chatServer.getServer().sendToAllExceptTCP(clientId, msg);
    }
}
