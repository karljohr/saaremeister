package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.Building;

public class DefenceBuilding extends Building {

    public DefenceBuilding(int id, int tileX, int tileY, World world) {
        super(id, tileX, tileY, world);
    }


}
