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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ee.kool.saaremeister.component.ChatTable;
import ee.kool.saaremeister.component.Hotbar;
import ee.kool.saaremeister.component.LabelComponent;
import lombok.Getter;
import message.GameStateMessage;

import java.util.List;

import static constant.Constants.MATCH_DURATION_SECONDS;

/**
 * HUD (Heads-Up Display) for the game stats, player stats, and chat.
 */
public class Hud implements Disposable {

    @Getter
    private final Stage stage;
    private final Label moneyLabel;
    private final Label livesLabel;
    private final Label defenseLabel;
    private final Texture coinTexture;
    private final ChatTable chatTable;
    private final Label timerLabel;
    private final Label zonesLabel;
    @Getter
    private final Hotbar hotbar;
    private final Table buildLegend;

    public Hud(SpriteBatch spriteBatch, int lobbyId, String username, Skin skin) {
        stage = new Stage(new ScreenViewport(), spriteBatch);

        // HUD elements for the player stats.
        coinTexture = new Texture(Gdx.files.internal("goldCoin.png"));
        Image moneyIcon = new Image(coinTexture);
        moneyLabel = LabelComponent.createLabel("0", Color.GOLD, 20);
        livesLabel = LabelComponent.createLabel("Elud: 3", Color.RED, 20);
        defenseLabel = LabelComponent.createLabel("Kaitse: 0", Color.WHITE, 20);

        // HUD elements for the zone capturing.
        timerLabel = LabelComponent.createLabel("Aeg: 0", Color.WHITE, 20);
        zonesLabel = LabelComponent.createLabel("Tsoonid: 0", Color.CYAN, 20);

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

        // A row for displaying defense.
        statsTable.row();
        statsTable.add(defenseLabel).padTop(5).padLeft(10).left();
        defenseLabel.setFontScale(2f);

        // A row for displaying game timer.
        statsTable.row();
        statsTable.add(timerLabel).padTop(5).padLeft(10).left();
        timerLabel.setFontScale(2f);

        // A row for displaying captured zones.
        statsTable.row();
        statsTable.add(zonesLabel).padTop(5).padLeft(10).left();
        zonesLabel.setFontScale(2f);

        // HUD table for the chat.
        chatTable = new ChatTable(lobbyId, username, skin);
        Table chatContainer = new Table();
        chatContainer.setFillParent(true);
        chatContainer.bottom().right();
        chatContainer.add(chatTable).colspan(2).padTop(10).padRight(10).right();

        // Hotbar
        hotbar = new Hotbar(skin);
        Table hotbarContainer = new Table();
        hotbarContainer.setFillParent(true);
        hotbarContainer.setTouchable(Touchable.childrenOnly); // So hotbar doesn't block mouse clicks around itself.

        hotbarContainer.bottom();

        hotbarContainer.add(hotbar)
                .expandX()
                .center()
                .padBottom(20);

        // BuildMode Legend
        buildLegend = new Table();
        buildLegend.setFillParent(true);
        buildLegend.bottom().left();

        Table panel = new Table(skin);
        panel.defaults().left().pad(4);
        panel.add(LabelComponent.createLabel("Build mode", Color.WHITE, 24)).row();
        panel.add(LabelComponent.createLabel("LMB  Place", Color.LIGHT_GRAY, 20)).row();
        panel.add(LabelComponent.createLabel("T    Next variation", Color.LIGHT_GRAY, 20)).row();
        panel.add(LabelComponent.createLabel("RMB  Cancel", Color.LIGHT_GRAY, 20)).row();

        buildLegend.add(panel).padLeft(16).padBottom(16);
        buildLegend.setVisible(false);
        stage.addActor(buildLegend);

        stage.addActor(statsTable);
        stage.addActor(chatContainer);
        stage.addActor(hotbarContainer);
    }

    public void selectHotbarSlot(int index) {
        hotbar.setSelection(index);
    }

    public void scrollHotbar(int direction) {
        int newIndex = hotbar.getSelectedIndex() - direction;
        hotbar.setSelection(newIndex);
    }

    /**
     * Updates the HUD with the given GameStateMessage.
     */
    public void update(GameStateMessage msg, int myId) {
        // Updates the player's money and lives. Lives are set to 0 if the player is currently not in the game (dead).
        var me = msg.getPlayerStates().stream()
                .filter(p -> p.getId() == myId)
                .findFirst();
        if (me.isPresent()) {
            moneyLabel.setText(String.valueOf(me.get().getMoney()));
            livesLabel.setText("Elud: " + me.get().getLives());
            defenseLabel.setText("Kaitse: " + me.get().getDefense());
        } else {
            livesLabel.setText("Elud: 0");
            defenseLabel.setText("Kaitse: 0");
        }

        // Updates the game timer.
        timerLabel.setText("Aeg: " + ((int) (MATCH_DURATION_SECONDS - msg.getGameTime())));

        // Updates the player's captured zones.
        int ownedZones = 0;
        if (msg.getCaptureZones() != null) {
            ownedZones = (int) msg.getCaptureZones().stream()
                    .filter(zone -> zone.getOwnerPlayerId() == myId)
                    .count();
        }
        zonesLabel.setText("Tsoonid: " + ownedZones + "/4");
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

    public void changeBuildLegendVisibility() {
        buildLegend.setVisible(!buildLegend.isVisible());
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
