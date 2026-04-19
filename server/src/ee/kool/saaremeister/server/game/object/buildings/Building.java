package ee.kool.saaremeister.server.game.object.buildings;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import constant.Constants;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import lombok.Getter;
import message.dto.BuildingState;

/**
 * Represents a building in the game.
 */
@Getter
public abstract class Building {
    protected final int uniqueId;
    protected int id;
    protected final int tileX;
    protected final int tileY;
    protected float x;
    protected float y;
    protected final World world;
    protected int health;
    protected  int maxHealth;

    protected Body body;

    private final Matrix4 isoDebugTransform = new Matrix4();
    private final Vector3 tmpVector = new Vector3();

    private static int i = 100;

    public Building(int id, int tileX, int tileY, World world, int health) {
        this.uniqueId = getIdAndIncrementI();
        this.id = id;
        this.tileX = tileX;
        this.tileY = tileY;
        this.world = world;
        this.maxHealth = health;
        this.health = maxHealth;
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
        buildingState.worldX = (int) x;
        buildingState.worldY = (int) y;
        buildingState.health = health;
        buildingState.maxHealth = maxHealth;
        buildingState.uniqueId = uniqueId;
        return buildingState;
    }

    /**
     * Decreases the buildings health by bullet damage.
     */
    public void decreaseHealth() {
        health -= (int) Constants.BULLET_DAMAGE;
    }

    /**
     * Destroys the Box2D body of the building.
     */
    public void destroy() {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }

    /**
     * Returns a unique id for a building and increments the static counter.
     */
    private static int getIdAndIncrementI() {
        return i++;
    }
}
