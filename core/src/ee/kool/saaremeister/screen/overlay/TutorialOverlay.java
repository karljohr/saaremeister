package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import ee.kool.saaremeister.utility.SoundEffectManager;

import static constant.Constants.*;

/**
 * TutorialOverlay is a UI component that displays the game's manual.
 * It provides information about the goal, controls, and economy.
 * It is designed to be shown as a popup on Title and Lobby screens.
 */
public class TutorialOverlay extends Table {

    private final Cell<ScrollPane> scrollPaneCell;
    private final Table scrollContainer;
    private final Label infoLabel;

    public TutorialOverlay(Skin skin) {
        this.setFillParent(false);
        this.center();

        try {
            Texture backgroundTexture = new Texture(Gdx.files.internal("background/sand.png"));
            this.setBackground(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        } catch (Exception e) {
            // If textures not found, no background
        }

        String text = String.format(
                """
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
                - ALGKAPITAL: Iga m2ngija alustab m2ngu kindla rahasummaga (Saaremeister: %d, Maksuametnik: %d).
                - TULUALLIKAD: Raha teenid j2rgmisetel viisidel:
                    - vastase tapmisel (Saaremeister: %d, Maksuametnik: %d),
                    - maksuametnikuna ehitiste lõhkumisel %d
                    - vallutatud ala eest iga %.0f sekundi p2rast
                      (Saaremeister: %d, Maksuametnik: %d),
                    - aardekirstu leidmisel (Coming Soon).
               \s
                JUHTIMINE:
                - WASD: Liikumine
                - SPACE: Tulistamine (sihtimine hiirega)
                - B: Pood
                - Ehitamise juhtimised: Ehitamise ajal all vasakus nurgas
               \s""",
                MASTER_MONEY, OFFICER_MONEY,
                MASTER_KILL_PROFIT, OFFICER_KILL_PROFIT,
                OFFICER_DESTROY_PROFIT,
                ZONE_PROFIT_INTERVAL, MASTER_ZONE_PROFIT, OFFICER_ZONE_PROFIT
                );

        infoLabel = new Label(text, skin);
        infoLabel.setWrap(true);
        infoLabel.setFontScale(0.9f);

        // Inner table for text
        scrollContainer = new Table();
        scrollContainer.add(infoLabel).pad(20).fillX().expandX();

        // Scroll panel
        ScrollPane scrollPane = new ScrollPane(scrollContainer, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // Scrollable area to the main table
        scrollPaneCell = this.add(scrollPane).pad(20);
        this.row();

        // Closing button
        TextButton closeButton = new TextButton("Sulge", skin);
        SoundEffectManager.bindMenuClick(closeButton);
        closeButton.addListener(event -> {
            if (!closeButton.isPressed()) {
                return false;
            }
            this.setVisible(false);
            return true;
        });
        this.add(closeButton).padBottom(30).size(200, 60);

        this.setVisible(false);

        updatePosition();
    }

    /**
     * If tutorial opened and ESC is pressed, then tutorial gets closed.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        if (this.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            this.setVisible(false);
        }
    }

    /**
     * Updates the position of the overlay to be centered on the screen.
     */
    public void updatePosition() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Calculate dynamic sizes, to consider lower resolutions
        float targetWidth = Math.min(1000, screenWidth - 100);
        float targetHeight = Math.min(600, screenHeight - 200);

        // Scrollable areas limitations and text
        scrollPaneCell.width(targetWidth).height(targetHeight);
        scrollContainer.getCell(infoLabel).width(targetWidth - 60);

        this.pack();

        // Window to the middle of the screen
        float x = (screenWidth - this.getWidth()) / 2;
        float y = (screenHeight - this.getHeight()) / 2;

        this.setPosition(x, y);
    }
}