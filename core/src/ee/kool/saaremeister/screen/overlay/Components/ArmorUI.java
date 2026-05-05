package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import ee.kool.saaremeister.component.LabelComponent;
import message.dto.PlayerState;

public class ArmorUI implements UIComponent<PlayerState> {

    private final Table root;
    private final ProgressBar armorBar;
    private final Label armorLabel;

    private static final Color BAR_COLOR = Color.LIGHT_GRAY;
    private static final Color BAR_BG_COLOR = Color.DARK_GRAY;
    private static final Color TEXT_COLOR = Color.WHITE;

    public ArmorUI(Skin skin) {
        root = new Table();

        Image livesIcon = new Image(skin.getDrawable("armor"));

        ProgressBar.ProgressBarStyle armorBarStyle = new ProgressBar.ProgressBarStyle();
        armorBarStyle.background = skin.newDrawable("white", BAR_BG_COLOR);
        armorBarStyle.knobBefore = skin.newDrawable("white", BAR_COLOR);
        armorBarStyle.background.setMinHeight(15);
        armorBarStyle.knobBefore.setMinHeight(15);

        armorBar = new ProgressBar(0, 100, 10, false, armorBarStyle);
        armorBar.setAnimateDuration(0.1f);

        armorLabel = LabelComponent.createLabel("0% ARMOR", TEXT_COLOR, 20);

        root.add(livesIcon).size(24, 24).padTop(5).padLeft(14).padRight(14).left();
        root.add(armorBar).width(200);
        root.add(armorLabel).padLeft(10);

    }

    public void update(PlayerState me) {
            armorBar.setValue(me.getDefense());
            armorLabel.setText(me.getDefense() + "% ARMOR");
    }

    public void update() {
        PlayerState p = new PlayerState();
        p.setDefense(0);
        this.update(p);
    }

    public Table getActor() {
        return root;
    }
}
