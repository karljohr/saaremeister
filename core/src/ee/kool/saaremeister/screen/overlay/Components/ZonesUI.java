package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.esotericsoftware.minlog.Log;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.CaptureZoneState;
import message.dto.PlayerRole;


import java.util.List;

import static constant.Constants.CAPTURE_SECONDS_REQUIRED;

public class ZonesUI implements UIComponent<GameStateMessage> {

    private final Table container;
    private final Image[] zoneFills;
    private final Image[] zoneFrames;
    private final Cell<?>[] fillCells;
    private final Drawable emptyFill;
    private final Drawable playerFill;
    private final Drawable enemyFill;
    private final Drawable contestedFrame;
    @Setter
    private PlayerRole playerRole;

    private static final int BOX_SIZE = 48;
    private static final int BOX_PADDING = 4;
    private static final int BORDER_WIDTH = 3;
    private static final float FRAME_INNER_SIZE = BOX_SIZE - BORDER_WIDTH * 2f;
    private static final Color EMPTY_DOT_COLOR = Color.DARK_GRAY;
    private static final Color PLAYER_DOT_COLOR = new Color(57/255f, 93/255f, 238/255f, 1);
    private static final Color ENEMY_DOT_COLOR = new Color(238/255f, 57/255f, 57/255f, 1);
    private static final Color CONTESTED_BORDER_COLOR = Color.GOLD;
    private static final Color BORDER_COLOR = new Color(0, 0, 0, 1);

    public ZonesUI(Skin skin) {
        Table zonesGrid = new Table();

        emptyFill = skin.newDrawable("white", EMPTY_DOT_COLOR);
        playerFill = skin.newDrawable("white", PLAYER_DOT_COLOR);
        enemyFill = skin.newDrawable("white", ENEMY_DOT_COLOR);
        contestedFrame = createFrameDrawable(CONTESTED_BORDER_COLOR);
        zoneFrames = new Image[4];
        zoneFills = new Image[4];
        fillCells = new Cell[4];

        for (int i = 0; i < 4; i++) {
            Stack zoneBox = new Stack();

            zoneFrames[i] = new Image();
            zoneFills[i] = new Image(emptyFill);

            Table fillContainer = new Table();
            fillCells[i] = fillContainer.add(zoneFills[i]).size(BOX_SIZE, BOX_SIZE).center();

            zoneBox.add(fillContainer);
            zoneBox.add(zoneFrames[i]);

            zonesGrid.add(zoneBox).size(BOX_SIZE, BOX_SIZE).pad(BOX_PADDING);
            if (i == 1) {
                zonesGrid.row();
            }
        }

        container = new Table();
        container.setFillParent(true);
        container.top().left();
        container.pad(50);

        container.add(zonesGrid);

    }

    public Table getActor() {
        return container;
    }

    public void update(GameStateMessage msg) {
        for (int i = 0; i < zoneFrames.length; i++) {
            zoneFills[i].setDrawable(emptyFill);
            zoneFrames[i].setVisible(false);
            zoneFills[i].setScale(1f);
        }

        List<CaptureZoneState> zones = msg.getCaptureZones();
        if (zones == null) return;

        for (CaptureZoneState zone : zones) {
            int index = zone.getZoneId() - 1;
            if (index < 0 || index >= zoneFills.length) continue;

            zoneFills[index].setDrawable(getFillDrawable(zone));

            if (zone.isContested()) {
                zoneFrames[index].setVisible(true);
                zoneFrames[index].setDrawable(contestedFrame);
            } else if (zone.getCapturingRole() != null) {
                float ratio = Math.min(1f, zone.getCaptureProgressSeconds() / CAPTURE_SECONDS_REQUIRED);
                float scale = 1f - ratio;
                zoneFills[index].setOrigin(Align.center);
                zoneFills[index].setScale(scale);
            }
        }
    }

    private Drawable getFillDrawable(CaptureZoneState zone) {
        if (zone.getOwnerRole() == null) {
            return emptyFill;
        }
        if (zone.getOwnerRole() == playerRole) {
            return playerFill;
        }
        return enemyFill;
    }

    private Drawable createFrameDrawable(Color borderColor) {
        Pixmap pixmap = new Pixmap(BOX_SIZE, BOX_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        if (borderColor.a > 0f) {
            pixmap.setColor(borderColor);
            for (int i = 0; i < BORDER_WIDTH; i++) {
                pixmap.drawRectangle(i, i, BOX_SIZE - i * 2, BOX_SIZE - i * 2);
            }
        }
        Drawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

}
