package ee.kool.saaremeister.utility;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import message.dto.StaticBodyState;

import com.badlogic.gdx.utils.Array;

import java.util.List;

import static constant.Constants.PPM;

/**
 * Debug renderer for Box2D physics bodies.
 */
public class DebugRenderer {
    private final World mirrorWorld;
    private final Box2DDebugRenderer renderer;

    public DebugRenderer() {
        mirrorWorld = new World(new Vector2(0, 0), false);
        renderer = new Box2DDebugRenderer();
    }

    /**
     * Loads static bodies from the server.
     */
    public void loadStaticBodies(List<StaticBodyState> bodies) {
        clearBodies();

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;

        for (StaticBodyState data : bodies) {
            def.position.set(data.worldPosition);
            Body body = mirrorWorld.createBody(def);

            PolygonShape shape = new PolygonShape();
            shape.set(data.vertices);
            body.createFixture(shape, 0f);
            shape.dispose();
        }
    }

    public void render(Camera camera) {
        Matrix4 debugMatrix = new Matrix4(camera.combined).scale(PPM, PPM, 1f);
        renderer.render(mirrorWorld, debugMatrix);
    }

    /**
     * Clears all bodies from the physics world.
     */
    private void clearBodies() {
        Array<Body> bodies = new Array<>();
        mirrorWorld.getBodies(bodies);

        for (Body body : bodies) {
            mirrorWorld.destroyBody(body);
        }
    }

    public void dispose() {
        mirrorWorld.dispose();
        renderer.dispose();
    }

}
