package ee.kool.saaremeister.server.game.object;

import com.badlogic.gdx.physics.box2d.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.listener.BuildingListener;
import ee.kool.saaremeister.server.listener.PlayerMovementListener;
import ee.kool.saaremeister.server.listener.PlayerShootingListener;
import lombok.Getter;
import lombok.Setter;

import message.PlayerBuildMessage;
import message.dto.Direction;
import message.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.*;

/**
 * The server-side representation of a player. Listens to client input
 * and updates the player state accordingly.
 */
@Getter
@Setter
public class Player {

    private final Connection connection;
    private final PlayerMovementListener movementListener = new PlayerMovementListener(this);
    private final PlayerShootingListener shootingListener = new PlayerShootingListener(this);
    private final BuildingListener buildingListener = new BuildingListener(this);

    private final int id;
    private final GameInstance gameInstance;
    private float x, y = 0f;
    private int lives = PLAYER_LIVES;
    private int money;
    private String username = "Player";

    private final World world;
    private Body body;

    private final BuildHandler buildHandler;

    private List<Integer> buildingPermitById;

    /**
     * Initializes a new server/side representation of a player.
     */
    public Player(Connection connection, GameInstance gameInstance, World world, BuildHandler buildHandler) {
        this.connection = connection;
        this.id = connection.getID();
        this.gameInstance = gameInstance;
        this.connection.addListener(movementListener);
        this.connection.addListener(shootingListener);
        this.connection.addListener(buildingListener);
        this.world = world;
        this.money = PLAYER_MONEY;
        definePlayer();
        this.buildHandler = buildHandler;
        buildingPermitById = new ArrayList<>();
    }

    /**
     * Creates a Box2D body for player.
     */
    private void definePlayer() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x / PPM, y / PPM); // Starts at a visible position and not in a collision box
        bdef.type = BodyDef.BodyType.DynamicBody; // Dynamic body not static
        bdef.fixedRotation = true; // don't tip / rotate when colliding

        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        // Keep player friction low to avoid grabbing onto vertical walls.
        fdef.friction = 0.0f;
        fdef.density = 1.0f;

        PolygonShape shape = new PolygonShape(); // Also circleShapes, PolygonShapes, etc.
        // Adjust hit-box size as needed.
        shape.setAsBox(10 / PPM, 15 / PPM);

        fdef.shape = shape;
        body.createFixture(fdef);
        shape.dispose();
    }

    /**
     * Returns the player's state.
     */
    public PlayerState getPlayerState() {
        PlayerState playerState = new PlayerState();
        playerState.setId(connection.getID());
        playerState.setX(body.getPosition().x * PPM);
        playerState.setY(body.getPosition().y * PPM);
        playerState.setLives(lives);
        playerState.setUsername(username);
        playerState.setMoney(this.money);
        return playerState;
    }

    /**
     * Moves the player in the given direction.
     */
    public void move(Direction directionX, Direction directionY) {
        if (directionX == null && directionY == null) {
            return;
        }


        switch (directionX) {
            case LEFT -> body.setLinearVelocity(-PLAYER_SPEED / PPM, body.getLinearVelocity().y);
            case RIGHT ->  body.setLinearVelocity(PLAYER_SPEED / PPM, body.getLinearVelocity().y);
            case null, default -> {}
        }
        switch (directionY) {
            case UP -> body.setLinearVelocity(body.getLinearVelocity().x, PLAYER_SPEED / PPM);
            case DOWN -> body.setLinearVelocity(body.getLinearVelocity().x, -PLAYER_SPEED / PPM);
            case null, default -> {}
        }

        System.out.println(body.getPosition());
    }

    /**
     * Converts player location from Box2D to simple coordinates.
     */
    public void updateFromPhysics() {
        if (body == null) return;
        this.x = body.getPosition().x * PPM;
        this.y = body.getPosition().y * PPM;
        body.setLinearVelocity(0f, 0f);
    }

    public void decreaseLives() {
        if (lives > 0) {
            setLives(lives - 1);
        }
    }

    /**
     *
     * TODO: This function must be unique to the tax officer.
     * @param message info about the building object to be built. Sent by player.
     */
    public void build(PlayerBuildMessage message) {
        if (buildingPermitById.contains(message.id)) {
            buildHandler.build(
                    this,
                    new Building(message.id, message.tileX, message.tileY, world)
            );
            buildingPermitById.remove((Object) message.id);
        } else {
            Log.info("Player doesn't have the right to build this building: " + message.id + " not in " + buildingPermitById);
        }
    }

    public void shoot(Direction direction) {
        gameInstance.addBullet(
                new Bullet(x, y, direction, id)
        );
        Log.info("spawning bullet for " + id + " at " + x + "," + y + " dir=" + direction);
    }

    /**
     * Attempts to buy an item.
     * @return true if purchase successful
     */
    public boolean attemptPurchase(int cost) {
        if (this.money >= cost) {
            this.money -= cost;
            return true;
        }
        return false;
    }

    /**
     * Gives the player the building permit for the building after purchase.
     * @param id
     */
    public void addBuildingPermit(int id) {
        buildingPermitById.add(id);
    }

    /**
     * Removes the player's listeners from the connection (when the game ends).
     */
    public void dispose() {
        connection.removeListener(movementListener);
        connection.removeListener(shootingListener);
    }
}
