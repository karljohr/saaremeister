package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import ee.kool.saaremeister.server.TMXLoaders.HijackedTmxLoader;
import ee.kool.saaremeister.server.TMXLoaders.MyServer;
import message.dto.StaticBodyState;


import java.util.ArrayList;
import java.util.List;

import static constant.Constants.*;

public class Box2dWorldGenerator {

    public List<StaticBodyState> initializeWorld(World world) {
        List<StaticBodyState> staticBodies = new ArrayList<>();
        TiledMap tiledMap = getMap();
        MapLayer mapLayer = tiledMap.getLayers().get("collisions"); // Get the collision layer from the Tiled map // assuming the layer is named "Collisions"

        BodyDef bdef = new BodyDef(); // Definition for creating a physics body
        PolygonShape shape = new PolygonShape(); // Shape used for collision detection
        FixtureDef fdef = new FixtureDef(); // Fixture configuration for the body
        Body body; // Reference to the created body

        for (RectangleMapObject object : mapLayer.getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = object.getRectangle(); // for loop to get only rectangular objects in the collision map. There are different types of objects in Tiled maps.

            float gridX = rectangle.getX() / TILE_WIDTH;
            float gridY = rectangle.getY() / TILE_WIDTH;

            float cx = (gridX - gridY) * (TILE_WIDTH / 2f) / PPM;
            float cy = (gridX + gridY) * (TILE_WIDTH / 2f) / PPM;

            float halfWidth = rectangle.getWidth() / 2 / PPM;
            float halfHeight = rectangle.getHeight() / 2 / PPM;
            /*
            2D coordinates.
            float cx = (rectangle.getX() + rectangle.getWidth() / 2) / PPM;
            float cy = (rectangle.getY() + rectangle.getHeight() / 2) / PPM;
            */

            bdef.type = BodyDef.BodyType.StaticBody; // Make the body static (doesn't move)
            bdef.position.set(cx, cy);  // Set body's center position in meters

            body = world.createBody(bdef);

            shape.setAsBox(halfWidth, halfHeight);
            fdef.shape = shape;
            body.createFixture(fdef);

            staticBodies.add(new StaticBodyState(cx, cy, halfWidth, halfHeight));

            System.out.println((rectangle.getX() + rectangle.getWidth() / 2) / PPM + " ; " + (rectangle.getY() + rectangle.getHeight() / 2) / PPM
            + " ----- " + rectangle.getWidth() / 2 / PPM + " x " + rectangle.getHeight() / 2 / PPM);
        }

        shape.dispose();
        return staticBodies;
    }

    public TiledMap getMap() {
        return new HijackedTmxLoader(new MyServer.MyFileHandleResolver())
                .load("shared/src/TMXAssets/map.tmx");
    }

}

