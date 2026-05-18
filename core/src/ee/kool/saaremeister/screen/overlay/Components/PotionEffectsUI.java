package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import ee.kool.saaremeister.component.LabelComponent;
import message.dto.PlayerState;

/**
 * Displays active speed and strength potion durations in the HUD.
 */
public class PotionEffectsUI implements UIComponent<PlayerState>, Disposable {

    private static final Color TEXT_COLOR = Color.WHITE;

    private final Table root;
    private final Table speedRow;
    private final Table strengthRow;
    private final Label speedDurationLabel;
    private final Label strengthDurationLabel;
    private final Texture speedTexture;
    private final Texture strengthTexture;

    /**
     * Creates the HUD rows used for active potion effects.
     */
    public PotionEffectsUI() {
        root = new Table();
        root.left();

        speedTexture = new Texture(Gdx.files.internal("shop/speed_pot.png"));
        strengthTexture = new Texture(Gdx.files.internal("shop/strength_pot.png"));

        speedDurationLabel = LabelComponent.createLabel("", TEXT_COLOR, 20);
        strengthDurationLabel = LabelComponent.createLabel("", TEXT_COLOR, 20);

        speedDurationLabel.setFontScale(2f);
        strengthDurationLabel.setFontScale(2f);

        speedRow = createRow(speedTexture, speedDurationLabel);
        strengthRow = createRow(strengthTexture, strengthDurationLabel);

        root.add(speedRow).left().padBottom(8).row();
        root.add(strengthRow).left();

        speedRow.setVisible(false);
        strengthRow.setVisible(false);
    }

    /**
     * Creates one HUD row containing a potion icon and a duration label.
     */
    private Table createRow(Texture texture, Label durationLabel) {
        Table row = new Table();
        row.left();
        row.add(new Image(texture)).size(44, 44).padLeft(14).padRight(10);
        row.add(durationLabel).left();
        return row;
    }

    /**
     * Updates the potion effect rows using the latest player state.
     */
    @Override
    public void update(PlayerState playerState) {
        int speedRemainingMs = playerState == null ? 0 : playerState.getSpeedPotionRemainingMs();
        int strengthRemainingMs = playerState == null ? 0 : playerState.getStrengthPotionRemainingMs();

        updateRow(speedRow, speedDurationLabel, speedRemainingMs);
        updateRow(strengthRow, strengthDurationLabel, strengthRemainingMs);

        root.setVisible(speedRemainingMs > 0 || strengthRemainingMs > 0);
    }

    /**
     * Shows or hides one potion row and updates its countdown label.
     */
    private void updateRow(Table row, Label label, int remainingMs) {
        boolean visible = remainingMs > 0;
        row.setVisible(visible);
        if (visible) {
            label.setText(formatDurationSeconds(remainingMs));
        }
    }

    /**
     * Formats remaining milliseconds as whole seconds for the HUD.
     */
    private String formatDurationSeconds(int remainingMs) {
        int seconds = Math.max(1, (int) Math.ceil(remainingMs / 1000f));
        return seconds + "s";
    }

    /**
     * Returns the root actor used by the HUD layout.
     */
    @Override
    public Table getActor() {
        return root;
    }

    /**
     * Disposes textures loaded for the potion icons.
     */
    @Override
    public void dispose() {
        speedTexture.dispose();
        strengthTexture.dispose();
    }
}
