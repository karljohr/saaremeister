package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.component.ChatTable;
import ee.kool.saaremeister.network.ServerConnection;
import message.LobbyExitMessage;
import message.StartGameMessage;

import java.util.List;

import static ee.kool.saaremeister.component.ButtonComponent.getButton;
import static ee.kool.saaremeister.component.LabelComponent.createLabel;

public class LobbyScreen extends ScreenAdapter {

    private final Stage stage;
    private final Game game;
    private final int lobbyId;
    private Label playerCountLabel;
    private int currentPlayerCount;
    private Label playerNameLabel;
    private List<String> currentPlayerNames;
    private ChatTable chatTable;
    private final String username;

    public LobbyScreen(Game game, int lobbyId, int initialPlayerCount, List<String> usernames, String username) {
        this.game = game;
        this.lobbyId = lobbyId;
        this.currentPlayerCount = initialPlayerCount;
        this.currentPlayerNames = usernames;
        this.username = username;
        stage = new Stage();

        Gdx.input.setInputProcessor(stage);
        createUi();
    }

    private void createUi() {
        Label lobbyCodeLabel = createLabel("Lobby " + lobbyId, Color.BLACK, 24);

        playerCountLabel = createLabel("Players: " + currentPlayerCount, Color.BLACK, 24);

        // A label that displays the usernames currently joined in the lobby.
        playerNameLabel = createLabel(currentPlayerNames.toString(), Color.BLACK, 24);

        // A button to start the game.
        var startButton = getButton(20, "Start Game", () -> {
            StartGameMessage startMessage = new StartGameMessage();
            startMessage.setLobbyId(lobbyId);
            ServerConnection.getInstance().getClient().sendTCP(startMessage);
        });

        // A button to exit the lobby.
        var leaveButton = getButton(20, "Exit the Lobby", () -> {
            game.setScreen(new TitleScreen(game));
            LobbyExitMessage exitMessage = new LobbyExitMessage();
            exitMessage.setLobbyId(lobbyId);
            ServerConnection.getInstance().getClient().sendTCP(exitMessage);
        });

        // Positioning the buttons.
        var table = new Table();
        table.setFillParent(true);
        table.add(lobbyCodeLabel).padBottom(20);
        table.row();
        table.add(playerCountLabel);
        table.row();
        table.add(playerNameLabel).padBottom(20);
        table.row();
        table.add(startButton).padBottom(20);
        table.row();
        table.add(leaveButton);

        stage.addActor(table);

        // Creating and positioning the chat table.
        chatTable = new ChatTable(lobbyId, username);
        chatTable.setSize(355, 230);
        stage.addActor(chatTable);
        chatTable.setPosition(10, 10);
    }

    @Override
    public void show() {
        ServerConnection connection = ServerConnection.getInstance();
        connection.setLobbyUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                updatePlayerCount(msg.getPlayerCount());
                updatePlayerNames(msg.getUsernames());
            }
        });
        connection.setStartGameListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                game.setScreen(new GameScreen(game, lobbyId, username));
                this.dispose();
            }
        });
        connection.setLobbyExitListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                game.setScreen(new TitleScreen(game));
                this.dispose();
            }
        });
        connection.setChatUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    System.out.println("[UI] Updating ChatTable with " + msg.getMessages().size() + " messages.");
                    chatTable.updateMessages(msg.getMessages());
                });
            }
        });
    }

    public void updatePlayerCount(int playerCount) {
        currentPlayerCount = playerCount;
        playerCountLabel.setText("Players: " + currentPlayerCount);
        Log.info("Player count updated to " + currentPlayerCount);
    }

    public void updatePlayerNames(List<String> playerNames) {
        currentPlayerNames = playerNames;
        playerNameLabel.setText(currentPlayerNames.toString());
        Log.info("Player names updated to " + currentPlayerNames);
    }

    @Override
    public void hide() {
        ServerConnection.getInstance().setLobbyUpdateListener(null);
        ServerConnection.getInstance().setStartGameListener(null);
        ServerConnection.getInstance().setLobbyExitListener(null);
        ServerConnection.getInstance().setChatUpdateListener(null);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
    }
}
