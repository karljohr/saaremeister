package ee.kool.saaremeister.server.game.object.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import common.ShopRegistry;
import constant.BuildingInfo;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.projectiles.Bullet;
import ee.kool.saaremeister.server.listener.BuildingListener;
import ee.kool.saaremeister.server.listener.PlayerMovementListener;
import ee.kool.saaremeister.server.listener.PlayerShootingListener;
import lombok.Getter;
import lombok.Setter;
import message.PlayerBuildMessage;
import message.dto.Direction;
import message.dto.PlayerRole;
import message.dto.PlayerState;
import message.dto.ShopItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static constant.Constants.*;

/**
 * The server-side representation of a player. Listens to client input
 * and updates the player state accordingly.
 */
@Getter
@Setter
public abstract class Player {

    private final Connection connection;
    private final PlayerMovementListener movementListener = new PlayerMovementListener(this);
    private final PlayerShootingListener shootingListener = new PlayerShootingListener(this);
    private final BuildingListener buildingListener = new BuildingListener(this);

    private final int id;
    private final GameInstance gameInstance;
    private float x, y = 0f;
    private boolean alive = true;
    private long respawnAtMs;
    private int lives;
    private int maxHealth;
    private int defense = 0;
    private int currentArmorLevel = 0; // 0 = no armor, 1 = leather, 2 = silver, 3 = diamond
    private int money;
    private PlayerRole role;
    private String username = "Player";
    private final Integer[] hotbarItemIds = new Integer[3];
    private long speedBoostExpiresAtMs = 0L;
    private long damageBoostExpiresAtMs = 0L;

    private final World world;
    private Body body;

    private final BuildHandler buildHandler;

    private List<Integer> buildingPermitById;

    /**
     * Initializes a new server/side representation of a standard player.
     */
    public Player(
            Connection connection,
            int id,
            GameInstance gameInstance,
            World world,
            BuildHandler buildHandler,
            int money,
            int lives,
            PlayerRole role
    ) {
        this.connection = connection;
        // For bots which dont have connection
        this.id = (connection != null) ? connection.getID() : id;
        this.gameInstance = gameInstance;
        this.world = world;
        this.money = money;
        this.lives = lives;
        this.maxHealth = this.lives;
        this.role = role;
        definePlayer();
        this.buildHandler = buildHandler;
        buildingPermitById = new ArrayList<>();

        // Add listeners only if player
        if (connection != null) {
            this.connection.addListener(movementListener);
            this.connection.addListener(shootingListener);
            this.connection.addListener(buildingListener);
        }
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
        shape.setAsBox(
                PLAYER_PIXEL_WIDTH / 4f / PPM,
                PLAYER_PIXEL_HEIGHT / 8f / PPM,
                new Vector2(0, -PLAYER_PIXEL_HEIGHT / 2f / PPM),
                0
        );

        fdef.shape = shape;
        body.createFixture(fdef);
        shape.dispose();
    }

    /**
     * Returns the player's state.
     */
    public PlayerState getPlayerState() {
        PlayerState playerState = new PlayerState();
        // Instead of connection.getID() use this.id for bots.
        playerState.setId(this.id);
        playerState.setX(body.getPosition().x * PPM);
        playerState.setY(body.getPosition().y * PPM);
        playerState.setLives(lives);
        playerState.setMaxHealth(maxHealth);
        playerState.setDefense(defense);
        playerState.setUsername(username);
        playerState.setMoney(this.money);
        playerState.setRole(role);
        playerState.setBot(this instanceof Bot);
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
        float speed = (PLAYER_SPEED * getSpeedMultiplier()) / PPM;
        Vector2 velocity = new Vector2(0, 0);

        if (directionX == Direction.LEFT) velocity.x = -speed;
        else if (directionX == Direction.RIGHT) velocity.x = speed;

        if (directionY == Direction.UP) velocity.y = speed;
        else if (directionY == Direction.DOWN) velocity.y = -speed;

        // Normalize moving diagonally
        if (velocity.x != 0 && velocity.y != 0) {
            velocity.scl(0.707f);
        }

        body.setLinearVelocity(velocity);
    }

    /**
     * Converts player location from Box2D to simple coordinates.
     */
    public void updateFromPhysics() {
        if (body == null) return;
        this.x = body.getPosition().x * PPM;
        this.y = body.getPosition().y * PPM;
    }

