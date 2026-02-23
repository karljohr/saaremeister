package ee.kool.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import ee.kool.server.listener.GameServerListener;
import ee.kool.server.objects.ServerPlayer;
import messages.DTO.Input;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

public class GameServer {
    private static final int TCP_PORT = 8081;
    private static final int UDP_PORT = 8082;

    private final static float TICK_RATE = 60f;
    private final static float TICK_DELTA = 1f / TICK_RATE;

    private final Server server;
    private final GameServerListener listener;

    private final Map<Integer, ServerPlayer> players = new ConcurrentHashMap<>();
    private final Map<Integer, Input> latestInputs = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public GameServer() throws IOException {
        Log.set(Log.LEVEL_INFO);

        server = new Server();
        listener = new GameServerListener(this);

        network.register.Register.register(server.getKryo());

        bindServer();

        server.addListener(listener);
        server.start();

        startTickThread();

        System.out.println("GameServer started on port: tcp=" + TCP_PORT + ", udp=" + UDP_PORT);
    }

    private void bindServer() throws IOException {
        try {
            server.bind(TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            System.err.println("Failed to bind server!");
            throw e;
        }
    }

    public void onConnected(Connection connection) {
        int id = connection.getID();
        players.putIfAbsent(id, new ServerPlayer());
        Log.info("Player connected: " + id);
    }

    public void onDisconnected(Connection connection) {
        int id = connection.getID();
        players.remove(id);
        latestInputs.remove(id);
        Log.info("Player disconnected: " + id);
    }

    public void onInput(Connection connection, Input input) {
        latestInputs.put(connection.getID(), input);
    }

    private void startTickThread() {
        Thread tick = new Thread(() -> {
            long tickNanos = (long) (1_000_000_000L / TICK_RATE);
            long next = System.nanoTime();

            while (running) {
                long now = System.nanoTime();
                if (now < next) {
                    LockSupport.parkNanos(next - now);
                    continue;
                }

                next += tickNanos;

                tick(TICK_DELTA);
            }
        }, "game-tick");

        tick.setDaemon(true);
        tick.start();
    }

    private void tick(float delta) {
        for (var entry : players.entrySet()) {
            int playerID = entry.getKey();
            ServerPlayer player = entry.getValue();

            Input input = latestInputs.get(playerID);
            if (input == null) continue;

            player.applyInput(input, delta);
        }
    }

    public void stop() {
        running = false;
        server.stop();
    }
}
