package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import ee.kool.saaremeister.server.game.object.Building;
import lombok.Getter;
import message.dto.BuildingState;

import java.util.List;

import static constant.Constants.PPM;

public class DefenceBuilding extends Building {

    @Getter
    private List<BuildingState> tiles;
    private int health = 10;

    public DefenceBuilding(int id, int tileX, int tileY, World world) {
        super(id, tileX, tileY, world);
        tiles = BuildingInfo.getWallTileLocations(new BuildingState(id, tileX, tileY));

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
        body = generator.buildBody(tileX, tileY, world,  width, height);
        System.out.println("Body position: " + body.getPosition());
    }

}
