package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import ee.kool.saaremeister.server.TMXLoaders.HijackedTmxLoader;
import ee.kool.saaremeister.server.TMXLoaders.MyServer;
import message.dto.StaticBodyState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.*;

/**
 * Generates the Box2D world from the Tiled map.
 */
public class Box2dWorldGenerator {
    private static final float DEBUG_ISO_ROTATION_DEGREES = 135f;
    private static final float COLLISION_OFFSET_X = 62.320f; // 62.32
    private static final float COLLISION_OFFSET_Y = 20.881f; // 20.881
    private static final int GRID_ORIGIN_TILE = 130;
    private static final float GRID_TILE_SIZE_PIXELS = 16f;

    private static TiledMap cachedMap;

    private final Matrix4 isoDebugTransform = new Matrix4();
    private final Vector3 tmpVector = new Vector3();

    /**
     * Initializes the Box2D world from the Tiled map.
     */
    public List<StaticBodyState> initializeWorld(World world) {
        List<StaticBodyState> staticBodies = new ArrayList<>();
        TiledMap tiledMap = getMap();
        // Get the collision layer from the Tiled map // assuming the layer is named "Collisions"
        MapLayer mapLayer = tiledMap.getLayers().get("collisions");

        buildIsometricDebugTransform(tiledMap);

        BodyDef bdef = new BodyDef(); // Definition for creating a physics body
        bdef.type = BodyDef.BodyType.StaticBody; // Make the body static (doesn't move)
        PolygonShape shape = new PolygonShape(); // Shape used for collision detection
        FixtureDef fdef = new FixtureDef(); // Fixture configuration for the body
        Body body; // Reference to the created body

        for (RectangleMapObject object : mapLayer.getObjects().getByType(RectangleMapObject.class)) {
            // for loop to get only rectangular objects in the collision map.
            // There are different types of objects in Tiled maps.
            Rectangle rectangle = object.getRectangle();

            float width = rectangle.getWidth() / PPM;
            float height = rectangle.getHeight() / PPM;
            if (width <= 0 || height <= 0) continue;

            Vector2 rawCenter = tileToWorld(
                    tiledMap,
                    rectangle.getX() + rectangle.getWidth() / 2f,
                    rectangle.getY() + rectangle.getHeight() / 2f
            );
            Vector2 worldPosition = applyIsometricTransform(rawCenter.x, rawCenter.y);
            bdef.position.set(worldPosition);

            float halfWidth = width / 2f;
            float halfHeight = height / 2f;
            float[] vertices = new float[8];

            setVertex(vertices, 0, rawCenter.x - halfWidth, rawCenter.y - halfHeight, worldPosition);
            setVertex(vertices, 2, rawCenter.x + halfWidth, rawCenter.y - halfHeight, worldPosition);
            setVertex(vertices, 4, rawCenter.x + halfWidth, rawCenter.y + halfHeight, worldPosition);
            setVertex(vertices, 6, rawCenter.x - halfWidth, rawCenter.y + halfHeight, worldPosition);

            body = world.createBody(bdef);
            shape.set(vertices);
            fdef.shape = shape;
            body.createFixture(fdef);

            staticBodies.add(new StaticBodyState(worldPosition, vertices));

            System.out.println(bdef.position.x + " " + bdef.position.y + "\n");


        }

        shape.dispose();
        return staticBodies;
    }

    public static TiledMap getMap() {
        if (cachedMap == null) {
            cachedMap = new HijackedTmxLoader(new MyServer.MyFileHandleResolver())
                    .load("TMXAssets/map.tmx");
        }
        return cachedMap;
    }

    /**
     * Builds the isometric debug transform matrix.
     */
    private void buildIsometricDebugTransform(TiledMap tiledMap) {
        MapProperties props = tiledMap.getProperties();
        float tileWidth = props.get("tilewidth", Integer.class);
        float tileHeight = props.get("tileheight", Integer.class);
        float mapHeight = props.get("height", Integer.class);

        float originX = (mapHeight * tileWidth / 2f) / PPM;
        float scaleX = (float) Math.sqrt(2f);
        float scaleY = (float) Math.sqrt(2f);

        if (tileWidth > tileHeight) {
            scaleY *= tileHeight / tileWidth;
        } else {
            scaleX *= tileWidth / tileHeight;
        }

        isoDebugTransform.idt()
                .translate(originX, 0f, 0f)
                .scale(scaleX, scaleY, 1f)
                .rotate(0f, 0f, 1f, DEBUG_ISO_ROTATION_DEGREES);
    }

    /**
     * Converts pixel coordinates from a Tiled map into world coordinates (scaled by PPM), flipping the origin from
     * top-left to bottom-left and applying collision offsets.
     */
    public static Vector2 tileToWorld(TiledMap tiledMap, float pixelX, float pixelY) {
        MapProperties props = tiledMap.getProperties();
        float tileWidth = props.get("tilewidth", Integer.class);
        float tileHeight = props.get("tileheight", Integer.class);
        int mapHeight = props.get("height", Integer.class);
        int mapWidth = props.get("width", Integer.class);

        float mapHeightPixels = mapHeight * tileHeight;
        float mapWidthPixels = mapWidth * tileWidth;

        float y = (mapHeightPixels - pixelY) / PPM;
        float x = (mapWidthPixels - pixelX) / PPM;

        x -= COLLISION_OFFSET_X;
        y -= COLLISION_OFFSET_Y;

        return new Vector2(x, y);
    }

