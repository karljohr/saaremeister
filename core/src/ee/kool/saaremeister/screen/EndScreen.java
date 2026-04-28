package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ee.kool.saaremeister.Main;
import ee.kool.saaremeister.network.ServerConnection;
import lombok.Getter;
import lombok.Setter;
import message.LobbyExitMessage;
import message.dto.PlayerRole;

import java.util.ArrayList;

/**
 * Shows in full screen, whether the player won or lost.
 */
public class EndScreen extends ScreenAdapter {

    private final Game game;
    private final Stage stage;
    private final Skin skin;
    @Getter @Setter
    private int gameWinnerRole;
    private final boolean finalRound;
    private final int lobbyId;
    private final String username;

    public EndScreen(Game game, PlayerRole gameWinnerRole, PlayerRole myRole, boolean finalRound, int lobbyId,
                     String username, int currentRound, int totalRounds) {
        this.game = game;
        this.stage = new Stage();
        this.skin = ((Main) game).getSkin();
        this.finalRound = finalRound;
        this.lobbyId = lobbyId;
        this.username = username;

        // Will make the stage listen for user input.
        Gdx.input.setInputProcessor(stage);

        String resultMessage = "Sa " + (gameWinnerRole != myRole ? "kaotasid!" : "v6itsid!");
        Label resultLabel = new Label(resultMessage, skin, "title");

        Label roundLabel = new Label("Raund " + currentRound + "/" + totalRounds, skin, "big");
        String exitInstruction = finalRound
                ? "Vajuta ESC, et minna tagasi avaekraanile"
                : "Vajuta ESC, et minna tagasi lobbyisse";
        Label instructionLabel = new Label(exitInstruction, skin, "big");

        var table = new Table();
        table.setFillParent(true);
        table.add(resultLabel).padTop(100).center();
        table.row();
        table.add(roundLabel).padTop(20).center();
        table.row();
        table.add(instructionLabel).padTop(20).center();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // clear the screen
        Gdx.gl.glClearColor(192 / 255f, 192 / 255f, 192 / 255f, 1); // to get that win 95 look
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw the buttons
        stage.act(delta);
        stage.draw();

        // Exit the game if the user presses ESC, depending on whether this is the final round or not.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (finalRound) {
                LobbyExitMessage exitMessage = new LobbyExitMessage();
                exitMessage.setLobbyId(lobbyId);
                ServerConnection.getInstance().getClient().sendTCP(exitMessage);
                game.setScreen(new TitleScreen(game));
            } else {
                game.setScreen(new LobbyScreen(game, lobbyId, 0, new ArrayList<>(), username));
            }
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
        stage.dispose();
    }
}
