package ee.kool.saaremeister.manager;

import com.esotericsoftware.kryonet.Connection;
import ee.kool.saaremeister.ChatClient;
import messages.PeopleCountMsg;
import messages.TextMsg;

public class ClientManager {
    private final ChatClient chatClient;

    public ClientManager(ChatClient chatClient) {
        // Store reference to the main ChatClient to send messages back to server
        this.chatClient = chatClient;
    }

    public void playerConnected(Connection c) {
        // Called when connection to the server is established
        // Print out the unique connection ID assigned by the server
        System.out.printf("Connected to the server: your ID is %d%n", c.getID());

        // Send a PeopleCountMsg to the server to request current number of connected clients
        PeopleCountMsg msg = new PeopleCountMsg();
        chatClient.getClient().sendTCP(msg);
    }

    public void playerSentText(TextMsg message) {
        // Handle incoming chat message
        if (message.isSentByServer()) {
            // If message was not sent by any player, just print the text
            System.out.println(message.getText());
        } else {
            // Otherwise, print message with sender's ID for identification
            System.out.println("#" + message.getSenderId() + ": " + message.getText());
        }
    }

    public void numberOfPeople(PeopleCountMsg message) {
        // Handle message containing number of connected people
        // Print a welcome message showing how many other users are online
        String text = "Welcome to anonchat. " + message.getNumber() + " other users online.";
        System.out.println(text);
    }
}
