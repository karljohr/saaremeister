package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import ee.kool.saaremeister.component.LabelComponent;
import message.dto.PlayerState;

import java.util.NoSuchElementException;

public class MoneyUI implements UIComponent<PlayerState> {

    private final Table root;
    private final Texture coinTexture;
    private final Label moneyLabel;
    private final Image moneyIcon;
    private int lastMoney = -1;

    private static final Color TEXT_COLOR = Color.GOLD;


    public MoneyUI() {
        root = new Table();

        root.setTransform(true);
        root.setOrigin(Align.left);

        coinTexture = new Texture(Gdx.files.internal("goldCoin.png"));
        moneyIcon = new Image(coinTexture);
        moneyLabel = LabelComponent.createLabel("0", TEXT_COLOR, 20);


        root.add(moneyIcon).size(24, 24).padLeft(14);
        root.add(moneyLabel).padLeft(14);
        moneyLabel.setFontScale(2f);


    }

    public void update(PlayerState me) {
        int currentMoney = me.getMoney();
        if (lastMoney != -1 && currentMoney > lastMoney) {
            flash();
        }
        moneyLabel.setText(String.valueOf(currentMoney));
        lastMoney = currentMoney;
    }

    public Table getActor() {
        return root;
    }

    public void dispose() {
        coinTexture.dispose();
}

    private void flash() {
        root.clearActions();
        moneyIcon.clearActions();
        moneyLabel.clearActions();

        root.addAction(Actions.sequence(
                Actions.scaleTo(1.15f, 1.15f, 0.08f),
                Actions.scaleTo(1f, 1f, 0.12f)
        ));
        moneyIcon.addAction(Actions.sequence(
                Actions.color(Color.GOLD, 0.06f),
                Actions.color(Color.WHITE, 0.14f)
        ));

        moneyLabel.addAction(Actions.sequence(
                Actions.color(Color.GOLD, 0.06f),
                Actions.color(Color.WHITE, 0.14f)
        ));
    }

}
