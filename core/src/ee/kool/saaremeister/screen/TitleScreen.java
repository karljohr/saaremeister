package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.network.ServerConnection;
import message.GameJoinMessage;
import message.LobbyUpdateMessage;

import static ee.kool.saaremeister.component.ButtonComponent.getButton;

public class TitleScreen extends ScreenAdapter {
    private final Game game;
    private final Stage stage;
    private final int lobbyId = 1;
    private int pendingLobbyId = 0;

    /**
     * TitleScreen represents the main menu of the game, where players can choose to start the game or exit.
     * It listens for user input and directs user to different screens, for example TitleScreen -> GameScreen.
     */
    public TitleScreen(Game game) {
        this.game = game;
        stage = new Stage();

        // NB! an important line - will make the stage listen for user input,
        // For example, when this is not set no hover or click events will be triggered
        Gdx.input.setInputProcessor(stage);

        // menu buttons
        var startButton = getButton(20, "Join a Lobby", () -> {
            // send a message to the server that the player wants to join a lobby
            tryJoiningLobby(lobbyId);
        });
        var exitButton = getButton(20, "Exit", () -> Gdx.app.exit());

        // positioning the buttons. you can think of the following as a table (or flexbox) in HTML
        var table = new Table();
        table.setFillParent(true);
        table.add(startButton).padBottom(20);
        table.row();
        table.add(exitButton);

        stage.addActor(table);
    }

    private void tryJoiningLobby(int lobbyId) {
        if (pendingLobbyId != 0) {
            return;
        }

        pendingLobbyId = lobbyId;
        Log.info("Attempting to join lobby " + lobbyId);
        ServerConnection.getInstance().joinLobby(lobbyId, msg -> {
            if (msg.getPlayerCount() >= 0) {
                game.setScreen(new LobbyScreen(game, lobbyId, msg.getPlayerCount()));
                this.dispose();
            } else {
                Log.info("Failed to join lobby " + lobbyId);
                pendingLobbyId = 0;
            }
        });
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
