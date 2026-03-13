package ee.kool.saaremeister.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import ee.kool.saaremeister.network.listener.LobbyListener;
import ee.kool.saaremeister.network.listener.debugListener;
import lombok.Getter;
import lombok.Setter;
import message.GameJoinMessage;
import message.LobbyExitMessage;
import message.LobbyUpdateMessage;
import message.StartGameMessage;
import message.dto.StaticBodyState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static constant.Constants.*;
import static network.Register.registerClasses;

@Getter
@Setter
public class ServerConnection {
    private static ServerConnection instance;
    private final Client client;
    private final LobbyListener lobbyListener;
    private Consumer<StartGameMessage> startGameListener;
    private Consumer<LobbyUpdateMessage> lobbyUpdateListener;
    private Consumer<LobbyExitMessage> lobbyExitListener;
    private final Map<Integer, Consumer<LobbyUpdateMessage>> pendingJoinCallbacks = new ConcurrentHashMap<>();
    private Consumer<StaticBodyState[]> staticBodiesListener;

    private StaticBodyState[] cachedStaticBodies;

    private ServerConnection() {
        client = new Client(65536, 65536);
        registerClasses(client.getKryo());
        lobbyListener = new LobbyListener(this);
        client.addListener(lobbyListener);
        client.addListener(new debugListener(this));
        client.start();
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public void connect() {
        new Thread(() -> {
            try {
                client.connect(5000, SERVER_IP, TCP_PORT, UDP_PORT);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Failed to connect to server", e);
            }
        }, "ServerConnection").start();
    }

    public void joinLobby(int lobbyId, String username, Consumer<LobbyUpdateMessage> callback) {
        pendingJoinCallbacks.put(lobbyId, callback);
        GameJoinMessage joinMessage = new GameJoinMessage();
        joinMessage.setLobbyId(lobbyId);
        joinMessage.setUsername(username);
        try {
            client.sendTCP(joinMessage);
        } catch (Exception e) {
            pendingJoinCallbacks.remove(lobbyId);
            Gdx.app.error("ServerConnection", "Failed to join lobby", e);
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

    public void handleLobbyUpdate(LobbyUpdateMessage msg) {
        Consumer<LobbyUpdateMessage> callBack = pendingJoinCallbacks.remove(msg.getLobbyId());
        if (callBack != null) {
            try {
                callBack.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in callback", e);
            }
        } else if (lobbyUpdateListener != null) {
            try {
                lobbyUpdateListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in lobby update listener", e);
            }
        }
    }

    public void handleLobbyExit(LobbyExitMessage msg) {
        if (lobbyExitListener != null) {
            try {
                lobbyExitListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in lobby exit listener", e);
            }
        }
    }

    public void handleStaticBodies(StaticBodyState[] bodies) {
        this.cachedStaticBodies = bodies;
        if (staticBodiesListener != null) {
            staticBodiesListener.accept(bodies);
        }
    }

    public void setStaticBodiesListener(Consumer<StaticBodyState[]> listener) {
        this.staticBodiesListener = listener;
        if (cachedStaticBodies != null) {
            listener.accept(cachedStaticBodies);
        }
    }

}
