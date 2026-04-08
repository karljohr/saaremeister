package ee.kool.saaremeister.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import constant.Category;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hotbar extends Table {
    // Colors constants
    private static final Color SLOT_BG_NORMAL = new Color(0.1f, 0.1f, 0.1f, 0.7f);
    private static final Color SLOT_BG_SELECTED = new Color(0.3f, 0.3f, 0.5f, 0.9f);
    private static final Color BORDER_NORMAL = Color.DARK_GRAY;
    private static final Color BORDER_SELECTED = Color.GOLD;

    // Size constants
    private static final int SLOT_SIZE = 70;
    private static final int BORDER_WIDTH = 3;
    private static final int SLOT_PADDING = 5;

    private final List<Table> slots = new ArrayList<>();
    private final TextureRegionDrawable slotNormal;
    private final TextureRegionDrawable slotSelected;
    @Getter
    private int selectedIndex = 0;
    private final int totalSlots = 3;
    private final String[] slotItems = new String[3];
    private final Table[] itemContainers = new Table[3]; // For item icons

    public Hotbar(Skin skin) {
        slotNormal = createDrawable(SLOT_BG_NORMAL, BORDER_NORMAL);
        slotSelected = createDrawable(SLOT_BG_SELECTED, BORDER_SELECTED);

        for (int i = 0; i < totalSlots; i++) {
            Table slotFrame = new Table();
            slotFrame.setBackground(i == 0 ? slotSelected : slotNormal);

            Stack stack = new Stack();

            // Number 1, 2, 3
            Table labelTable = new Table();
            labelTable.top().left();
            labelTable.add(new Label(String.valueOf(i + 1), skin)).pad(2);

            // Image container
            Table itemTable = new Table();
            itemContainers[i] = itemTable;

            stack.add(labelTable);
            stack.add(itemTable);

            slotFrame.add(stack).size(SLOT_SIZE, SLOT_SIZE);
            add(slotFrame).size(SLOT_SIZE, SLOT_SIZE).pad(SLOT_PADDING);
            slots.add(slotFrame);
        }
    }

    public boolean addItem(message.dto.ShopItem item) {
        int targetSlot = -1;

        // Decide slot
        if (item.getCategory() == Category.RELVAD) {
            targetSlot = 0;
        } else if (item.getCategory() == Category.TARVIKUD && !item.getName().contains("rüü")) {
            if (slotItems[1] == null) targetSlot = 1;
            else if (slotItems[2] == null) targetSlot = 2;
        }

        if (targetSlot != -1) {
            slotItems[targetSlot] = item.getName();

            // Create icon
            Texture tex = getIconTexture(item.getTextureName());
            Image icon = new Image(tex);

            // Clear old icon and add new
            itemContainers[targetSlot].clearChildren();
            itemContainers[targetSlot].add(icon).size(48, 48).center();

            return true;
        }
        return false;
    }

    public String getSelectedItem() {
        return slotItems[selectedIndex];
    }

    public boolean isGunSelected() {
        String selected = getSelectedItem();
        return selectedIndex == 0 && "Püstol".equals(selected);
    }

    public List<String> getAllItems() {
        return Arrays.asList(slotItems);
    }

    public void clearSlot(int index) {
        if (index >= 0 && index < totalSlots) {
            slotItems[index] = null;
            itemContainers[index].clearChildren();
        }
    }

    private Texture getIconTexture(String textureName) {
        return new Texture(Gdx.files.internal("shop/" + textureName + ".png"));
    }

    private TextureRegionDrawable createDrawable(Color bg, Color border) {
        Pixmap pm = new Pixmap(SLOT_SIZE, SLOT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(bg);
        pm.fill();
        pm.setColor(border);
        for(int i=0; i<BORDER_WIDTH; i++) pm.drawRectangle(i, i, SLOT_SIZE-i*2, SLOT_SIZE-i*2);
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pm)));
        pm.dispose();
        return drawable;
    }

    public void setSelection(int index) {
        // Keep indexes in [0-2]
        if (index < 0) index = totalSlots - 1;
        else if (index >= totalSlots) index = 0;

        slots.get(selectedIndex).setBackground(slotNormal);
        selectedIndex = index;
        slots.get(selectedIndex).setBackground(slotSelected);
    }
}
