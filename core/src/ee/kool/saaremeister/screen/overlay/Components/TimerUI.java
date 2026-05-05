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

    private static final int PULSE_START_WHEN_SECONDS_TO_END = 30;
    private static final Color TEXT_COLOR = Color.WHITE;

    public TimerUI() {
        root = new Table();

        root.setFillParent(true);
        root.top();

        root.pad(20);

        timerLabel = LabelComponent.createLabel("2:00", TEXT_COLOR, 20);
        Container<Label> timerContainer = new Container<>(timerLabel);
        timerContainer.setTransform(true);

        root.add().expandX();
        root.add(timerContainer).width(200).height(80).center();
        root.add().expandX();

        timerLabel.setFontScale(4f);
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

    }

    public Table getActor() {
        return root;
    }

    public void updatePulseTimer(float delta) {
        pulseTime += delta;
    }

}
