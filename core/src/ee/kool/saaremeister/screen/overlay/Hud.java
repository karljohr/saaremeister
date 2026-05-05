package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ee.kool.saaremeister.component.ChatTable;
import ee.kool.saaremeister.component.Hotbar;
import ee.kool.saaremeister.component.LabelComponent;
import ee.kool.saaremeister.screen.overlay.Components.*;
import lombok.Getter;
import message.GameStateMessage;
import message.dto.PlayerRole;

import java.util.List;

/**
 * HUD (Heads-Up Display) for the game stats, player stats, and chat.
 */
public class Hud implements Disposable {

    @Getter
    private final Stage stage;

    private final ChatTable chatTable;
    @Getter
    private final Hotbar hotbar;
    private final Table buildLegend;

    private final HealthUI healthUI;
    private final ArmorUI armorUI;
    private final MoneyUI moneyUI;
    private final TimerUI timerUI;
    private final ZonesUI zonesUI;
    private final DamageOverlay damageOverlay;


    public Hud(SpriteBatch spriteBatch, int lobbyId, String username, Skin skin) {
        stage = new Stage(new ScreenViewport(), spriteBatch);

        healthUI = new HealthUI(skin);
        armorUI = new ArmorUI(skin);
        moneyUI = new MoneyUI();
        timerUI = new TimerUI();
        zonesUI = new ZonesUI(skin);

        damageOverlay = new DamageOverlay();

        // HUD table for the stats of the player.
        Table statsTable = new Table();
        statsTable.setFillParent(true);
        statsTable.setTouchable(Touchable.childrenOnly);
        statsTable.bottom().left();
        statsTable.padLeft(10);
        statsTable.padBottom(10);

        // A row for displaying lives.
        statsTable.add(healthUI.getActor()).left();

        // A row for displaying defense.
        statsTable.row();
        statsTable.add(armorUI.getActor()).left();

        // Add money icon and label on the same row.
        statsTable.row();
        statsTable.add(moneyUI.getActor()).left();

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
        buildLegend.center().left();

        Table panel = new Table(skin);
        Drawable panelBg = skin.newDrawable("white", new Color(0, 0, 0, 0.3f));
        panel.background(panelBg);
        panel.defaults().left().pad(10);
        panel.add(LabelComponent.createLabel("Build mode", Color.WHITE, 24)).row();
        panel.add(LabelComponent.createLabel("LMB  Place", Color.LIGHT_GRAY, 20)).row();
        panel.add(LabelComponent.createLabel("T    Next variation", Color.LIGHT_GRAY, 20)).row();
        panel.add(LabelComponent.createLabel("RMB  Cancel", Color.LIGHT_GRAY, 20)).row();

        buildLegend.add(panel).padLeft(16).padBottom(16);
        buildLegend.setVisible(false);
        stage.addActor(buildLegend);

        stage.addActor(damageOverlay.getActor());
        stage.addActor(timerUI.getActor());
        stage.addActor(statsTable);
        stage.addActor(chatContainer);
        stage.addActor(hotbarContainer);
        stage.addActor(zonesUI.getActor());
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
    public void update(GameStateMessage msg, int myId, PlayerRole myRole) {
        // Updates the player's money and lives. Lives are set to 0 if the player is currently not in the game (dead).
        var me = msg.getPlayerStates().stream()
                .filter(p -> p.getId() == myId)
                .findFirst();
        if (me.isPresent()) {
            timerUI.setPlayerRole(myRole);
            zonesUI.setPlayerRole(myRole);

            zonesUI.update(msg);
            timerUI.update(msg);
            healthUI.update(me.get());
            armorUI.update(me.get());
            moneyUI.update(me.get());
            damageOverlay.checkDamage(me.get().getLives());

        } else {
            healthUI.update();
            armorUI.update();
            damageOverlay.checkDamage(0);
        }
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
        timerUI.updatePulseTimer(delta);
        damageOverlay.update(delta);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        moneyUI.dispose();
    }
}
