package ee.kool.saaremeister.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import ee.kool.saaremeister.network.listener.ChatListener;
import ee.kool.saaremeister.network.listener.LobbyListener;
import ee.kool.saaremeister.network.listener.DebugListener;
import lombok.Getter;
import lombok.Setter;
import message.GameJoinMessage;
import message.LobbyExitMessage;
import message.LobbyUpdateMessage;
import message.StartGameMessage;
import message.dto.StaticBodyState;
import message.ChatUpdateMessage;

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
    private final ChatListener chatListener;
    private Consumer<StartGameMessage> startGameListener;
    private Consumer<LobbyUpdateMessage> lobbyUpdateListener;
    private Consumer<LobbyExitMessage> lobbyExitListener;
    private Consumer<ChatUpdateMessage> chatUpdateListener;
    private Consumer<LobbyUpdateMessage> pendingJoinCallback;
    private Consumer<StaticBodyState[]> staticBodiesListener;
    private final Map<Integer, ChatUpdateMessage> cachedChatUpdates = new ConcurrentHashMap<>();

    private StaticBodyState[] cachedStaticBodies;

    private ServerConnection() {
        client = new Client(65536, 65536);
        registerClasses(client.getKryo());
        lobbyListener = new LobbyListener(this);
        chatListener = new ChatListener(this);
        client.addListener(lobbyListener);
        client.addListener(chatListener);
        client.addListener(new DebugListener(this));
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
        this.pendingJoinCallback = callback;
        GameJoinMessage joinMessage = new GameJoinMessage();
        joinMessage.setLobbyId(lobbyId);
        joinMessage.setUsername(username);
        try {
            client.sendTCP(joinMessage);
            Gdx.app.log("ServerConnection", "Join request sent for lobby " + lobbyId);
        } catch (Exception e) {
            this.pendingJoinCallback = null;
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
        if (pendingJoinCallback != null) {
            try {
                Consumer<LobbyUpdateMessage> callback = this.pendingJoinCallback;
                this.pendingJoinCallback = null;
                callback.accept(msg);
                Gdx.app.log("ServerConnection", "Joined lobby: " + msg.getLobbyId());
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

    public void handleChatUpdate(ChatUpdateMessage msg) {
        System.out.println("[CLIENT] Received ChatUpdateMessage from server! Messages count: " + msg.getMessages().size());
        cachedChatUpdates.put(msg.getLobbyId(), msg);
        if (chatUpdateListener != null) {
            try {
                chatUpdateListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in chat update listener", e);
            }
        } else {
            System.out.println("[CLIENT] ERROR: chatUpdateListener is NULL!");
        }
    }

    /**
     * Replays the cached chat updates for the given lobby ID, so that when a player joins a lobby or a game, they
     * see the previous chat history.
     */
    public void replayCachedChatUpdates(int lobbyId) {
        ChatUpdateMessage cachedUpdate = cachedChatUpdates.get(lobbyId);
        if (cachedUpdate != null && chatUpdateListener != null) {
            try {
                chatUpdateListener.accept(cachedUpdate);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in chat update listener", e);
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
