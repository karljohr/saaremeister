package ee.kool.saaremeister.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.network.listener.ChatListener;
import ee.kool.saaremeister.network.listener.LobbyListener;
import ee.kool.saaremeister.network.listener.DebugListener;
import lombok.Getter;
import lombok.Setter;
import message.*;
import message.dto.StaticBodyState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static constant.Constants.*;
import static network.Register.registerClasses;

/**
 * Handles the connection to the server and handles incoming messages.
 */
@Getter
@Setter
public class ServerConnection {
    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED
    }

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
    private final Map<Integer, LobbyUpdateMessage> cachedLobbyUpdates = new ConcurrentHashMap<>();
    private StaticBodyState[] cachedStaticBodies;
    private Consumer<RoundEndMessage> roundEndListener;
    private volatile ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private volatile String lastErrorMessage;
    private static final int CONNECTION_TIMEOUT_MS = 15000;

    /**
     * Private constructor to prevent instantiation.
     */
    private ServerConnection() {
        client = new Client(65536, 65536);
        registerClasses(client.getKryo());
        lobbyListener = new LobbyListener(this);
        chatListener = new ChatListener(this);
        client.addListener(lobbyListener);
        client.addListener(chatListener);
        client.addListener(new DebugListener(this));
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                connectionStatus = ConnectionStatus.CONNECTED;
                lastErrorMessage = null;
            }

            @Override
            public void disconnected(Connection connection) {
                if (connectionStatus == ConnectionStatus.CONNECTED) {
                    connectionStatus = ConnectionStatus.FAILED;
                    lastErrorMessage = "Uhendus serveriga katkes.";
                } else if (connectionStatus == ConnectionStatus.CONNECTING) {
                    connectionStatus = ConnectionStatus.FAILED;
                    if (lastErrorMessage == null) {
                        lastErrorMessage = "Uhendus serveriga ebaonnestus.";
                    }
                } else {
                    connectionStatus = ConnectionStatus.DISCONNECTED;
                }
            }
        });
        client.start();
    }

    /**
     * Returns the singleton instance of ServerConnection.
     */
    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    /**
     * Connects the client to the server.
     */
    public void connect() {
        if (connectionStatus == ConnectionStatus.CONNECTING ||
                connectionStatus == ConnectionStatus.CONNECTED) {
            return;
        }

        connectionStatus = ConnectionStatus.CONNECTING;
        lastErrorMessage = null;

        new Thread(() -> {
            try {
                client.stop();
                client.close();
                client.start();

                client.connect(CONNECTION_TIMEOUT_MS, SERVER_IP, TCP_PORT, UDP_PORT);
            } catch (Exception e) {
                connectionStatus = ConnectionStatus.FAILED;
                lastErrorMessage = "Serveriga ei saanud uhendust.";
                Gdx.app.error("ServerConnection", "Failed to connect to server", e);
            }
        }, "ServerConnection").start();
    }

    /**
     * Joins a lobby with the given lobby ID and username.
     */
    public void joinLobby(int lobbyId, String username, Consumer<LobbyUpdateMessage> callback) {
        if (connectionStatus != ConnectionStatus.CONNECTED) {
            lastErrorMessage = "Serveriga ei ole uhendust.";
            connectionStatus = ConnectionStatus.FAILED;
            return;
        }

        this.pendingJoinCallback = callback;
        GameJoinMessage joinMessage = new GameJoinMessage();
        joinMessage.setLobbyId(lobbyId);
        joinMessage.setUsername(username);
        try {
            client.sendTCP(joinMessage);
            Gdx.app.log("ServerConnection", "Join request sent for lobby " + lobbyId);
        } catch (Exception e) {
            this.pendingJoinCallback = null;
            connectionStatus = ConnectionStatus.FAILED;
            lastErrorMessage = "Lobbyga liitumine ebaonnestus.";
            Gdx.app.error("ServerConnection", "Failed to join lobby", e);
        }
    }

    /**
     * Handles the StartGameMessage and calls the startGameListener if it is not null.
     */
    public void handleStartGame(StartGameMessage msg) {
        if (startGameListener != null) {
            try {
                startGameListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in start game listener", e);
            }
        }
    }

    /**
     * Handles the RoundEndMessage and calls the roundEndListener if it is not null.
     */
    public void handleRoundEnd(RoundEndMessage msg) {
        if (roundEndListener != null) {
            try {
                roundEndListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in round end listener", e);
            }
        }
    }

    /**
     * Handles the LobbyUpdateMessage and calls the lobbyUpdateListener if it is not null.
     */
    public void handleLobbyUpdate(LobbyUpdateMessage msg) {
        cachedLobbyUpdates.put(msg.getLobbyId(), msg);
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

    /**
     * Handles the LobbyExitMessage and calls the lobbyExitListener if it is not null.
     */
    public void handleLobbyExit(LobbyExitMessage msg) {
        if (lobbyExitListener != null) {
            try {
                lobbyExitListener.accept(msg);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in lobby exit listener", e);
            }
        }
    }

    /**
     * Handles the ChatUpdateMessage and calls the chatUpdateListener if it is not null.
     */
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

    /**
     * Replays the cached lobby updates for the given lobby ID, so that when a player joins a lobby after a game round,
     * they see the previous lobby state.
     */
    public void replayCachedLobbyUpdates(int lobbyId) {
        LobbyUpdateMessage cachedUpdate = cachedLobbyUpdates.get(lobbyId);
        if (cachedUpdate != null && lobbyUpdateListener != null) {
            try {
                lobbyUpdateListener.accept(cachedUpdate);
            } catch (Exception e) {
                Gdx.app.error("ServerConnection", "Error in lobby update listener", e);
            }
        }
    }

    /**
     * Handles the StaticBodyState array and calls the staticBodiesListener if it is not null.
     */
    public void handleStaticBodies(StaticBodyState[] bodies) {
        this.cachedStaticBodies = bodies;
        if (staticBodiesListener != null) {
            staticBodiesListener.accept(bodies);
        }
    }

    /**
     * Sets the staticBodiesListener and calls it with the cachedStaticBodies if it is not null.
     */
    public void setStaticBodiesListener(Consumer<StaticBodyState[]> listener) {
        this.staticBodiesListener = listener;
        if (listener != null && cachedStaticBodies != null) {
            listener.accept(cachedStaticBodies);
        }
    }
}
