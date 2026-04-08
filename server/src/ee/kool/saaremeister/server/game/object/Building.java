package ee.kool.saaremeister.server.game.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import lombok.Getter;
import message.dto.BuildingState;

/**
 * Represents a building in the game.
 */
@Getter
public abstract class Building {
    private final int uniqueId;
    private final int id;
    protected final int tileX;
    protected final int tileY;
    protected float x;
    protected float y;
    protected final World world;

    protected Body body;

    private final Matrix4 isoDebugTransform = new Matrix4();
    private final Vector3 tmpVector = new Vector3();

    private static int i = 100;

    public Building(int id, int tileX, int tileY, World world) {
        this.uniqueId = getIdAndIncrementI();
        this.id = id;
        this.tileX = tileX;
        this.tileY = tileY;
        this.world = world;
    }

    protected void defineBody() {
        Box2dWorldGenerator generator = new Box2dWorldGenerator();
        body = generator.buildBody(tileX, tileY, world);
        System.out.println("Body position: " + body.getPosition());
    }

    /**
     * Returns the building state.
     */
    public BuildingState getBuildingState() {
        BuildingState buildingState = new BuildingState();
        buildingState.id = id;
        buildingState.x = tileX;
        buildingState.y = tileY;
        return buildingState;
    }

    /**
     * Returns a unique id for a building and increments the static counter.
     */
    private static int getIdAndIncrementI() {
        return i++;
    }

}
