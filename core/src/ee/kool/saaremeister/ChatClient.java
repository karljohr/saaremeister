package ee.kool.saaremeister;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.listener.ClientListener;
import messages.TextMsg;
import network.register.Register;

import java.io.IOException;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int TCP_PORT = 8080;
    private final Client client;
    private final ClientListener clientListener;

    public ChatClient() throws IOException {
        Log.set(Log.LEVEL_INFO); // Reduce verbosity in logs
        client = new Client(); // Creating a KryoNet client
        client.start(); // Start the client's networking threads
        clientListener = new ClientListener(this);

        Register.register(client.getKryo()); // Register all classes that will be sent over the network

        bindServer(); // Connect to the server
        client.addListener(clientListener);
    }

    private void bindServer() throws IOException {
        try {
            // Connect to server with a 15-second timeout
            client.connect(15000, SERVER_IP, TCP_PORT);
            System.out.println("Connected to server: " + SERVER_IP + ":" + TCP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error starting the client: " + e.getMessage());
            System.err.println("Make sure the server is running, reachable, and that the correct IP address and port are set.");
            System.exit(1);
            throw e;
        }
    }

    public Client getClient() {
        return client;
    }

    public void sendMessage(String text) {
        // Create a message object with text
        TextMsg msg = new TextMsg(text, client.getID());
        client.sendTCP(msg); // Send a message via TCP
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient();

        // Reading messages from the console and sending them to the server
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            client.sendMessage(input);
        }
    }
}
