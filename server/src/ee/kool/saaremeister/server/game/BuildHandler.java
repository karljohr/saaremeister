package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.object.Building;
import ee.kool.saaremeister.server.game.object.Player;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.OffenceBuilding;
import message.PlayerBuildMessage;
import message.dto.BuildingState;

import java.util.ArrayList;
import java.util.List;

public class BuildHandler {

    private List<Building> buildings;
    private List<BuildingState> buildMessageQueue;
    private final World world;
    private final GameInstance gameInstance;

    public BuildHandler(World world, GameInstance gameInstance) {
        this.world = world;
        this.gameInstance = gameInstance;
        buildings = new ArrayList<>();
        buildMessageQueue = new ArrayList<>();
    }

    public boolean validate(int x, int y) {
        // TODO: only continue if player is "saaremeister".
        // TODO: only continue if tile allows building (buildable == true).
        for (Building build : buildings) {
            if (build.getTileX() == x && build.getTileY() == y) {
                return false;
            }
        }
        return true;
    }

    public void build(PlayerBuildMessage message, Player player) {
        if (!validate(message.getTileX(), message.getTileY())) {
            Log.info("Player(" + player.getId() + ") tried to build on an invalid tile (" + message.getTileX() + "; " + message.getTileY() + ")");
            return;
        }
        if (player.getBuildingPermitById().contains(BuildingInfo.getBuildingFromVariation(message.id))) {
            if (BuildingInfo.getBuildingVariations(156).contains(message.id)) {
                // Walls
                buildMessageQueue.addAll(BuildingInfo.getWallTileLocations(new BuildingState(message.id, message.tileX, message.tileY)));

            } else {
                // Everything else
                buildMessageQueue.add(new BuildingState(message.id, message.tileX, message.tileY));
            }
            player.removeBuildingPermit(message.id);
        } else {
            Log.info("Player doesn't have the right to build this building: " + message.id + " not in " + player.getBuildingPermitById());
        }
    }

    public void buildFromQueue() {
        if (buildMessageQueue.isEmpty()) return;
        BuildingState buildingState =  buildMessageQueue.removeFirst();
        if (BuildingInfo.getBuildingType(buildingState.id) == BuildingInfo.BuildingType.OFFENCE) {
            buildings.add(
                    new OffenceBuilding(buildingState.id, buildingState.x, buildingState.y, world, gameInstance)
            );
        } else {
            buildings.add(
                    new DefenceBuilding(buildingState.id, buildingState.x, buildingState.y, world)
            );
        }
        Log.info("Building placed at " + buildingState.x + ";" + buildingState.y);
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void shoot() {
        for (Building building : buildings) {
            if (building instanceof OffenceBuilding offenceBuilding) {
                offenceBuilding.shoot();
            }
        }
    }


}
