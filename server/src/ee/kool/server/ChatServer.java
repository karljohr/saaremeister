package ee.kool.server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import ee.kool.server.listener.ChatServerListener;
import register.Register;

import java.io.IOException;

public class ChatServer {
    private static final int TCP_PORT = 8080;
    private final Server server;
    private final ChatServerListener serverListener;

    public ChatServer() throws IOException {
        Log.set(Log.LEVEL_INFO);

        server = new Server();
        serverListener = new ChatServerListener(this);

        Register.register(server.getKryo());

        bindServer();

        server.addListener(serverListener);
        server.start();
    }

    private void bindServer() throws IOException {
        try {
            server.bind(TCP_PORT);
            System.out.println("Server started on port: " + TCP_PORT);
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            System.err.println("Failed to bind server!");
            throw e;
        }
    }

    public Server getServer() {
        return server;
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
    }
}
