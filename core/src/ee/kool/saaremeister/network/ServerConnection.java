package ee.kool.saaremeister.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import lombok.Getter;
import lombok.Setter;
import message.GameJoinMessage;
import message.StartGameMessage;

import java.util.function.Consumer;

import static constant.Constants.*;
import static network.Register.registerClasses;

@Getter
@Setter
public class ServerConnection {
    private static ServerConnection instance;
    private final Client client;
    private Consumer<StartGameMessage> startGameListener;

    private ServerConnection() {
        client = new Client();
        registerClasses(client.getKryo());
        client.start();
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public void connect() {
        try {
            client.connect(5000, SERVER_IP, TCP_PORT, UDP_PORT);
        } catch (Exception e) {
            Gdx.app.error("ServerConnection", "Failed to connect to server", e);
        }
    }

    public void handleStartGame(StartGameMessage msg) {
        if (startGameListener != null) {
            try {
                startGameListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in start game listener", e);
            }
        }
    }

}
