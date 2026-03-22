package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import ee.kool.saaremeister.component.ButtonComponent;
import ee.kool.saaremeister.component.LabelComponent;

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
                MÄNGUJUHEND
                
                MÄNGU EESMÄRK:
                - SAAREMEISTER: Kaitse saart ja selle strateegilisi objekte. Sinu ülesanne on hävitada sissetungivad maksuametnikud.
                - MAKSUAMETNIK: Vallutage tiimiga saare objekte. Võiduks peab teie meeskond kontrollima vähemalt 80% objektidest.
                - SURM: Mõlemad osapooled sünnivad uuesti (respawn) pärast surma.
                
                MAJANDUS:
                - ALGKAPITAL: Iga mängija alustab mängu kindla rahasummaga.
                - TULUALLIKAD: Raha teenid vastaste alistamisel ja aardekirstude leidmisel.
                - MAKSUTULU: Maksuametnikuna teenid lisaressursse iga vallutatud objekti pealt.
                
                JUHTIMINE:
                - WASD: Liikumine
                - ENTER: Chat
                - B: Pood (Coming soon)
                """;

        Label infoLabel = new Label(text, skin);
        infoLabel.setFontScale(1.5f);
        infoLabel.setWrap(true);

        this.add(infoLabel).width(600).pad(40);
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

    // Updates the position of the overlay to be centered on the screen.
    public void updatePosition() {
        this.pack();

        // Horizontally centered
        float x = (Gdx.graphics.getWidth() - this.getWidth()) / 2;

        // Vertically 50 px from top
        float y = Gdx.graphics.getHeight() - this.getHeight() - 50;

        this.setPosition(x, y);
    }
}