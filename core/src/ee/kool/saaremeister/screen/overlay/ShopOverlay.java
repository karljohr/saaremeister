package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.kool.saaremeister.component.LabelComponent;
import lombok.Getter;

import static ee.kool.saaremeister.utility.Font.getPixelFont;

/**
 * In-game shop (toggle with B). Category buttons above the item grid; balance only in HUD.
 */
public class ShopOverlay implements Disposable {

    private static final float HUD_VIEWPORT_W = 800f;
    private static final float HUD_VIEWPORT_H = 600f;

    private static final float SLOT_SIZE = 72f;
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 3;

    @Getter
    private final Stage stage;
    private final Table root;
    private final Texture panelBgTexture;
    private final Texture dividerTexture;
    private final Texture slotTexture;

    private final Texture shopButtonUpTexture;
    private final Texture shopButtonOverTexture;

    public ShopOverlay(SpriteBatch spriteBatch) {
        Viewport viewport = new FitViewport(HUD_VIEWPORT_W, HUD_VIEWPORT_H);
        this.stage = new Stage(viewport, spriteBatch);

        panelBgTexture = createPanelTexture();
        panelBgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegionDrawable panelBg = new TextureRegionDrawable(new TextureRegion(panelBgTexture));

        slotTexture = createSlotTexture();
        slotTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegionDrawable slotDrawable = new TextureRegionDrawable(new TextureRegion(slotTexture));

        shopButtonUpTexture = createShopButtonTexture(false);
        shopButtonOverTexture = createShopButtonTexture(true);
        shopButtonUpTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        shopButtonOverTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        root = new Table();
        root.setFillParent(true);
        root.top();
        root.setVisible(false);
        root.setTouchable(Touchable.childrenOnly);

        Table panel = new Table();
        panel.setBackground(panelBg);
        panel.pad(14, 18, 16, 18);

        Label title = LabelComponent.createLabel("Pood", Color.LIGHT_GRAY, 22);
        panel.add(title).left().padBottom(2).row();

        Label subtitle = LabelComponent.createLabel("Varustus vastavalt rollile", Color.LIGHT_GRAY, 13);
        panel.add(subtitle).left().padBottom(6).row();

        dividerTexture = createDividerTexture();
        dividerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        panel.add(new Image(dividerTexture)).height(2).growX().padBottom(10).row();

        // Category buttons
        Table categoryRow = new Table();

        categoryRow.defaults().pad(2).uniformX();
        categoryRow.add(shopCategoryButton(13, "Kõik", () -> { }));
        categoryRow.add(shopCategoryButton(13, "Relvad", () -> { }));
        categoryRow.add(shopCategoryButton(13, "Ehitised", () -> { }));
        categoryRow.add(shopCategoryButton(13, "Tarvikud", () -> { }));
        panel.add(categoryRow).growX().padBottom(10).row();

        Table grid = buildItemGrid(slotDrawable);
        panel.add(grid).padBottom(10).row();

        Label closeHint = LabelComponent.createLabel("Klahv B | sulge / ava", Color.LIGHT_GRAY, 13);
        panel.add(closeHint).center().padTop(10).row();

        root.add(panel).width(600f).padTop(12f).padBottom(56f).expandX().center();

        stage.addActor(root);
        setOpen(false);
    }

    private TextButton shopCategoryButton(int fontSize, String text, Runnable onClick) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = getPixelFont(fontSize);
        style.fontColor = Color.LIGHT_GRAY;
        style.up = new TextureRegionDrawable(new TextureRegion(shopButtonUpTexture));
        style.over = new TextureRegionDrawable(new TextureRegion(shopButtonOverTexture));
        style.down = style.over;
        TextButton button = new TextButton(text, style);

        // All buttons same width
        button.getLabelCell().width(100f);
        button.pad(4, 8, 4, 8);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
        });
        return button;
    }

    private static Table buildItemGrid(TextureRegionDrawable slotDrawable) {
        Table grid = new Table();
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                Image slot = new Image(slotDrawable);
                grid.add(slot).size(SLOT_SIZE, SLOT_SIZE).pad(0);
            }
            grid.row();
        }
        return grid;
    }

    private static Texture createShopButtonTexture(boolean hover) {
        // Darkgray buttons
        Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        if (hover) {
            // Darkblue hover
            pm.setColor(0.2f, 0.2f, 0.25f, 1f);
        } else {
            // Darkgray like panel
            pm.setColor(0.12f, 0.12f, 0.14f, 1f);
        }
        pm.fill();
        // Dark border
        pm.setColor(0.4f, 0.4f, 0.42f, 1f);
        pm.drawRectangle(0, 0, 32, 32);
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static Texture createPanelTexture() {
        Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);

        pm.setColor(0.1f, 0.1f, 0.12f, 1f);
        pm.fill();

        // Dark border
        pm.setColor(0.25f, 0.25f, 0.28f, 1f);
        pm.drawRectangle(0, 0, 32, 32);

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static Texture createDividerTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        // Separationline which is thin and golden
        pm.setColor(0.85f, 0.70f, 0.45f, 1f);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static Texture createSlotTexture() {
        int s = 72;
        Pixmap pm = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        pm.setColor(0.44f, 0.30f, 0.20f, 1f);
        pm.fill();
        for (int i = 0; i < s; i += 3) {
            pm.setColor(0.40f, 0.27f, 0.18f, 1f);
            pm.drawLine(i, 0, i, s);
        }
        pm.setColor(0.18f, 0.11f, 0.07f, 1f);
        pm.drawRectangle(0, 0, s, s);
        pm.setColor(0.52f, 0.38f, 0.26f, 1f);
        pm.drawRectangle(2, 2, s - 4, s - 4);
        pm.setColor(0.58f, 0.46f, 0.34f, 1f);
        pm.drawRectangle(4, 4, s - 8, s - 8);
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    public void setOpen(boolean open) {
        root.setVisible(open);
        root.setTouchable(open ? Touchable.childrenOnly : Touchable.disabled);
    }

    public boolean isOpen() {
        return root.isVisible();
    }

    public void render(float delta) {
        if (!root.isVisible()) {
            return;
        }
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        panelBgTexture.dispose();
        dividerTexture.dispose();
        slotTexture.dispose();
        shopButtonUpTexture.dispose();
        shopButtonOverTexture.dispose();
    }
}
