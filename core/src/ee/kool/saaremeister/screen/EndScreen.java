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
import lombok.Getter;
import lombok.Setter;

/**
 * Shows in full screen, whether the player won or lost.
 */
public class EndScreen extends ScreenAdapter {

    private final Game game;
    private final Stage stage;
    private final Skin skin;
    @Getter @Setter
    private int gameWinnerId;

    public EndScreen(Game game, int gameWinnerId, int myId) {
        this.game = game;
        this.stage = new Stage();
        this.skin = ((Main) game).getSkin();

        // Will make the stage listen for user input.
        Gdx.input.setInputProcessor(stage);

        String resultMessage = "Sa " + (gameWinnerId != myId ? "kaotasid!" : "v6itsid!");
        Label resultLabel = new Label(resultMessage, skin, "title");

        Label instructionLabel = new Label("Vajuta ESC, et minna tagasi avaekraanile", skin, "big");

        var table = new Table();
        table.setFillParent(true);
        table.add(resultLabel).padTop(100).center();
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new TitleScreen(game));
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