    /**
     * Decreases the player's lives.
     */
    public void decreaseLives(int damage) {
        if (lives > 0) {
            setLives(Math.max(0, lives - damage));
        }
    }

    public void takeBulletDamage(float baseDamage) {
        float reducedDamage = baseDamage * Math.max(0f, 1f - (defense / 100f));
        int appliedDamage = Math.max(1, Math.round(reducedDamage));
        decreaseLives(appliedDamage);
    }

    /**
     * Builds a building.
     *
     * @param message info about the building object to be built. Sent by player.
     */
    public void build(PlayerBuildMessage message) {
        buildHandler.build(message, this);
    }

    /**
     * Shoot a bullet depending on the mouse position.
     */
    public void shoot(Vector2 aim) {
        gameInstance.addBullet(new Bullet(x, y, aim.cpy().nor(), id));
    }

    public float getBulletDamage() {
        return BULLET_DAMAGE * getDamageMultiplier();
    }

    private float getSpeedMultiplier() {
        return isSpeedBoostActive() ? SPEED_POTION_MULTIPLIER : 1f;
    }

    private float getDamageMultiplier() {
        return isDamageBoostActive() ? STRENGTH_POTION_MULTIPLIER : 1f;
    }

    private boolean isSpeedBoostActive() {
        return System.currentTimeMillis() < speedBoostExpiresAtMs;
    }

    private boolean isDamageBoostActive() {
        return System.currentTimeMillis() < damageBoostExpiresAtMs;
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
        return hotbarItemIds[1] != null && hotbarItemIds[2] != null;
    }

    public boolean addHotbarItem(ShopItem item) {
        int slot = getTargetHotbarSlot(item);
        if (slot < 0) {
            return false;
        }
        hotbarItemIds[slot] = item.getId();
        return true;
    }

    private int getTargetHotbarSlot(ShopItem item) {
        if (item.getCategory() == constant.Category.RELVAD) {
            return 0;
        }

        if (item.getCategory() == constant.Category.TARVIKUD && !item.getName().contains("rüü")) {
            if (hotbarItemIds[1] == null) return 1;
            if (hotbarItemIds[2] == null) return 2;
        }

        return -1;
    }

    public Integer getHotbarItemId(int slot) {
        if (slot < 0 || slot >= hotbarItemIds.length) {
            return null;
        }
        return hotbarItemIds[slot];
    }

    public boolean canShootFromSlot(int selectedSlot) {
        return selectedSlot == 0 && Objects.equals(hotbarItemIds[0], 1);
    }

    public void clearHotbarSlot(int slot) {
        if (slot < 0 || slot >= hotbarItemIds.length) {
            return;
        }
        hotbarItemIds[slot] = null;
        Log.info(username + " kasutas eseme slotist " + slot + ".");
    }

    public void consumeHealthPotion() {
        setLives(Math.min(maxHealth, lives + HEALTH_POTION_HEAL));
    }

    public void consumeSpeedPotion() {
        speedBoostExpiresAtMs = System.currentTimeMillis() + SPEED_POTION_DURATION_MILLISECONDS;
    }

    public void consumeStrengthPotion() {
        damageBoostExpiresAtMs = System.currentTimeMillis() + STRENGTH_POTION_DURATION_MILLISECONDS;
    }

    public void resetCombatBonuses() {
        speedBoostExpiresAtMs = 0L;
        damageBoostExpiresAtMs = 0L;
    }

    /**
     * Gives the player the building permit for the building after purchase.
     */
    public void addBuildingPermit(int id) {
        buildingPermitById.add(id);
    }

    public void removeBuildingPermit(int id) {
        buildingPermitById.remove((Object) id);
    }

    /**
     * Gives the player the money back after cancelling building.
     *
     * @param id tileId of the building.
     */
    public void refundBuildingPermit(int id) {
        removeBuildingPermit(id);
        money += Objects.requireNonNull(ShopRegistry
                        .getItemById(
                                BuildingInfo.getShopId(
                                        BuildingInfo.getBuildingFromVariation(id)
                                )
                        ))
                .getPrice();
    }

    /**
     * Removes the player's listeners from the connection (when the game ends).
     */
    public void dispose() {
        connection.removeListener(movementListener);
        connection.removeListener(shootingListener);
        connection.removeListener(buildingListener);
    }

    public abstract void earnFromKill();

    public abstract void earnFromDestroying();

    public abstract void passiveIncome(int zoneCount);
}
