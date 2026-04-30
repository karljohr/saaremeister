package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import ee.kool.saaremeister.component.LabelComponent;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.CaptureZoneState;
import message.dto.PlayerRole;
import message.dto.PlayerState;

import java.util.HashMap;
import java.util.Map;

import static constant.Constants.MATCH_DURATION_SECONDS;

public class TopBarUI implements UIComponent<GameStateMessage> {

    private final Table root;
    private float pulseTime;
    private final Map<String, Image[]> zoneDots;
    private final Drawable emptyDot;
    private final Drawable playerDot;
    private final Drawable enemyDot;
    private final Label timerLabel;
    @Setter
    private PlayerRole playerRole;
    @Setter
    private PlayerState playerState;

    private static final int DOT_SIZE = 20;
    private static final int DOT_PADDING = 4;
    private static final int PULSE_START_WHEN_SECONDS_TO_END = 30;

    private static final Color EMPTY_DOT_COLOR = Color.DARK_GRAY;
    private static final Color PLAYER_DOT_COLOR = new Color(57/255f, 93/255f, 238/255f, 1);
    private static final Color ENEMY_DOT_COLOR = new Color(238/255f, 57/255f, 57/255f, 1);
    private static final Color TEXT_COLOR = Color.WHITE;

    public TopBarUI(Skin skin) {
        root = new Table();

        zoneDots = new HashMap<>();
        zoneDots.put("player", new Image[4]);
        zoneDots.put("enemy", new Image[4]);

        root.setFillParent(true);
        root.top();

        root.pad(20);

        // Player zones table
        root.add().expandX();
        emptyDot = skin.newDrawable("white", EMPTY_DOT_COLOR);
        playerDot = skin.newDrawable("white", PLAYER_DOT_COLOR);
        Table playerZonesTable = new Table();
        for (int i = 0; i < 4; i++) {
            zoneDots.get("player")[i] = new Image(emptyDot);
            playerZonesTable.add(zoneDots.get("player")[i]).size(DOT_SIZE, DOT_SIZE).pad(DOT_PADDING);
        }
        root.add(playerZonesTable);
        root.add().expandX();

        // Timer
        timerLabel = LabelComponent.createLabel("2:00", TEXT_COLOR, 20);
        Container<Label> timerContainer = new Container<>(timerLabel);
        timerContainer.setTransform(true);
        root.add(timerContainer).width(200).height(80).center();
        timerLabel.setFontScale(4f);
        root.add().expandX();

        // Enemy zones table
        enemyDot = skin.newDrawable("white", ENEMY_DOT_COLOR);
        Table enemyZonesTable = new Table();
        for (int i = 0; i < 4; i++) {
            zoneDots.get("enemy")[i] = new Image(emptyDot);
            enemyZonesTable.add(zoneDots.get("enemy")[i]).size(DOT_SIZE, DOT_SIZE).pad(DOT_PADDING);
        }
        root.add(enemyZonesTable);
        root.add().expandX();
    }

    public void update(GameStateMessage msg) {

        int totalSeconds = Math.max(0, (int) (MATCH_DURATION_SECONDS - msg.getGameTime()));
        int minutes =  totalSeconds / 60;
        int seconds = totalSeconds % 60;

        float pulse = (MathUtils.sin(pulseTime * 6f) * 1.5f);

        if (totalSeconds == PULSE_START_WHEN_SECONDS_TO_END) pulseTime = 0;
        if (totalSeconds < PULSE_START_WHEN_SECONDS_TO_END) {
            timerLabel.setFontScale(4f + pulse / 3f);
            timerLabel.setColor(new Color(Color.WHITE)
                    .lerp(playerRole == PlayerRole.MASTER ? Color.RED : Color.GREEN,
                            pulse));
        }
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));


        // Updates the player's captured zones.
        int ownedZones = 0;
        if (msg.getCaptureZones() != null) {
            ownedZones = (int) msg.getCaptureZones().stream()
                    .filter(zone -> zone.getOwnerRole() == playerRole)
                    .count();
        }
        for (int i = 0; i < 4; i++) {
            zoneDots.get("player")[i]
                    .setDrawable(i < ownedZones ? playerDot : emptyDot);
            zoneDots.get("enemy")[i]
                    .setDrawable(i < ownedZones ? emptyDot : enemyDot);
        }

    }

    public Table getActor() {
        return root;
    }

    public void updatePulseTimer(float delta) {
        pulseTime += delta;
    }

}
