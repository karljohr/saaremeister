package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.kool.saaremeister.component.LabelComponent;
import message.GameStateMessage;

public class Hud implements Disposable {
    private final Stage stage;
    private final Label moneyLabel;
    private final Label livesLabel;
    private final Texture coinTexture;

    public Hud(SpriteBatch spriteBatch) {
        Viewport viewport = new FitViewport(800, 600); // Fikseeritud suurus HUD-i jaoks
        stage = new Stage(viewport, spriteBatch);

        coinTexture = new Texture(Gdx.files.internal("goldCoin.png"));
        Image moneyIcon = new Image(coinTexture);

        // Kasutame olemasolevat LabelComponenti
        moneyLabel = LabelComponent.createLabel("0", Color.GOLD, 20);
        livesLabel = LabelComponent.createLabel("Lives: 3", Color.RED, 20);

        Table table = new Table();
        table.top().left();
        table.setFillParent(true);

        // Lisa raha ikoon ja tekst samale reale
        table.add(moneyIcon).size(24, 24).padTop(10).padLeft(10);
        table.add(moneyLabel).padTop(10).padLeft(5).left();

        // Liigume uuele reale elude jaoks
        table.row();
        table.add(livesLabel).colspan(2).padTop(5).padLeft(10).left();

        stage.addActor(table);
    }

    public void update(GameStateMessage msg, int myId) {
        msg.getPlayerStates().stream()
                .filter(p -> p.getId() == myId)
                .findFirst()
                .ifPresent(me -> {
                    moneyLabel.setText(String.valueOf(me.getMoney()));
                    livesLabel.setText("Lives: " + me.getLives());
                });
    }

    public void render() {
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        coinTexture.dispose();
    }
}
