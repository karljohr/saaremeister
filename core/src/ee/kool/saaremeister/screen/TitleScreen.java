package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.overlay.TutorialOverlay;

import static ee.kool.saaremeister.component.ButtonComponent.getButton;
import static ee.kool.saaremeister.component.LabelComponent.createLabel;
import static ee.kool.saaremeister.component.TextFieldComponent.getTextField;

public class TitleScreen extends ScreenAdapter {
    private final Game game;
    private final Stage stage;
    private int pendingLobbyId = 0;
    private final TextField nameField;
    private final Label createUsernameLabel;

    /**
     * TitleScreen represents the main menu of the game, where players can choose to start the game or exit.
     * It listens for user input and directs the user to different screens, for example, TitleScreen -> GameScreen.
     */
    public TitleScreen(Game game) {
        this.game = game;
        stage = new Stage();

        // Creates a text field to enter or change the username.
        nameField = getTextField(20);
        createUsernameLabel = createLabel("Enter your username:", Color.BLACK, 20);

        // menu buttons
        var startButton = getButton(20, "Join a Lobby", () -> {
            // Send a message to the server that the player wants to join a lobby.
            tryJoiningLobby(1);
        });
        var exitButton = getButton(20, "Exit", () -> Gdx.app.exit());

        var tutorial = new TutorialOverlay();

        // Tutorial button
        var helpButton = getButton(20, "Juhend", () -> {
            tutorial.setVisible(true);
            tutorial.toFront(); // Brings window on top
        });

        // positioning the buttons. you can think of the following as a table (or flexbox) in HTML
        var table = new Table();
        table.setFillParent(true);
        table.add(createUsernameLabel).padBottom(20);
        table.row();
        table.add(nameField).padBottom(20);
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
                    game.setScreen(new LobbyScreen(game, lobbyId, msg.getPlayerCount(), msg.getUsernames(), username));
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
