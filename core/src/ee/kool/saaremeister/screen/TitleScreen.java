package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.TutorialOverlay;

public class TitleScreen extends ScreenAdapter {

    private final Game game;
    private final Stage stage;
    private int pendingLobbyId = 0;
    private final TextField nameField;

    /**
     * TitleScreen represents the main menu of the game, where players can choose to start the game or exit.
     * It listens for user input and directs the user to different screens, for example, TitleScreen -> GameScreen.
     */
    public TitleScreen(Game game) {
        this.game = game;
        Skin skin = ((Main) game).getSkin();
        stage = new Stage();

        // NB! an important line - will make the stage listen for user input,
        // For example, when this is not set, no hover or click events will be triggered
        Gdx.input.setInputProcessor(stage);

        // Game title label
        Label titleLabel = new Label("Saaremeister", skin, "title");

        // Creates a text field to enter or change the username.
        TextField.TextFieldStyle inputStyle =
                new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        inputStyle.font = skin.getFont("button");
        nameField = new TextField("", inputStyle);
        Label createUsernameLabel = new Label("Sisesta oma nimi:", skin, "big");

        // menu buttons
        TextButton startButton = new TextButton("Liitu lobbysse", skin);
        startButton.addListener(event -> {
            if (!startButton.isPressed()) {
                return false;
            }
            tryJoiningLobby(1);
            return true;
        });

        TextButton exitButton = new TextButton("Lahku", skin);
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
        helpButton.addListener(event -> {
            if (!helpButton.isPressed()) {
                return false;
            }
            tutorial.setVisible(true);
            tutorial.toFront(); // Brings the window on top
            return true;
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
        table.add(exitButton).padBottom(20);

        stage.addActor(table);
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
        // clear the screen
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1); // to get that win 95 look
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw the buttons
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
