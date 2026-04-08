package ee.kool.saaremeister.server.game.object;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.buildings.DefenceBuilding;
import ee.kool.saaremeister.server.game.object.buildings.OffenceBuilding;
import ee.kool.saaremeister.server.listener.BuildingListener;
import ee.kool.saaremeister.server.listener.PlayerMovementListener;
import ee.kool.saaremeister.server.listener.PlayerShootingListener;
import lombok.Getter;
import lombok.Setter;

import message.PlayerBuildMessage;
import message.dto.BuildingState;
import message.dto.Direction;
import message.dto.PlayerRole;
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
    private int lives;
    private int defense = 0;
    private int currentArmorLevel = 0; // 0 = no armor, 1 = leather, 2 = silver, 3 = diamond
    private int money;
    private PlayerRole role;
    private String username = "Player";
    private int hotbarItemCount = 0;

    private final World world;
    private Body body;

    private final BuildHandler buildHandler;

    private List<Integer> buildingPermitById;

    /**
     * Initializes a new server/side representation of a standard player.
     */
    public Player(
            Connection connection,
            GameInstance gameInstance,
            World world,
            BuildHandler buildHandler,
            int money,
            int lives,
            PlayerRole role
    ) {
        this.connection = connection;
        this.id = connection.getID();
        this.gameInstance = gameInstance;
        this.connection.addListener(movementListener);
        this.connection.addListener(shootingListener);
        this.connection.addListener(buildingListener);
        this.world = world;
        this.money = money;
        this.lives = lives;
        this.role = role;
        definePlayer();
        this.buildHandler = buildHandler;
        buildingPermitById = new ArrayList<>();
    }

    /**
     * Creates a Box2D body for the player.
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
        playerState.setDefense(defense);
        playerState.setUsername(username);
        playerState.setMoney(this.money);
        playerState.setRole(role);
        return playerState;
    }

    public void applyArmor(int level, int defBoost) {
        if (level > this.currentArmorLevel) {
            this.currentArmorLevel = level;
            this.defense = defBoost;
            Log.info(username + " varustus paranes! Uus tase: " + level + ", Kaitse: " + defense);
        }
    }

    /**
     * Moves the player in the given direction.
     */
    public void move(Direction directionX, Direction directionY) {
        float speed;
        if (directionX == null && directionY == null) {
            return;
        } else if (directionX != null && directionY != null) {
            speed = (float) (PLAYER_SPEED / Math.sqrt(2) / PPM);
        } else {
            speed = PLAYER_SPEED / PPM;
        }

        switch (directionX) {
            case LEFT -> body.setLinearVelocity(-speed, body.getLinearVelocity().y);
            case RIGHT -> body.setLinearVelocity(speed, body.getLinearVelocity().y);
            case null, default -> {
            }
        }
        switch (directionY) {
            case UP -> body.setLinearVelocity(body.getLinearVelocity().x, speed);
            case DOWN -> body.setLinearVelocity(body.getLinearVelocity().x, -speed);
            case null, default -> {
            }
        }

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

    /**
     * Decreases the player's lives.
     */
    public void decreaseLives() {
        if (lives > 0) {
            setLives(lives - 1);
        }
    }

    /**
     *
     * TODO: This function must be unique to the island master.
     * @param message info about the building object to be built. Sent by player.
     */
    public void build(PlayerBuildMessage message) {
        buildHandler.build(message, this);
    }

    public void shoot(Direction direction) {
        gameInstance.addBullet(
                new Bullet(x, y, direction, id)
        );
        Log.info("spawning bullet for " + id + " at " + x + "," + y + " dir=" + direction);
    }

    /**
     * Attempts to buy an item.
     *
     * @return true if purchase successful
     */
    public boolean attemptPurchase(int cost) {
        if (this.money >= cost) {
            this.money -= cost;
            return true;
        }
        return false;
    }

    public boolean isHotbarFull() {
        return hotbarItemCount >= 3;
    }

    public void addHotbarItem() {
        this.hotbarItemCount++;
    }

    public void removeHotbarItem() {
        if (this.hotbarItemCount > 0) {
            this.hotbarItemCount--;
            Log.info(username + " kasutas eseme. Hotbaris nüüd ruumi: " + (3 - hotbarItemCount));
        }
    }

    /**
     * Gives the player the building permit for the building after purchase.
     * @param id
     */
    public void addBuildingPermit(int id) {
        buildingPermitById.add(id);
    }

    public void removeBuildingPermit(int id) {
        buildingPermitById.remove((Object) id);
    }

    /**
     * Removes the player's listeners from the connection (when the game ends).
     */
    public void dispose() {
        connection.removeListener(movementListener);
        connection.removeListener(shootingListener);
        connection.removeListener(buildingListener);
    }
}
