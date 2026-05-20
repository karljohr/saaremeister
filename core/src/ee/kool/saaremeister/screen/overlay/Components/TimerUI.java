package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import ee.kool.saaremeister.component.LabelComponent;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.PlayerRole;

import static constant.Constants.MATCH_DURATION_SECONDS;

public class TimerUI implements UIComponent<GameStateMessage> {

    private final Table root;
    private float pulseTime;
    private final Label timerLabel;
    @Setter
    private PlayerRole playerRole;

    private float syncedGameTimeSeconds = 0f;
    private long syncedAtNanos = -1L;
    private boolean hasServerSync = false;
    private boolean wasInPulseWindow = false;

    private static final int PULSE_START_WHEN_SECONDS_TO_END = 30;
    private static final Color TEXT_COLOR = Color.WHITE;

    public TimerUI() {
        root = new Table();

        root.setFillParent(true);
        root.top();

        root.pad(20);

        int startMinutes = MATCH_DURATION_SECONDS / 60;
        int startSeconds = MATCH_DURATION_SECONDS % 60;
        timerLabel = LabelComponent.createLabel(String.format("%d:%02d", startMinutes, startSeconds), TEXT_COLOR, 20);
        Container<Label> timerContainer = new Container<>(timerLabel);
        timerContainer.setTransform(true);

        root.add().expandX();
        root.add(timerContainer).width(200).height(80).center();
        root.add().expandX();

        timerLabel.setFontScale(4f);
    }

    public void update(GameStateMessage msg) {
        if (msg == null) return;
        syncedGameTimeSeconds = Math.max(0f, msg.getGameTime());
        syncedAtNanos = System.nanoTime();
        hasServerSync = true;
        refreshTimeLabel();
    }

    public Table getActor() {
        return root;
    }

    public void updatePulseTimer(float delta) {
        pulseTime += delta;
        refreshTimeLabel();
    }

    private void refreshTimeLabel() {
        float effectiveGameTime = syncedGameTimeSeconds;
        if (hasServerSync && syncedAtNanos > 0L) {
            effectiveGameTime += (System.nanoTime() - syncedAtNanos) / 1_000_000_000f;
        }
        effectiveGameTime = Math.min(effectiveGameTime, MATCH_DURATION_SECONDS);

        int totalSeconds = Math.max(0, (int) Math.ceil(MATCH_DURATION_SECONDS - effectiveGameTime));
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        boolean inPulseWindow = totalSeconds < PULSE_START_WHEN_SECONDS_TO_END;
        if (inPulseWindow && !wasInPulseWindow) {
            pulseTime = 0f;
        }
        wasInPulseWindow = inPulseWindow;

        if (inPulseWindow) {
            float pulse01 = (MathUtils.sin(pulseTime * 6f) + 1f) * 0.5f;
            Color target = switch (playerRole) {
                case MASTER -> Color.GREEN;
                case OFFICER -> Color.RED;
                default -> Color.WHITE;
            };
            timerLabel.setFontScale(4f + (pulse01 * 0.5f));
            timerLabel.setColor(new Color(TEXT_COLOR).lerp(target, pulse01));
        } else {
            timerLabel.setFontScale(4f);
            timerLabel.setColor(TEXT_COLOR);
        }

        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
    }

}
