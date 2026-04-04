package ee.kool.saaremeister.server.game;

import ee.kool.saaremeister.server.game.object.Building;
import ee.kool.saaremeister.server.game.object.Player;

import java.util.ArrayList;
import java.util.List;

public class BuildHandler {

    private List<Building> buildings;

    public BuildHandler() {
        buildings = new ArrayList<>();
    }

    private boolean validate(Player player, Building building) {
        // TODO: only continue if player is "saaremeister".
        for (Building build : buildings) {
            if (build.getTileX() == building.getTileX() && build.getTileY() == building.getTileY()) {
                return false;
            }
        }
        return true;
    }

    public void build(Player player, Building building) {
        if (!validate(player, building)) return;
        buildings.add(building);
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void shoot() {
        for (Building building : buildings) {
            building.shoot();
        }
    }


}
