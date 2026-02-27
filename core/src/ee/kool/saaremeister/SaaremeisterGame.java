package ee.kool.saaremeister;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Client;
import register.Register;

public class SaaremeisterGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapes;

    // Networking
    private ChatClient chatClient; // For chat (Port 8080)
    private Client gameNetworkClient; // For game (Port 8081/8082)

    // Tile map
    private TiledMap map;
    private IsometricTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private static final float UNIT_SCALE = 1f;
    private static final float CAMERA_MIN_ZOOM = 0.5f;
    private static final float CAMERA_MAX_ZOOM = 4f;

    private java.util.List<messages.DTO.PlayerState> otherPlayers = new java.util.ArrayList<>();
    private final float playerSize = 20f; // pixels when UNIT_SCALE == 1f

    private final Rectangle goal = new Rectangle(500, 300, 100, 50);
    private final Rectangle tempRect = new Rectangle();
    private boolean won = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        // Camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() * UNIT_SCALE, Gdx.graphics.getHeight() * UNIT_SCALE);
        camera.update();

        // Load tiled map
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new IsometricTiledMapRenderer(map, UNIT_SCALE);

        try {
            // Activate chat
            chatClient = new ChatClient(this);

            // Activate game
            gameNetworkClient = new Client();
            Register.register(gameNetworkClient.getKryo());
            gameNetworkClient.start();
            gameNetworkClient.connect(5000, "127.0.0.1", 8081, 8082);

            // Add listener, which checks world state.
            gameNetworkClient.addListener(new com.esotericsoftware.kryonet.Listener() {
                public void received(com.esotericsoftware.kryonet.Connection c, Object obj) {
                    if (obj instanceof messages.WorldStateMsg msg) {
                        updateWorldState(msg.players);
                    }
                }
            });
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void updateWorldState(java.util.List<messages.DTO.PlayerState> players) {
        this.otherPlayers = players;
    }

    private boolean centerCameraOnPlayer() {
        if (gameNetworkClient == null) return false;
        long myId = gameNetworkClient.getID();

        for (messages.DTO.PlayerState ps : otherPlayers) {
            if (ps.playerId == myId) {
                float targetX = ps.x + playerSize / 2f;
                float targetY = ps.y + playerSize / 2f;

                camera.position.set(targetX, targetY + 50, 0);
                camera.position.lerp(new Vector3(targetX, targetY, 0), 0.1f);
                camera.zoom = MathUtils.clamp(camera.zoom, CAMERA_MIN_ZOOM, CAMERA_MAX_ZOOM);
                camera.update();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render() {
        // Take input and send to server.
        messages.DTO.Input inputMsg = new messages.DTO.Input();

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) inputMsg.up = true;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) inputMsg.down = true;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) inputMsg.left = true;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) inputMsg.right = true;

        if (gameNetworkClient.isConnected()) {
            gameNetworkClient.sendUDP(inputMsg);
        }

        camera.update();

        // Screen.
        Gdx.gl.glClearColor(0.2f, 0.45f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean centered = centerCameraOnPlayer();
        if (!centered) {
            camera.position.set(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f, 0);
        }

        // Render tiled map
        mapRenderer.setView(camera);
        mapRenderer.render();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Drawing goal.
        shapes.setColor(0, 0, 0, 1);
        shapes.rect(goal.x, goal.y, goal.width, goal.height);

        // Draw all players, so its synced.
        for (messages.DTO.PlayerState ps : otherPlayers) {
            tempRect.set(ps.x, ps.y, playerSize, playerSize);

            if (gameNetworkClient != null && ps.playerId == gameNetworkClient.getID()) {
                shapes.setColor(0, 1, 0, 1); // You green

                // Win condition.
                if (!won && goal.overlaps(tempRect)) {
                    won = true;
                    System.out.println("YOU WON!");
                }
            } else {
                shapes.setColor(0, 0, 1, 1); // others blue
            }
            shapes.rect(ps.x, ps.y, playerSize, playerSize);
        }

        shapes.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        map.dispose();
        mapRenderer.dispose();
    }
}