package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.physics.box2d.World;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import lombok.Getter;
import message.dto.BuildingState;

import java.util.List;

import static constant.Constants.PPM;

/**
 * Represents a defense building that protects the player from bullets.
 */
public class DefenceBuilding extends Building {

    @Getter
    private List<BuildingState> tiles;

    public DefenceBuilding(int id, int tileX, int tileY, World world, int health) {
        super(id, tileX, tileY, world, health);
        tiles = BuildingInfo.getWallTileLocations(new BuildingState(id, tileX, tileY, health, health));

        defineBody();

        super.x = body.getPosition().x * PPM;
        super.y = body.getPosition().y * PPM;
    }

    @Override
    protected void defineBody() {
        Box2dWorldGenerator generator = new Box2dWorldGenerator();
        int width = 16;
        int height = 16;

        if (tiles.get(0).getX() == tiles.get(1).getX()) {
            height *= tiles.size();
        } else {
            width *= tiles.size();
        }
        body = generator.buildBody(tileX, tileY, world, width, height);
        System.out.println("Body position: " + body.getPosition());
    }

    public List<BuildingState> getTiles() {
        return BuildingInfo.getWallTileLocations(super.getBuildingState());
    }

}
