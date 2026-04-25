package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.gamescreen.GameScreen;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PreviewRenderSource;
import ee.kool.saaremeister.utility.IsoMath;
import lombok.Getter;
import lombok.Setter;
import message.PlayerBuildMessage;
import message.RefundMessage;
import message.dto.BuildingState;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the building process in the game.
 */
public class BuildManager {

    private final TiledMap map;
    private TiledMapTile tile;
    private int selectedBuilding;
    @Getter
    private int selectedVariation;
    private final GameScreen game;
    private final Map<Integer, Integer> previousHealthById;
    private final Map<Integer, Long> healthBarVisibleUntilById;
    private List<BuildingState> currentBuildingStates;
    @Setter
    private PreviewRenderSource previewRenderSource;

    // Health bar
    private static final int BAR_WIDTH = 20;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_X_OFFSET_FROM_BUILDING = -(BAR_WIDTH / 2);
    private static final int BAR_Y_OFFSET_FROM_BUILDING = 40;
    private static final int HEALTH_BAR_VISIBLE_FOR_SECONDS = 2;

    public BuildManager(TiledMap map, GameScreen game) {
        this.map = map;
        this.game = game;

        previousHealthById =  new HashMap<>();
        healthBarVisibleUntilById = new HashMap<>();

        currentBuildingStates = new ArrayList<>();
    }

    /**
     * Handles input for the build manager. If the left mouse button is pressed, the building process is started. If
     * the T key is pressed, the next building variation is selected.
     */
    public void handleInput(float mouseX, float mouseY) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handlePlacement(mouseX, mouseY);
            game.leaveBuildMode();
            previewRenderSource.clearPreview();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            List<Integer> variations = BuildingInfo.getBuildingVariations(selectedBuilding);
            selectedVariation = variations.get(
                    variations.indexOf(selectedVariation) != variations.size() - 1
                            ? variations.indexOf(selectedVariation) + 1 : 0
            );
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.leaveBuildMode();
            previewRenderSource.clearPreview();
            RefundMessage msg = new RefundMessage();
            msg.setId(selectedVariation);
            ServerConnection
                    .getInstance()
                    .getClient()
                    .sendTCP(msg);
        }
    }

    /**
     * Sends a message to the server to build the chosen building.
     */
    private void handlePlacement(float mouseX, float mouseY) {
        PlayerBuildMessage message = new PlayerBuildMessage();
        message.id = selectedVariation;
        GridPoint2 coordinates = IsoMath.worldToTile(mouseX, mouseY);
        message.tileX = coordinates.x;
        message.tileY = coordinates.y;
        message.worldX = (int) mouseX;
        message.worldY = (int) mouseY;

        ServerConnection
                .getInstance()
                .getClient()
                .sendTCP(message);
    }

    /**
     * Sets the selected building
     */
    public void setSelectedBuilding(int id) {
        selectedBuilding = BuildingInfo.getTileId(id);
        selectedVariation = selectedBuilding;
    }

    /**
     * Compares buildings new health to old one and requests health bar rendering if health has changed.
     * @param buildingStates message from the server that includes information about the buildings.
     *                       A list of buildingStates.
     */
    public void compareBuildingOldHealthToNew(List<BuildingState> buildingStates) {
        currentBuildingStates = buildingStates;

        for (BuildingState building : buildingStates) {

            if (building.maxHealth == 0) continue;
            Integer oldHealth = previousHealthById.get(building.getUniqueId());

            if (oldHealth != null && building.getHealth() < oldHealth) {
                healthBarVisibleUntilById.put(
                        building.getUniqueId(),
                        System.currentTimeMillis() + HEALTH_BAR_VISIBLE_FOR_SECONDS * 1000
                );
            }

            previousHealthById.put(building.getUniqueId(), building.getHealth());
        }
    }

    /**
     * Renders the health bar of the building if it has taken damage.
     * @param shapeRenderer gameScreen shapeRenderer.
     */
    public void renderHealthBars(ShapeRenderer shapeRenderer) {
        for (BuildingState building : currentBuildingStates) {
            if (building.maxHealth == 0) continue;
            Long visibleUntil = healthBarVisibleUntilById.get(building.uniqueId);
            if (visibleUntil != null && System.currentTimeMillis() < visibleUntil) {
                float ratio = (float) building.health / building.maxHealth;

                shapeRenderer.setColor(Color.DARK_GRAY);
                shapeRenderer.rect(
                        building.worldX + BAR_X_OFFSET_FROM_BUILDING,
                        building.getWorldY() + BAR_Y_OFFSET_FROM_BUILDING, BAR_WIDTH, BAR_HEIGHT
                );

                shapeRenderer.setColor(
                        ratio > 0.5f
                                ? Color.GREEN : ratio > 0.25f
                                ? Color.ORANGE : Color.RED);
                shapeRenderer.rect(
                        building.worldX + BAR_X_OFFSET_FROM_BUILDING,
                        building.worldY + BAR_Y_OFFSET_FROM_BUILDING, BAR_WIDTH * ratio, BAR_HEIGHT
                );
            }
        }
    }

}