    /**
     * Converts a build/grid tile coordinate to transformed world coordinates in Box2D meters.
     */
    public static Vector2 tileIndexToWorld(int tileX, int tileY) {
        TiledMap map = getMap();
        Matrix4 isoTransform = createIsometricTransform(map);
        Vector3 tmp = new Vector3();

        float pixelX = (tileX - GRID_ORIGIN_TILE) * GRID_TILE_SIZE_PIXELS + GRID_TILE_SIZE_PIXELS / 2f;
        float pixelY = (tileY - GRID_ORIGIN_TILE) * GRID_TILE_SIZE_PIXELS + GRID_TILE_SIZE_PIXELS / 2f;

        Vector2 rawMeters = tileToWorld(map, pixelX, pixelY);
        tmp.set(rawMeters.x, rawMeters.y, 0f).mul(isoTransform);
        return new Vector2(tmp.x, tmp.y);
    }

    /**
     * Converts transformed world coordinates in Box2D meters to build/grid tile coordinates.
     */
    public static Vector2 worldToTileIndex(float worldX, float worldY) {
        TiledMap map = getMap();
        Matrix4 inverseIsoTransform = new Matrix4(createIsometricTransform(map)).inv();
        Vector3 tmp = new Vector3(worldX, worldY, 0f).mul(inverseIsoTransform);

        MapProperties props = map.getProperties();
        int mapHeight = props.get("height", Integer.class);
        int mapWidth = props.get("width", Integer.class);
        float tileWidth = props.get("tilewidth", Integer.class);
        float tileHeight = props.get("tileheight", Integer.class);

        float mapHeightPixels = mapHeight * tileHeight;
        float mapWidthPixels = mapWidth * tileWidth;

        float pixelX = mapWidthPixels - ((tmp.x + COLLISION_OFFSET_X) * PPM);
        float pixelY = mapHeightPixels - ((tmp.y + COLLISION_OFFSET_Y) * PPM);

        float tileX = (float) Math.floor(pixelX / GRID_TILE_SIZE_PIXELS) + GRID_ORIGIN_TILE;
        float tileY = (float) Math.floor(pixelY / GRID_TILE_SIZE_PIXELS) + GRID_ORIGIN_TILE;
        return new Vector2(tileX, tileY);
    }

    private Vector2 applyIsometricTransform(float x, float y) {
        tmpVector.set(x, y, 0f).mul(isoDebugTransform);
        return new Vector2(tmpVector.x, tmpVector.y);
    }

    /**
     * Sets a vertex in the vertices array.
     */
    private void setVertex(float[] vertices, int index, float x, float y, Vector2 center) {
        Vector2 transformed = applyIsometricTransform(x, y);
        vertices[index] = transformed.x - center.x;
        vertices[index + 1] = transformed.y - center.y;
    }

    /**
     * Builds a body for a static object.
     */
    public Body buildBody(int tileX, int tileY, World world, int bodyWidth, int bodyHeight) {
        TiledMap tiledMap = getMap();
        buildIsometricDebugTransform(tiledMap);

        tileX -= (bodyWidth / 16 / 2);
        tileY -= (bodyHeight / 16 / 2);
        Rectangle rectangle = new Rectangle((tileX - 130) * 16, (tileY - 130) * 16, bodyWidth, bodyHeight);

        BodyDef bdef = new BodyDef(); // Definition for creating a physics body
        bdef.type = BodyDef.BodyType.StaticBody; // Make the body static (doesn't move)
        PolygonShape shape = new PolygonShape(); // Shape used for collision detection
        FixtureDef fdef = new FixtureDef(); // Fixture configuration for the body
        Body body; // Reference to the created body

        float width = rectangle.getWidth() / PPM;
        float height = rectangle.getHeight() / PPM;
        if (width <= 0 || height <= 0) return null;

        Vector2 rawCenter = tileToWorld(
                tiledMap,
                rectangle.getX() + rectangle.getWidth() / 2f,
                rectangle.getY() + rectangle.getHeight() / 2f
        );
        Vector2 worldPosition = applyIsometricTransform(rawCenter.x, rawCenter.y);
        bdef.position.set(worldPosition);


        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float[] vertices = new float[8];

        setVertex(vertices, 0, rawCenter.x - halfWidth, rawCenter.y - halfHeight, worldPosition);
        setVertex(vertices, 2, rawCenter.x + halfWidth, rawCenter.y - halfHeight, worldPosition);
        setVertex(vertices, 4, rawCenter.x + halfWidth, rawCenter.y + halfHeight, worldPosition);
        setVertex(vertices, 6, rawCenter.x - halfWidth, rawCenter.y + halfHeight, worldPosition);

        body = world.createBody(bdef);
        shape.set(vertices);
        fdef.shape = shape;
        body.createFixture(fdef);

        System.out.println(body.getPosition());

        return body;
    }

    /**
     * Builds a body for a static object with default width and height.
     */
    public Body buildBody(int tileX, int tileY, World world) {
        return buildBody(tileX, tileY, world, 16, 16);
    }

    private static Matrix4 createIsometricTransform(TiledMap tiledMap) {
        MapProperties props = tiledMap.getProperties();
        float tileWidth = props.get("tilewidth", Integer.class);
        float tileHeight = props.get("tileheight", Integer.class);
        float mapHeight = props.get("height", Integer.class);

        float originX = (mapHeight * tileWidth / 2f) / PPM;
        float scaleX = (float) Math.sqrt(2f);
        float scaleY = (float) Math.sqrt(2f);

        if (tileWidth > tileHeight) {
            scaleY *= tileHeight / tileWidth;
        } else {
            scaleX *= tileWidth / tileHeight;
        }

        return new Matrix4().idt()
                .translate(originX, 0f, 0f)
                .scale(scaleX, scaleY, 1f)
                .rotate(0f, 0f, 1f, DEBUG_ISO_ROTATION_DEGREES);
    }
}
