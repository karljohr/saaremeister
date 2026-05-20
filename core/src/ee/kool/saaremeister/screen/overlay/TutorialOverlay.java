package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import ee.kool.saaremeister.utility.SoundEffectManager;

import static constant.Constants.MASTER_KILL_PROFIT;
import static constant.Constants.MASTER_MONEY;
import static constant.Constants.MASTER_ZONE_PROFIT;
import static constant.Constants.OFFICER_DESTROY_PROFIT;
import static constant.Constants.OFFICER_KILL_PROFIT;
import static constant.Constants.OFFICER_MONEY;
import static constant.Constants.OFFICER_ZONE_PROFIT;
import static constant.Constants.ZONE_PROFIT_INTERVAL;

/**
 * TutorialOverlay is a UI component that displays the game's manual.
 * It is designed to be shown as a popup on Title and Lobby screens.
 */
public class TutorialOverlay extends Table {

    private final Table dialogWindow;
    private final Cell<Table> dialogCell;
    private final Cell<ScrollPane> scrollPaneCell;
    private final Table scrollContainer;
    private final Label infoLabel;
    private final ScrollPane scrollPane;
    private final TextButton closeButton;

    public TutorialOverlay(Skin skin) {
        setFillParent(true);
        center();

        dialogWindow = new Table();
        try {
            Texture backgroundTexture = new Texture(Gdx.files.internal("background/sand.png"));
            dialogWindow.setBackground(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        } catch (Exception e) {
            // The tutorial still works without the decorative background.
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
        scrollContainer.top().left();
        scrollContainer.add(infoLabel).pad(30).growX().top();

        scrollPane = new ScrollPane(scrollContainer, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFlickScroll(true);

        scrollPaneCell = dialogWindow.add(scrollPane).pad(20).expand().fill();
        dialogWindow.row();

        closeButton = new TextButton("Sulge", skin);
        SoundEffectManager.bindMenuClick(closeButton);
        closeButton.addListener(event -> {
            if (!closeButton.isPressed()) {
                return false;
            }
            close();
            return true;
        });
        dialogWindow.add(closeButton).padBottom(30).size(220, 60);

        dialogCell = add(dialogWindow);

        setVisible(false);
        updatePosition();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible()) {
            return;
        }

        updatePosition();

        Stage stage = getStage();
        if (stage != null) {
            Vector2 stageCoords = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            stage.screenToStageCoordinates(stageCoords);

            Actor hit = stage.hit(stageCoords.x, stageCoords.y, true);
            if (hit != null && isDescendantOf(hit, scrollPane)) {
                stage.setScrollFocus(scrollPane);
            } else if (stage.getScrollFocus() == scrollPane) {
                stage.setScrollFocus(null);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
        }
    }

    /**
     * Checks whether the actor under the mouse is inside the scrollPane somewhere in its hierarchy.
     */
    private boolean isDescendantOf(Actor actor, Actor parent) {
        while (actor != null) {
            if (actor == parent) {
                return true;
            }
            actor = actor.getParent();
        }
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updatePosition();
            scrollPane.layout();
            scrollPane.setScrollPercentY(0f);
        }
    }

    private void close() {
        setVisible(false);
    }

    /**
     * Keeps the tutorial centered and sized inside the currently visible UI viewport.
     */
    public void updatePosition() {
        float viewportWidth = getStage() == null
                ? Gdx.graphics.getWidth()
                : getStage().getViewport().getWorldWidth();
        float viewportHeight = getStage() == null
                ? Gdx.graphics.getHeight()
                : getStage().getViewport().getWorldHeight();

        float margin = Math.clamp(Math.min(viewportWidth, viewportHeight) * 0.05f, 16f, 50f);
        float dialogWidth = Math.clamp(viewportWidth - margin * 2f, 320f, 1050f);
        float dialogHeight = Math.clamp(viewportHeight - margin * 2f, 260f, 760f);
        float contentPadding = Math.clamp(dialogWidth * 0.03f, 12f, 30f);
        float buttonHeight = Math.clamp(dialogHeight * 0.08f, 44f, 60f);

        infoLabel.setFontScale(Math.clamp(dialogWidth / 1050f, 0.65f, 0.9f));

        dialogCell.width(dialogWidth).height(dialogHeight);
        scrollPaneCell
                .width(dialogWidth - 40f)
                .height(Math.max(120f, dialogHeight - buttonHeight - 70f));
        scrollContainer.getCell(infoLabel)
                .width(Math.max(220f, dialogWidth - 40f - contentPadding * 2f))
                .pad(contentPadding);
        dialogWindow.getCell(closeButton)
                .padBottom(Math.clamp(dialogHeight * 0.04f, 12f, 30f))
                .size(Math.min(220f, dialogWidth - 80f), buttonHeight);

        invalidateHierarchy();
    }
}
