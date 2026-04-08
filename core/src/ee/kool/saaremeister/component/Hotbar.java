package ee.kool.saaremeister.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import lombok.Getter;

import java.util.ArrayList;
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

    public Hotbar(Skin skin) {
        slotNormal = createDrawable(SLOT_BG_NORMAL, BORDER_NORMAL);
        slotSelected = createDrawable(SLOT_BG_SELECTED, BORDER_SELECTED);

        for (int i = 1; i <= totalSlots; i++) {
            Table slot = new Table();
            slot.setBackground(i == 1 ? slotSelected : slotNormal);

            Label nrLabel = new Label(String.valueOf(i), skin);
            slot.add(nrLabel).expand().top().left().pad(SLOT_PADDING);

            add(slot).size(SLOT_SIZE, SLOT_SIZE).pad(SLOT_PADDING);
            slots.add(slot);
        }
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
