package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import ee.kool.saaremeister.server.game.object.CaptureZone;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.PPM;

/**
 * Loads the capture zones into the game from the TMX map.
 */
public class CaptureZoneLoader {

    private static final float ISO_ROTATION_DEGREES = 135f;

    /**
     * Loads the capture zones from the TMX map.
     */
    public List<CaptureZone> loadCaptureZones() {
        List<CaptureZone> zones = new ArrayList<>();

        // Get the capture zone layer from the Tiled map.
        TiledMap map = Box2dWorldGenerator.getMap();
        MapLayer layer = map.getLayers().get("capture_zones");
        if (layer == null) {
            throw new IllegalStateException("Missing TMX object layer: capture_zones");
        }

        Matrix4 isoTransform = buildIsometricTransform(map);
        Vector3 tmp = new Vector3();

        // Load the capture zones.
        for (RectangleMapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
            Integer zoneId = object.getProperties().get("id", Integer.class);
            if (zoneId == null) {
                throw new IllegalStateException("Each capture zone must have int property: id");
            }

            Rectangle r = object.getRectangle();

            // Convert all 4 corners from TMX pixel space -> server world pixel space.
            Vector2 p1 = mapPixelToWorldPixels(map, isoTransform, tmp, r.x, r.y);
            Vector2 p2 = mapPixelToWorldPixels(map, isoTransform, tmp, r.x + r.width, r.y);
            Vector2 p3 = mapPixelToWorldPixels(map, isoTransform, tmp, r.x + r.width, r.y + r.height);
            Vector2 p4 = mapPixelToWorldPixels(map, isoTransform, tmp, r.x, r.y + r.height);

            // Keep the transformed isometric quad (clockwise order).
            float[] polygonVertices = new float[]{
                    p1.x, p1.y,
                    p2.x, p2.y,
                    p3.x, p3.y,
                    p4.x, p4.y
            };

            zones.add(new CaptureZone(zoneId, polygonVertices));
        }

        return zones;
    }

    /**
     * Builds a transform that converts from TMX pixel space to server world pixel space.
     */
    private Matrix4 buildIsometricTransform(TiledMap map) {
        MapProperties props = map.getProperties();
        float tileWidth = props.get("tilewidth", Integer.class);
        float tileHeight = props.get("tileheight", Integer.class);
        float mapHeight = props.get("height", Integer.class);

        // The origin is in the center of the map.
        float originX = (mapHeight * tileWidth / 2f) / PPM;
        float scaleX = (float) Math.sqrt(2f);
        float scaleY = (float) Math.sqrt(2f);

        // Scale the map to fit the screen.
        if (tileWidth > tileHeight) {
            scaleY *= tileHeight / tileWidth;
        } else {
            scaleX *= tileWidth / tileHeight;
        }

        // Rotate the map to be isometric.
        return new Matrix4().idt()
                .translate(originX, 0f, 0f)
                .scale(scaleX, scaleY, 1f)
                .rotate(0f, 0f, 1f, ISO_ROTATION_DEGREES);
    }

    /**
     * Converts a pixel coordinate from TMX pixel space to server world pixel space.
     */
    private Vector2 mapPixelToWorldPixels(TiledMap map, Matrix4 isoTransform, Vector3 tmp, float pixelX, float pixelY) {
        Vector2 rawMeters = Box2dWorldGenerator.tileToWorld(map, pixelX, pixelY);
        tmp.set(rawMeters.x, rawMeters.y, 0f).mul(isoTransform);
        return new Vector2(tmp.x * PPM, tmp.y * PPM);
    }

}
