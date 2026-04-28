package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * TutorialOverlay is a UI component that displays the game's manual.
 * It provides information about the goal, controls, and economy.
 * It is designed to be shown as a popup on Title and Lobby screens.
 */
public class TutorialOverlay extends Table {
    public TutorialOverlay(Skin skin) {
        this.setFillParent(false);
        this.center();

        try {
            Texture backgroundTexture = new Texture(Gdx.files.internal("button/up.png"));
            this.setBackground(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        } catch (Exception e) {
            // If textures not found, no background
        }

        String text = """
                M2NGUJUHEND
               \s
                M2NGU EESM2RK:
                - SAAREMEISTER: Kaitse saart ja sellel asuvaid vallutusalasid. Sinu ylesanne on h2vitada sissetungivad\s
                maksuametnikud ning mitte lasta neil vallutada kõik saarel olevad alad enne m2nguaja lõppu
                - MAKSUAMETNIK: Vallutage tiimiga saarel olevaid alasid. V6iduks peab teie meeskond kontrollima k6iki\s
                alasid enne m2nguaja l6ppu.
                - SURM: M6lemad osapooled synnivad uuesti (respawn) 5 sekundit peale surma.
               \s
                MAJANDUS:
                - ALGKAPITAL: Iga m2ngija alustab m2ngu kindla rahasummaga.
                - TULUALLIKAD: Raha teenid vastaste alistamisel ja aardekirstude leidmisel. (Coming soon)
                - MAKSUTULU: Maksuametnikuna teenid lisaressursse iga vallutatud objekti pealt. (Coming soon)
               \s
                JUHTIMINE:
                - WASD: Liikumine
                - SPACE: Tulistamine (sihtimine hiirega)
                - B: Pood
                - Ehitamise juhtimised: Ehitamise ajal all vasakus nurgas
               \s""";

        Label infoLabel = new Label(text, skin);
        infoLabel.setWrap(true);
        infoLabel.setFontScale(0.9f);

        this.add(infoLabel).width(1200).pad(40);
        this.row();

        // Closing button
        TextButton closeButton = new TextButton("Sulge", skin);
        closeButton.addListener(event -> {
            if (!closeButton.isPressed()) {
                return false;
            }
            this.setVisible(false);
            return true;
        });
        this.add(closeButton).padBottom(40).size(200, 60);

        this.setVisible(false);
        this.pack();

        updatePosition();
    }

    /**
     * Updates the position of the overlay to be centered on the screen.
     */
    public void updatePosition() {
        this.pack();

        // Horizontally centered
        float x = (Gdx.graphics.getWidth() - this.getWidth()) / 2;

        // Vertically 50 px from top
        float y = Gdx.graphics.getHeight() - this.getHeight() - 50;

        this.setPosition(x, y);
    }
}