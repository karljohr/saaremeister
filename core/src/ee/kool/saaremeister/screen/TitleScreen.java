package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.TutorialOverlay;
import ee.kool.saaremeister.utility.SoundEffectManager;
import ee.kool.saaremeister.utility.Ui;

public class TitleScreen extends ScreenAdapter {

    private final Game game;
    private final Stage stage;
    private int pendingLobbyId = 0;
    private final TextField nameField;
    private final Texture background;
    private final SpriteBatch spriteBatch;
    private final Label connectionStatusLabel;
    private final Label errorLabel;
    private final TextButton startButton;
    private final TextButton retryButton;

    /**
     * TitleScreen represents the main menu of the game, where players can choose to start the game or exit.
     * It listens for user input and directs the user to different screens, for example, TitleScreen -> GameScreen.
     */
    public TitleScreen(Game game) {
        this.game = game;

        background = new Texture("background/map.png");
        spriteBatch = new SpriteBatch();
        Skin skin = ((Main) game).getSkin();
        stage = new Stage(Ui.createViewport());

        // NB! an important line - will make the stage listen for user input,
        // For example, when this is not set, no hover or click events will be triggered
        Gdx.input.setInputProcessor(stage);

        // Game title label
        Label titleLabel = new Label("Saaremeister", skin, "title");

        // Creates a text field to enter or change the username.
        TextField.TextFieldStyle inputStyle =
                new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        inputStyle.font = skin.getFont("font");
        nameField = new TextField("", inputStyle);
        Label createUsernameLabel = new Label("Sisesta oma nimi:", skin, "default");
        connectionStatusLabel = new Label("", skin, "default");
        errorLabel = new Label("", skin, "default");

        // menu buttons
        startButton = new TextButton("Liitu lobbysse", skin);
        SoundEffectManager.bindMenuClick(startButton);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryJoiningLobby(1);
            }
        });

        retryButton = new TextButton("Proovi uuesti", skin);
        SoundEffectManager.bindMenuClick(retryButton);
        retryButton.addListener(event -> {
            if (!retryButton.isPressed()) {
                return false;
            }
            pendingLobbyId = 0;
            ServerConnection.getInstance().connect();
            return true;
        });

        TextButton exitButton = new TextButton("Lahku", skin);
        SoundEffectManager.bindMenuClick(exitButton);
        exitButton.addListener(event -> {
            if (!exitButton.isPressed()) {
                return false;
            }
            Gdx.app.exit();
            return true;
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
            tutorial.toFront(); // Brings the window on top
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
                            TitleScreen.this,
                            SettingsScreen.SettingsMode.FULL
                            )
                    );
                });
            }
        });

        // positioning the buttons. you can think of the following as a table (or flexbox) in HTML
        var table = new Table();
        table.setFillParent(true);
        table.add(titleLabel).padBottom(20);
        table.row();
        table.add(createUsernameLabel).padBottom(20);
        table.row();
        table.add(nameField).width(260).padBottom(20);
        table.row();
        table.add(startButton).padBottom(20);
        table.row();
        table.add(helpButton).padBottom(20);
        table.row();
        table.add(settingsButton).padBottom(20);
        table.row();
        table.add(exitButton).padBottom(20);

        Table connectionTable = new Table();
        connectionTable.setFillParent(true);
        connectionTable.bottom().left();
        connectionTable.pad(10);
        connectionTable.add(connectionStatusLabel).left().row();
        connectionTable.add(errorLabel).left().row();
        connectionTable.add(retryButton).left().padTop(10);

        stage.addActor(table);
        stage.addActor(connectionTable);
        stage.addActor(tutorial);

        // Record the input in the nameField.
        stage.setKeyboardFocus(nameField);
    }

    /**
     * Tries to join the lobby with the given ID. If the lobby is full, it tries again with the next ID.
     */
    private void tryJoiningLobby(int lobbyId) {
        if (pendingLobbyId != 0) {
            return;
        }

        if (ServerConnection.getInstance().getConnectionStatus() != ServerConnection.ConnectionStatus.CONNECTED) {
            return;
        }

        pendingLobbyId = lobbyId;
        String username = nameField.getText().trim();
        Log.info("Attempting to join lobby " + lobbyId + " with username " + username);
        ServerConnection.getInstance().joinLobby(lobbyId, username, msg -> {
            Gdx.app.postRunnable(() -> {
                if (msg.getPlayerCount() >= 0 && !msg.isStarted()) {
                    game.setScreen(new LobbyScreen(game, msg.getLobbyId(), msg.getPlayerCount(), msg.getUsernames(), username));
                    this.dispose();
                } else {
                    Log.info("Failed to join lobby " + lobbyId);
                    pendingLobbyId = 0;
                    tryJoiningLobby(lobbyId + 1);
                }
            });
        });
    }

    @Override
    public void show() {
        // NB! an important line - will make the stage listen for user input,
        // For example, when this is not set, no hover or click events will be triggered
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renders the TitleScreen, clearing it and drawing the buttons.
     *
     * @param delta time since last frame.
     */
    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateConnectionUi();

        spriteBatch.begin();
        spriteBatch.enableBlending();
        spriteBatch.setColor(1, 1, 1, 0.5f);
        spriteBatch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.setColor(1, 1, 1, 1f);
        spriteBatch.end();

        stage.act(delta);
        stage.draw();
    }

    private void updateConnectionUi() {
        ServerConnection connection = ServerConnection.getInstance();
        String statusText = switch (connection.getConnectionStatus()) {
            case CONNECTED -> "Server: uhendatud";
            case CONNECTING -> "Server: uhendamine...";
            case FAILED -> "Server: uhendus ebaonnestus";
            case DISCONNECTED -> "Server: pole uhendatud";
        };

        connectionStatusLabel.setText(statusText);
        String errorMessage = connection.getLastErrorMessage();
        errorLabel.setText(errorMessage == null ? "" : errorMessage);

        boolean connected = connection.getConnectionStatus() == ServerConnection.ConnectionStatus.CONNECTED;
        startButton.setDisabled(!connected);
        retryButton.setVisible(!connected);

        if (!connected) {
            pendingLobbyId = 0;
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
        background.dispose();
        spriteBatch.dispose();
        stage.dispose();
    }
}
