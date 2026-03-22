package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.kool.saaremeister.component.ChatTable;
import ee.kool.saaremeister.component.LabelComponent;
import lombok.Getter;
import message.GameStateMessage;

import java.util.List;

public class Hud implements Disposable {

    @Getter
    private final Stage stage;
    private final Label moneyLabel;
    private final Label livesLabel;
    private final Texture coinTexture;
    private final ChatTable chatTable;

    public Hud(SpriteBatch spriteBatch, int lobbyId, String username) {
        Viewport viewport = new FitViewport(800, 600); // Fikseeritud suurus HUD-i jaoks
        stage = new Stage(viewport, spriteBatch);

        coinTexture = new Texture(Gdx.files.internal("goldCoin.png"));
        Image moneyIcon = new Image(coinTexture);

        // Kasutame olemasolevat LabelComponenti
        moneyLabel = LabelComponent.createLabel("0", Color.GOLD, 20);
        livesLabel = LabelComponent.createLabel("Lives: 3", Color.RED, 20);

        Table table = new Table();
        table.setFillParent(true);
        table.setTouchable(Touchable.childrenOnly);
        table.top().left();

        // Üks veerg: elud ja mündid sama vasaku servaga (mitte 2 veergu, mis nihutas münti paremale)
        Table topLeft = new Table();
        topLeft.top().left();
        float edge = 10f;
        topLeft.add(livesLabel).left().padTop(10).padLeft(edge).row();
        Table moneyRow = new Table();
        moneyRow.left();
        moneyRow.add(moneyIcon).size(24, 24);
        moneyRow.add(moneyLabel).left().padLeft(4);
        topLeft.add(moneyRow).left().padTop(5).padLeft(edge).row();

        table.add(topLeft).colspan(2).left().top().row();
        chatTable = new ChatTable(lobbyId, username);
        table.add(chatTable).colspan(2).padTop(10).padRight(10).right();

        stage.addActor(table);
    }

    /**
     * Updates the HUD with the given GameStateMessage.
     * @param msg
     * @param myId
     */
    public void update(GameStateMessage msg, int myId) {
        msg.getPlayerStates().stream()
                .filter(p -> p.getId() == myId)
                .findFirst()
                .ifPresent(me -> {
                    moneyLabel.setText(String.valueOf(me.getMoney()));
                    livesLabel.setText("Lives: " + me.getLives());
                });
    }

    /**
     * Updates the HUD chat.
     */
    public void updateChat(List<String> messages) {
        chatTable.updateMessages(messages);
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        coinTexture.dispose();
    }
}
