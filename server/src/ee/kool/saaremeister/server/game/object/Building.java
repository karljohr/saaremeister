package ee.kool.saaremeister.server.game.object;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import lombok.Getter;

import static constant.Constants.PPM;


@Getter
public class Building {
    private final int id;
    private final int x;
    private final int y;
    private final World world;

    private final Matrix4 isoDebugTransform = new Matrix4();
    private final Vector3 tmpVector = new Vector3();

    private static final int TILE_SIZE = 32;

    public Building(int id, int x, int y, World world) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.world = world;
    }

    public void defineBody() {
        //Box2dWorldGenerator generator = new Box2dWorldGenerator();
        //generator.buildBody(x, y, world);
    }
}
