package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ee.kool.saaremeister.component.LabelComponent;
import common.ShopRegistry;
import ee.kool.saaremeister.screen.gamescreen.GameScreen;
import message.dto.ShopItem;
import constant.Category;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constant.Constants.MAX_SLOTS;
import static ee.kool.saaremeister.utility.Font.getPixelFont;

/**
 * In-game shop (toggle with B). Category buttons above the item grid; balance only in HUD.
 */
public class ShopOverlay implements Disposable {

    private static final float SLOT_SIZE = 84f;
    private static final int GRID_COLS = 5;

    // Custom UI Colors
    private static final Color PANEL_DARK = new Color(0.1f, 0.1f, 0.12f, 1f);
    private static final Color PANEL_BORDER = new Color(0.25f, 0.25f, 0.28f, 1f);
    private static final Color GOLDEN_DIVIDER = new Color(0.85f, 0.70f, 0.45f, 1f);

    // Slot/Wood Colors
    private static final Color WOOD_NORMAL = new Color(0.44f, 0.30f, 0.20f, 1f);
    private static final Color WOOD_HOVER = new Color(0.54f, 0.40f, 0.30f, 1f);
    private static final Color WOOD_GRAIN = new Color(0.40f, 0.27f, 0.18f, 1f);
    private static final Color WOOD_BORDER = new Color(0.18f, 0.11f, 0.07f, 1f);

    // Button Colors
    private static final Color BTN_NORMAL = new Color(0.12f, 0.12f, 0.14f, 1f);
    private static final Color BTN_HOVER = new Color(0.2f, 0.2f, 0.25f, 1f);

    @Getter
    private final Stage stage;
    private final Table root;
    private final Table itemGrid;
    private final TextureRegionDrawable slotDrawableNormal;
    private final TextureRegionDrawable slotDrawableHover;

    private final Texture panelBgTexture, dividerTexture, slotTextureNormal, slotTextureHover, shopButtonUpTexture, shopButtonOverTexture;

    private final Map<String, Texture> iconCache = new HashMap<>();
    private Category currentCategory = Category.KOIK;

    private final GameScreen game;

