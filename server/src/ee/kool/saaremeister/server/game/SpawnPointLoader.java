package ee.kool.saaremeister.server.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import ee.kool.saaremeister.server.game.object.Player;
import message.dto.PlayerRole;

import java.util.*;

import static constant.Constants.PPM;

public class SpawnPointLoader {

    private static final float ISO_ROTATION_DEGREES = 135f;

    /**
     * Loads the spawn points from the TMX map.
     */
    public Map<PlayerRole, List<Vector2>> loadSpawnPoints() {
        Map<PlayerRole, List<Vector2>> spawnpoints = Map.of(
                PlayerRole.MASTER, new ArrayList<>(),
                PlayerRole.OFFICER, new ArrayList<>()
        );

        // Get the capture zone layer from the Tiled map.
        TiledMap map = Box2dWorldGenerator.getMap();
        MapLayer layer = map.getLayers().get("spawnpoints");
        if (layer == null) {
            throw new IllegalStateException("Missing TMX object layer: spawnpoints");
        }

        Matrix4 isoTransform = buildIsometricTransform(map);
        Vector3 tmp = new Vector3();

        // Load the capture zones.
        for (RectangleMapObject object : layer.getObjects().getByType(RectangleMapObject.class)) {
            PlayerRole role;
            if (object.getProperties().get("role").equals("MASTER")) {
                role = PlayerRole.MASTER;
            } else {
                role = PlayerRole.OFFICER;
            }

            Rectangle r = object.getRectangle();

            Vector2 location = mapPixelToWorldPixels(map, isoTransform, tmp, r.x, r.y);

            spawnpoints.get(role).add(location);
        }

        return spawnpoints;
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

    /**
     * Sets a random spawnpoint to the player according to his role.
     * @param role PlayerRole.
     */
    public static void setRandomSpawnPoint(
            Player player,
            PlayerRole role,
            Map<PlayerRole, List<Vector2>> spawnPoints
    ) {
        Random random = new Random();
        int ranNum = random.nextInt(0, spawnPoints.get(role).size());
        Vector2 playerStartingLocation = spawnPoints.get(role).get(ranNum);
        player.setX(playerStartingLocation.x);
        player.setY(playerStartingLocation.y);
        player.getBody().setTransform(
                playerStartingLocation.x / PPM,
                playerStartingLocation.y / PPM,
                0f
        );
    }
}
