package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.component.ChatTable;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.gamescreen.GameScreen;
import ee.kool.saaremeister.screen.overlay.TutorialOverlay;
import ee.kool.saaremeister.utility.AudioManager;
import ee.kool.saaremeister.utility.SoundEffectManager;
import ee.kool.saaremeister.utility.Ui;
import lombok.Getter;
import message.LobbyExitMessage;
import message.StartGameMessage;

import java.util.List;
import java.util.Map;

/**
 * The lobby screen where players meet before joining a game and can use the text-based chat.
 */
public class LobbyScreen extends ScreenAdapter {

    private final Stage stage;
    private final Game game;
    private final Skin skin;
    @Getter
    private final int lobbyId;
    private Label playerCountLabel;
    @Getter
    private int currentPlayerCount;
    private Label playerNameLabel;
    private Label leaderboardLabel;
    @Getter
    private List<String> currentPlayerNames;
    private ChatTable chatTable;
    @Getter
    private final String username;
    private Map<String, Integer> winsByUsername;
    private final Texture background;
    private final SpriteBatch spriteBatch;

    public LobbyScreen(Game game, int lobbyId, int initialPlayerCount, List<String> usernames, String username) {
        this.game = game;
        this.skin = ((Main) game).getSkin();
        this.lobbyId = lobbyId;
        this.currentPlayerCount = initialPlayerCount;
        this.currentPlayerNames = usernames;
        this.username = username;

        AudioManager.playMusic("assets/music/lobby_retro.wav", true);

        background = new Texture("assets/background/water.png");
        spriteBatch = new SpriteBatch();

        stage = new Stage(Ui.createViewport());
        Gdx.input.setInputProcessor(stage);
        createUi();
    }

    /**
     * LobbyScreen constructor for the game screen when at least 1 round has been played.
     */
    public LobbyScreen(Game game, int lobbyId, int initialPlayerCount, List<String> usernames, String username,
                       Map<String, Integer> winsByUsername) {
        this.game = game;
        this.skin = ((Main) game).getSkin();
        this.lobbyId = lobbyId;
        this.currentPlayerCount = initialPlayerCount;
        this.currentPlayerNames = usernames;
        this.username = username;
        this.winsByUsername = winsByUsername;

        background = new Texture("assets/background/map.png");
        spriteBatch = new SpriteBatch();

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

        // A label that displays the usernames currently joined in the lobby.
        playerNameLabel = new Label(currentPlayerNames.toString(), skin);

        // Leaderboard
        Label leaderboardLabelTitle = new Label("Edetabel:", skin);
        leaderboardLabel = new Label(formatLeaderboard(), skin);

        // A button to start the game.
        TextButton startButton = new TextButton("Mine saarele", skin);
        SoundEffectManager.bindMenuClick(startButton);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                StartGameMessage startMessage = new StartGameMessage();
                startMessage.setLobbyId(lobbyId);
                ServerConnection.getInstance().getClient().sendTCP(startMessage);
            }
        });

        // A button to exit the lobby.
        TextButton leaveButton = new TextButton("Lahku lobbyst", skin);
        SoundEffectManager.bindMenuClick(leaveButton);
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.postRunnable(() -> game.setScreen(new TitleScreen(game)));
                LobbyExitMessage exitMessage = new LobbyExitMessage();
                exitMessage.setLobbyId(lobbyId);
                ServerConnection.getInstance().getClient().sendTCP(exitMessage);
            }
        });

        var tutorial = new TutorialOverlay(skin);

        // Tutorial button
        TextButton helpButton = new TextButton("Juhend", skin);
        SoundEffectManager.bindMenuClick(helpButton);
        helpButton.addListener(event -> {
            if (!helpButton.isPressed()) {
                return false;
            }
            tutorial.setVisible(true);
            tutorial.toFront();
            return true;
        });

        // Settings button
        TextButton settingsButton = new TextButton("Settings", skin);
        SoundEffectManager.bindMenuClick(settingsButton);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new SettingsScreen(
                            game,
                            skin,
                            LobbyScreen.this,
                            SettingsScreen.SettingsMode.FULL
                    )
                    );
                });
            }
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
        table.add(leaderboardLabelTitle);
        table.row();
        table.add(leaderboardLabel).padBottom(20);
        table.row();
        table.add(startButton).padBottom(20);
        table.row();
        table.add(helpButton).padBottom(20);
        table.row();
        table.add(settingsButton).padBottom(20);
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
                    updateLeaderboard(msg.getWinsByUsername());
                });
            }
        });
        connection.replayCachedLobbyUpdates(lobbyId);

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
            playerCountLabel.setText("Osalejad: " + currentPlayerCount);
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

    public void updateLeaderboard(Map<String, Integer> winsByUsername) {
        this.winsByUsername = winsByUsername;
        leaderboardLabel.setText(formatLeaderboard());
    }

    private String formatLeaderboard() {
        if (winsByUsername == null || winsByUsername.isEmpty()) {
            return "-";
        }
        return winsByUsername.toString();
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
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        spriteBatch.enableBlending();
        spriteBatch.setColor(1, 1, 1, 0.5f);
        spriteBatch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.setColor(1, 1, 1, 1f);
        spriteBatch.end();

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
        background.dispose();
        spriteBatch.dispose();
        stage.dispose();
    }
}
