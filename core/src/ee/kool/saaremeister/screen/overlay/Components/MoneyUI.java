package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import ee.kool.saaremeister.component.LabelComponent;
import message.dto.PlayerState;

import java.util.NoSuchElementException;

public class MoneyUI implements UIComponent<PlayerState> {

    private final Table root;
    private final Texture coinTexture;
    private final Label moneyLabel;

    private static final Color TEXT_COLOR = Color.GOLD;


    public MoneyUI() {
        root = new Table();

        coinTexture = new Texture(Gdx.files.internal("goldCoin.png"));
        Image moneyIcon = new Image(coinTexture);
        moneyLabel = LabelComponent.createLabel("0", TEXT_COLOR, 20);


        root.add(moneyIcon).size(24, 24).padLeft(14);
        root.add(moneyLabel).padLeft(14);
        moneyLabel.setFontScale(2f);


    }

    public void update(PlayerState me) {
            moneyLabel.setText(String.valueOf(me.getMoney()));
    }

    public Table getActor() {
        return root;
    }

    public void dispose() {
        coinTexture.dispose();
}

}
