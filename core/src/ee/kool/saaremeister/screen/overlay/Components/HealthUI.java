package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import ee.kool.saaremeister.component.LabelComponent;
import message.dto.PlayerState;

import java.util.NoSuchElementException;

public class HealthUI implements UIComponent<PlayerState> {

    private final Table root;
    private final ProgressBar healthBar;
    private final Label healthLabel;

    private static final Color BAR_COLOR = Color.RED;
    private static final Color BAR_BG_COLOR = Color.DARK_GRAY;
    private static final Color TEXT_COLOR = Color.WHITE;

    public HealthUI(Skin skin) {
        root = new Table();

        Image livesIcon = new Image(skin.getDrawable("heart"));

        ProgressBar.ProgressBarStyle healthBarStyle = new ProgressBar.ProgressBarStyle();
        healthBarStyle.background = skin.newDrawable("white", BAR_BG_COLOR);
        healthBarStyle.knobBefore = skin.newDrawable("white", BAR_COLOR);
        healthBarStyle.background.setMinHeight(20);
        healthBarStyle.knobBefore.setMinHeight(20);

        healthBar = new ProgressBar(0, 100, 10, false, healthBarStyle);
        healthBar.setAnimateDuration(0.1f);

        healthLabel = LabelComponent.createLabel("0 HP", TEXT_COLOR, 25);

        root.add(livesIcon).size(32, 32).padTop(5).padLeft(10).padRight(10).left();
        root.add(healthBar).width(200);
        root.add(healthLabel).padLeft(10);

    }

    public void update(PlayerState me) {
            healthBar.setValue(me.getLives() * 10);
            healthLabel.setText(me.getLives() * 10 + " HP");
    }

    public void update() {
        PlayerState p = new PlayerState();
        p.setLives(0);
        this.update(p);
    }

    public Table getActor() {
        return root;
    }
}