    /**
     * Creates a new ShopOverlay instance used in a game instance.
     */
    public ShopOverlay(SpriteBatch spriteBatch, GameScreen game) {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(0.5f);
        this.stage = new Stage(viewport, spriteBatch);

        this.game = game;

        panelBgTexture = createPanelTexture();
        dividerTexture = createDividerTexture();

        slotTextureNormal = createSlotTexture(false);
        slotTextureHover = createSlotTexture(true);

        shopButtonUpTexture = createShopButtonTexture(false);
        shopButtonOverTexture = createShopButtonTexture(true);

        slotDrawableNormal = new TextureRegionDrawable(new TextureRegion(slotTextureNormal));
        slotDrawableHover = new TextureRegionDrawable(new TextureRegion(slotTextureHover));

        root = new Table();
        root.setFillParent(true);
        root.top();
        root.setVisible(false);

        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelBgTexture)));
        panel.pad(14, 18, 16, 18);

        panel.add(LabelComponent.createLabel("Pood", Color.LIGHT_GRAY, 22)).left().row();
        panel.add(new Image(dividerTexture)).height(2).growX().padBottom(10).row();

        Table categoryRow = new Table();
        categoryRow.defaults().pad(4);
        for (Category cat : Category.values()) {
            TextButton btn = shopCategoryButton(13, cat.getDisplayName(), () -> {
                currentCategory = cat;
                refreshGrid();
            });
            categoryRow.add(btn).padLeft(5).padRight(5);
        }
        panel.add(categoryRow).growX().padBottom(10).row();

        itemGrid = new Table();
        panel.add(itemGrid).padBottom(10).row();

        Label closeHint = LabelComponent.createLabel("Klahv B | sulge / ava", Color.LIGHT_GRAY, 13);
        panel.add(closeHint).center().padTop(10).row();

        root.add(panel).width(650f).padTop(12f).center();
        stage.addActor(root);

        refreshGrid();
    }

    /**
     * Refreshes the item grid based on the current category.
     */
    public void refreshGrid() {
        itemGrid.clearChildren();

        // Find players armor type
        message.GameStateMessage msg = game.getGameStateManager().getLatestGameStateMessage();
        common.ArmorType currentArmor = common.ArmorType.NONE;

        if (msg != null) {
            currentArmor = msg.getPlayerStates().stream()
                    .filter(p -> p.getId() == game.getMyId())
                    .findFirst()
                    .map(message.dto.PlayerState::getArmorType)
                    .orElse(common.ArmorType.NONE);
        }
        List<String> itemsInHotbar = game.getHud().getHotbar().getAllItems();

        List<ShopItem> filteredItems = new ArrayList<>();
        for (ShopItem item : ShopRegistry.ITEMS) {
            if (currentCategory != Category.KOIK && item.getCategory() != currentCategory) continue;

            // Armor check
            common.ArmorType itemArmor = common.ArmorType.fromName(item.getName());
            if (itemArmor != common.ArmorType.NONE) {
                if (itemArmor.getLevel() <= currentArmor.getLevel()) continue;
            }

            // Gun check
            if (item.getName().equals("Püstol") && itemsInHotbar.contains("Püstol")) {
                continue;
            }

            filteredItems.add(item);
        }

        // 15 slots
        for (int i = 0; i < MAX_SLOTS; i++) {
            Table slotContainer = new Table();
            slotContainer.setBackground(slotDrawableNormal);

            // If we have an item for this index, fill the slot
            if (i < filteredItems.size()) {
                ShopItem item = filteredItems.get(i);

                Texture iconTex = getIconTexture(item.getTextureName());
                Image icon = new Image(iconTex);
                slotContainer.add(icon).size(48, 48).pad(4).row();

                Label nameLabel = LabelComponent.createLabel(item.getName(), Color.WHITE, 9);
                Label priceLabel = LabelComponent.createLabel(item.getPrice() + "€", Color.GOLD, 10);
                slotContainer.add(nameLabel).padTop(2).row();
                slotContainer.add(priceLabel);

                slotContainer.setTouchable(Touchable.enabled);
                slotContainer.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        sendPurchaseRequest(item.getId());

                        if (item.getCategory() == Category.EHITISED) {
                            game.enterBuildMode(item);
                        }

                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        slotContainer.setBackground(slotDrawableHover);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        slotContainer.setBackground(slotDrawableNormal);
                    }
                });
            }

            // Add the slot (filled or empty) to the grid
            itemGrid.add(slotContainer).size(SLOT_SIZE, SLOT_SIZE + 20).pad(4);

            // New row every 5 columns
            if ((i + 1) % GRID_COLS == 0) {
                itemGrid.row();
            }
        }
    }

    /**
     * Gets the icon texture for the given item name.
     */
    private Texture getIconTexture(String name) {
        if (!iconCache.containsKey(name)) {
            try {
                iconCache.put(name, new Texture(Gdx.files.internal("shop/" + name + ".png")));
            } catch (Exception e) {
                // Fallback to a blank texture if file is missing
                return panelBgTexture;
            }
        }
        return iconCache.get(name);
    }

    /**
     * Creates a category button.
     */
    private TextButton shopCategoryButton(int fontSize, String text, Runnable onClick) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = getPixelFont(fontSize);
        style.fontColor = Color.LIGHT_GRAY;
        style.up = new TextureRegionDrawable(new TextureRegion(shopButtonUpTexture));
        style.over = new TextureRegionDrawable(new TextureRegion(shopButtonOverTexture));
        style.down = style.over;

        TextButton button = new TextButton(text, style);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
        });
        return button;
    }

    /**
     * Sends a purchase request to the server.
     */
    private void sendPurchaseRequest(int itemId) {
        message.ShopPurchaseMessage msg = new message.ShopPurchaseMessage();
        msg.setItemId(itemId);
        ee.kool.saaremeister.network.ServerConnection.getInstance().getClient().sendTCP(msg);
    }

    // UI Texture Creation

    private static Texture createShopButtonTexture(boolean hover) {
        // Darkgray buttons
        Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pm.setColor(hover ? BTN_HOVER : BTN_NORMAL);
        pm.fill();
        // Dark border
        pm.setColor(Color.GRAY);
        pm.drawRectangle(0, 0, 32, 32);
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static Texture createPanelTexture() {
        Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pm.setColor(PANEL_DARK);
        pm.fill();

        // Dark border
        pm.setColor(PANEL_BORDER);
        pm.drawRectangle(0, 0, 32, 32);
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static Texture createDividerTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(GOLDEN_DIVIDER);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static Texture createSlotTexture(boolean hover) {
        int s = 72;
        Pixmap pm = new Pixmap(s, s, Pixmap.Format.RGBA8888);

        // Background color (Lighter if hover)
        pm.setColor(hover ? WOOD_HOVER : WOOD_NORMAL);
        pm.fill();

        for (int i = 0; i < s; i += 3) {
            pm.setColor(WOOD_GRAIN);
            pm.drawLine(i, 0, i, s);
        }

        // Borders
        pm.setColor(WOOD_BORDER);
        pm.drawRectangle(0, 0, s, s);

        // Inner highlighting borders
        pm.setColor(hover ? Color.ORANGE : Color.BROWN);
        pm.drawRectangle(2, 2, s - 4, s - 4);

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    public void setOpen(boolean open) {
        if (open) {
            refreshGrid(); // Refresh each time the shop gets opened
        }
        root.setVisible(open);
        root.setTouchable(open ? Touchable.childrenOnly : Touchable.disabled);
    }

    public boolean isOpen() {
        return root.isVisible();
    }

    public void render(float delta) {
        if (!root.isVisible()) return;
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
        slotTextureNormal.dispose();
        slotTextureHover.dispose();
        shopButtonUpTexture.dispose();
        shopButtonOverTexture.dispose();
        for (Texture t : iconCache.values()) t.dispose();
        iconCache.clear();
    }
}