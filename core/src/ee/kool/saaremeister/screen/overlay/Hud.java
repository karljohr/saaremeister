package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
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

    public Hud(SpriteBatch spriteBatch, int lobbyId, String username, Skin skin) {
        stage = new Stage(new ScreenViewport(), spriteBatch);

        coinTexture = new Texture(Gdx.files.internal("goldCoin.png"));
        Image moneyIcon = new Image(coinTexture);

        // Kasutame olemasolevat LabelComponenti
        moneyLabel = LabelComponent.createLabel("0", Color.GOLD, 20);
        livesLabel = LabelComponent.createLabel("Elud: 3", Color.RED, 20);

        // HUD table for the stats of the player.
        Table statsTable = new Table();
        statsTable.setFillParent(true);
        statsTable.setTouchable(Touchable.childrenOnly);
        statsTable.top().left();

        // Add money icon and label on the same row.
        statsTable.add(moneyIcon).size(64, 64).padTop(10).padLeft(5).left();
        statsTable.add(moneyLabel).padTop(10).padLeft(1).left();
        moneyLabel.setFontScale(2f);

        // A row for displaying lives.
        statsTable.row();
        statsTable.add(livesLabel).padTop(5).padLeft(10).left();
        livesLabel.setFontScale(2f);

        // HUD table for the chat.
        chatTable = new ChatTable(lobbyId, username, skin);
        Table chatContainer = new Table();
        chatContainer.setFillParent(true);
        chatContainer.bottom().right();
        chatContainer.add(chatTable).colspan(2).padTop(10).padRight(10).right();

        stage.addActor(statsTable);
        stage.addActor(chatContainer);
    }

    /**
     * Updates the HUD with the given GameStateMessage.
     */
    public void update(GameStateMessage msg, int myId) {
        msg.getPlayerStates().stream()
                .filter(p -> p.getId() == myId)
                .findFirst()
                .ifPresent(me -> {
                    moneyLabel.setText(String.valueOf(me.getMoney()));
                    livesLabel.setText("Elud: " + me.getLives());
                });
    }

    /**
     * Updates the HUD chat.
     */
    public void updateChat(List<String> messages) {
        chatTable.updateMessages(messages);
    }

    /**
     * Returns true if the chat input is focused.
     */
    public boolean isChatInputFocused() {
        return chatTable.isInputFocused();
    }

    /**
     * Clears the chat input focus.
     */
    public void clearChatInputFocus() {
        chatTable.clearInputFocus();
    }

    /**
     * Checks if the user clicked inside the chat input.
     */
    public boolean clickedInsideChatInput(Actor target) {
        return chatTable.clickedInsideInput(target);
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
