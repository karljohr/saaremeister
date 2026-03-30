package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.object.Building;
import ee.kool.saaremeister.server.game.object.Player;

import java.util.List;

public class BuildHandler {

    private List<Building> buildings;

    public BuildHandler() {}

    private boolean validate(Player player, Building building) {
        // TODO: only continue if player is "saaremeister".
        for (Building build : buildings) {
            if (build.getX() == building.getX() && build.getY() == building.getY()) {
                return false;
            }
        }
        return true;
    }

    public void build(Player player, Building building) {
        if (!validate(player, building)) return;
        buildings.add(building);
    }


}
