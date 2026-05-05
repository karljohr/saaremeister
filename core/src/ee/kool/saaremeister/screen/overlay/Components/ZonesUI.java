package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import lombok.Setter;
import message.GameStateMessage;
import message.dto.PlayerRole;

public class ZonesUI implements UIComponent<GameStateMessage> {

    private final Table container;
    private final Image[] dots;
    private final Drawable playerDot;
    private final Drawable enemyDot;
    @Setter
    private PlayerRole playerRole;

    private static final int BOX_SIZE = 48;
    private static final int BOX_PADDING = 4;
    private static final Color EMPTY_DOT_COLOR = Color.DARK_GRAY;
    private static final Color PLAYER_DOT_COLOR = new Color(57/255f, 93/255f, 238/255f, 1);
    private static final Color ENEMY_DOT_COLOR = new Color(238/255f, 57/255f, 57/255f, 1);

    public ZonesUI(Skin skin) {
        Table zonesGrid = new Table();

        playerDot = skin.newDrawable("white", PLAYER_DOT_COLOR);
        enemyDot = skin.newDrawable("white", ENEMY_DOT_COLOR);



        dots = new Image[4];
        Drawable empty = skin.newDrawable("white", EMPTY_DOT_COLOR);

        for (int i = 0; i < 4; i++) {
            dots[i] = new Image(empty);
        }

        zonesGrid.add(dots[0]).size(BOX_SIZE, BOX_SIZE).pad(BOX_PADDING);
        zonesGrid.add(dots[1]).size(BOX_SIZE, BOX_SIZE).pad(BOX_PADDING);
        zonesGrid.row();
        zonesGrid.add(dots[2]).size(BOX_SIZE, BOX_SIZE).pad(BOX_PADDING);
        zonesGrid.add(dots[3]).size(BOX_SIZE, BOX_SIZE).pad(BOX_PADDING);

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

        // Updates the player's captured zones.
        int ownedZones = 0;
        if (msg.getCaptureZones() != null) {
            ownedZones = (int) msg.getCaptureZones().stream()
                    .filter(zone -> zone.getOwnerRole() == playerRole)
                    .count();
        }
        for (int i = 0; i < 4; i++) {
            dots[i].setDrawable(
                    i < ownedZones ? playerDot : enemyDot
            );
        }
    }

}
