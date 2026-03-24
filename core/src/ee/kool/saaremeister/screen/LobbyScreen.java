package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.component.ChatTable;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.TutorialOverlay;
import message.LobbyExitMessage;
import message.StartGameMessage;

import java.util.List;

public class LobbyScreen extends ScreenAdapter {

    private final Stage stage;
    private final Game game;
    private final Skin skin;
    private final int lobbyId;
    private Label playerCountLabel;
    private int currentPlayerCount;
    private Label playerNameLabel;
    private List<String> currentPlayerNames;
    private ChatTable chatTable;
    private final String username;

    public LobbyScreen(Game game, int lobbyId, int initialPlayerCount, List<String> usernames, String username) {
        this.game = game;
        this.skin = ((Main) game).getSkin();
        this.lobbyId = lobbyId;
        this.currentPlayerCount = initialPlayerCount;
        this.currentPlayerNames = usernames;
        this.username = username;

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        createUi();
    }

    /**
     * Creates the UI in the lobby screen.
     */
    private void createUi() {
        Label lobbyCodeLabel = new Label("Lobby " + lobbyId, skin, "title");

        playerCountLabel = new Label("Osalejad: " + currentPlayerCount, skin);
        playerCountLabel.setFontScale(2f);

        // A label that displays the usernames currently joined in the lobby.
        playerNameLabel = new Label(currentPlayerNames.toString(), skin, "alt");
        playerNameLabel.setFontScale(2f);

        // A button to start the game.
        TextButton startButton = new TextButton("Mine saarele", skin);
        startButton.addListener(event -> {
            if (!startButton.isPressed()) {
                return false;
            }
            StartGameMessage startMessage = new StartGameMessage();
            startMessage.setLobbyId(lobbyId);
            ServerConnection.getInstance().getClient().sendTCP(startMessage);
            return true;
        });

        // A button to exit the lobby.
        TextButton leaveButton = new TextButton("Lahku lobbyst", skin);
        leaveButton.addListener(event -> {
            if (!leaveButton.isPressed()) {
                return false;
            }
            Gdx.app.postRunnable(() -> game.setScreen(new TitleScreen(game)));
            LobbyExitMessage exitMessage = new LobbyExitMessage();
            exitMessage.setLobbyId(lobbyId);
            ServerConnection.getInstance().getClient().sendTCP(exitMessage);
            return true;
        });

        var tutorial = new TutorialOverlay(skin);

        // Create tutorial button
        TextButton helpButton = new TextButton("Juhend", skin);
        helpButton.addListener(event -> {
            if (!helpButton.isPressed()) {
                return false;
            }
            tutorial.setVisible(true);
            tutorial.toFront();
            return true;
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
        table.add(helpButton).padBottom(20);
        table.row();
        table.add(leaveButton);

        stage.addActor(table);
        stage.addActor(tutorial);

        // Creating and positioning the chat table.
        chatTable = new ChatTable(lobbyId, username, skin);
        chatTable.pack();
        chatTable.setPosition(10, 10);
        stage.addActor(chatTable);
    }

    /**
     * Sets up the listeners for lobby updates and starts the game.
     */
    @Override
    public void show() {
        ServerConnection connection = ServerConnection.getInstance();

        connection.setChatUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    System.out.println("[UI] Updating ChatTable with " + msg.getMessages().size() + " messages.");
                    chatTable.updateMessages(msg.getMessages());
                });
            }
        });

        connection.replayCachedChatUpdates(lobbyId);

        connection.setLobbyUpdateListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    updatePlayerCount(msg.getPlayerCount());
                    updatePlayerNames(msg.getUsernames());
                });
            }
        });
        connection.setStartGameListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new GameScreen(game, lobbyId, username));
                    this.dispose();
                });
            }
        });
        connection.setLobbyExitListener(msg -> {
            if (msg.getLobbyId() == lobbyId) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new TitleScreen(game));
                    this.dispose();
                });
            }
        });
    }

    /**
     * Updates the player count label so that it displays the current player count in the lobby.
     */
    public void updatePlayerCount(int playerCount) {
        currentPlayerCount = playerCount;
        Gdx.app.postRunnable(() -> {
            playerCountLabel.setText("Players: " + currentPlayerCount);
        });
        Log.info("Player count updated to " + currentPlayerCount);
    }

    /**
     * Updates the player names label so that it displays the current player names in the lobby.
     */
    public void updatePlayerNames(List<String> playerNames) {
        currentPlayerNames = playerNames;
        playerNameLabel.setText(currentPlayerNames.toString());
        Log.info("Player names updated to " + currentPlayerNames);
    }

    /**
     * Removes the listeners for lobby updates.
     */
    @Override
    public void hide() {
        ServerConnection.getInstance().setLobbyUpdateListener(null);
        ServerConnection.getInstance().setStartGameListener(null);
        ServerConnection.getInstance().setLobbyExitListener(null);
        ServerConnection.getInstance().setChatUpdateListener(null);
    }

    /**
     * Renders the lobby screen.
     */
    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    /**
     * Resizes the stage when the window is resized.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of the stage and other resources when the lobby screen is disposed.
     */
    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
    }
}
