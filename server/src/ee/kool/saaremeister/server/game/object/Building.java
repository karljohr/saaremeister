package ee.kool.saaremeister.server.game.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.Box2dWorldGenerator;
import ee.kool.saaremeister.server.game.GameInstance;
import lombok.Getter;
import message.dto.BuildingState;
import message.dto.Direction;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;

import static constant.Constants.PPM;


@Getter
public class Building {
    private final int uniqueId;
    private final int id;
    private final int tileX;
    private final int tileY;
    private final float x;
    private final float y;
    private final World world;
    private final int shootingFrequency = 650;
    private LocalTime lastShotTime = LocalTime.MIN;

    private Body body;

    private final Matrix4 isoDebugTransform = new Matrix4();
    private final Vector3 tmpVector = new Vector3();

    private static final int TILE_SIZE = 32;

    private final GameInstance gameInstance;

    private static int i = 100;

    public Building(int id, int tileX, int tileY, World world, GameInstance gameInstance) {
        this.uniqueId = getIdAndIncrementI();
        this.id = id;
        this.tileX = tileX;
        this.tileY = tileY;
        this.world = world;
        this.gameInstance = gameInstance;


        defineBody();

        this.x = body.getPosition().x * PPM;
        this.y = body.getPosition().y * PPM;
    }

    public void defineBody() {
        Box2dWorldGenerator generator = new Box2dWorldGenerator();
        body = generator.buildBody(tileX, tileY, world, id);
        System.out.println("Body position: " + body.getPosition());
    }

    public BuildingState getBuildingState() {
        BuildingState buildingState = new BuildingState();
        buildingState.id = id;
        buildingState.x = tileX;
        buildingState.y = tileY;
        return buildingState;
    }

    public void shoot() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(lastShotTime.plus(Duration.ofMillis(shootingFrequency)))) {
            return;
        }
        lastShotTime = now;

        Player closestPlayer = findClosestPlayer();
        if (closestPlayer == null) {
            return;
        }

        Vector2 aim = buildingToPlayerAim(closestPlayer);

        gameInstance.addBullet(
                new Bullet(x, y, aim, uniqueId)
        );
        Log.info("spawning bullet for " + uniqueId + " at " + x + "," + y + " dir(vector)=" + aim);
    }

    private Player findClosestPlayer() {
        return gameInstance
                .getPlayers()
                .stream()
                .min(Comparator.comparingInt(player -> (int) ((
                        (player.getX() - x) * (player.getX() - x))
                        + ((player.getY() - y) * (player.getY() - y)))
                ))
                .orElse(null);
    }

    private Vector2 buildingToPlayerAim(Player player) {
        float vectorX = player.getX() - x;
        float vectorY = player.getY() - y;

        Vector2 aim = new Vector2(vectorX, vectorY);
        if (aim.len2() == 0) {
            return new Vector2(0, 0);
        }

        return aim.nor();
    }

    private static int getIdAndIncrementI() {
        return i++;
    }

}
