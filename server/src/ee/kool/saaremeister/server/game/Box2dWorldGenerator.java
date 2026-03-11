package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import ee.kool.saaremeister.server.TMXLoaders.HijackedTmxLoader;
import ee.kool.saaremeister.server.TMXLoaders.MyServer;


import static constant.Constants.PPM;

public class Box2dWorldGenerator {

    public void initializeWorld(World world) {
        TiledMap tiledMap = getMap();
        MapLayer mapLayer = tiledMap.getLayers().get("collisions"); // Get the collision layer from the Tiled map // assuming the layer is named "Collisions"

        BodyDef bdef = new BodyDef(); // Definition for creating a physics body
        PolygonShape shape = new PolygonShape(); // Shape used for collision detection
        FixtureDef fdef = new FixtureDef(); // Fixture configuration for the body
        Body body; // Reference to the created body

        for (RectangleMapObject object : mapLayer.getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = object.getRectangle(); // for loop to get only rectangular objects in the collision map. There are different types of objects in Tiled maps.

            bdef.type = BodyDef.BodyType.StaticBody; // Make the body static (doesn't move)
            bdef.position.set((rectangle.getX() + rectangle.getWidth() / 2) / PPM,
                    (rectangle.getY() + rectangle.getHeight() / 2) / PPM);  // Set body's center position in meters

            body = world.createBody(bdef);

            shape.setAsBox(rectangle.getWidth() / 2 / PPM, rectangle.getHeight() / 2 / PPM); // Set shape size as half-width/height in meters
            fdef.shape = shape;
            body.createFixture(fdef);

            System.out.println((rectangle.getX() + rectangle.getWidth() / 2) / PPM + " ; " + (rectangle.getY() + rectangle.getHeight() / 2) / PPM
            + " ----- " + rectangle.getWidth() / 2 / PPM + " x " + rectangle.getHeight() / 2 / PPM);
        }
    }

    public TiledMap getMap() {
        return new HijackedTmxLoader(new MyServer.MyFileHandleResolver())
                .load("shared/src/TMXAssets/map.tmx");
    }
}

