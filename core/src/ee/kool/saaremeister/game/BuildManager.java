package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.gamescreen.GameScreen;
import ee.kool.saaremeister.screen.gamescreen.render.sources.PreviewRenderSource;
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

    private final TiledMapTileLayer layer;
    private final TiledMap map;
    private TiledMapTile tile;
    private final List<BuildingState> lastTiles;
    private int selectedBuilding;
    @Getter
    private int selectedVariation;
    private final GameScreen game;
    private final Map<Integer, Integer> previousHealthById;
    private final Map<Integer, Long> healthBarVisibleUntilById;
    private List<BuildingState> currentBuildingStates;
    private List<BuildingState> renderedBuildings;
    @Setter
    private PreviewRenderSource previewRenderSource;

    // Health bar
    private static final int BAR_WIDTH = 20;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_X_OFFSET_FROM_BUILDING = -(BAR_WIDTH / 2);
    private static final int BAR_Y_OFFSET_FROM_BUILDING = 40;
    private static final int HEALTH_BAR_VISIBLE_FOR_SECONDS = 2;

    public BuildManager(TiledMap map, GameScreen game) {
        lastTiles = new ArrayList<>();
        this.map = map;
        this.game = game;
        layer = (TiledMapTileLayer) map.getLayers().get("temp");

        previousHealthById =  new HashMap<>();
        healthBarVisibleUntilById = new HashMap<>();

        renderedBuildings = new ArrayList<>();
        currentBuildingStates = new ArrayList<>();
    }

    /**
     * Handles input for the build manager. If the left mouse button is pressed, the building process is started. If
     * the T key is pressed, the next building variation is selected.
     */
    public void handleInput(float mouseX, float mouseY) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handlePlacement(mouseX, mouseY);
            Log.info("Mouse at: " +  mouseX + ", " + mouseY);
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
     * Renders a projection of the chosen building under the player mouse
     *
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     */
    public void renderProjection(float mouseX, float mouseY) {
        tile = map.getTileSets().getTile(selectedVariation
        );

        deleteProjection();

        int[] coordinates = mouseToIsoCoordinates(mouseX, mouseY);
        int tileX = coordinates[0];
        int tileY = coordinates[1];

        if (BuildingInfo.getBuildingVariations(
                BuildingInfo.getBuildingId(
                        BuildingInfo.BuildingName.WALLS)).contains(selectedVariation)
            ) {
            for (BuildingState building : BuildingInfo.getWallTileLocations(new BuildingState(selectedVariation, tileX, tileY))) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                tile = map.getTileSets().getTile(building.id);
                cell.setTile(tile);
                layer.setCell(building.getX(), building.getY(), cell);
                lastTiles.add(building);
            }
        } else {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tile);
            layer.setCell(tileX, tileY, cell);
            lastTiles.add(new BuildingState(selectedVariation, tileX, tileY));
        }

    }

    /**
     * Deletes the projection of the chosen building
     */
    private void deleteProjection() {
        for (BuildingState lastTile : lastTiles) {
            layer.setCell((int) lastTile.x, (int) lastTile.y, null);
        }
    }

    /**
     * Renders a batch of buildings
     */
    public void renderBatch(List<BuildingState> buildingStates) {

        for (BuildingState building : renderedBuildings) {
            layer.setCell(building.getX(), building.getY(), null);
        }

        renderedBuildings = buildingStates;

        for (BuildingState building : buildingStates) {
            tile = map.getTileSets().getTile(building.getId());

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tile);

            layer.setCell(building.getX(), building.getY(), cell);
        }
    }

    /**
     * Sends a message to the server to build the chosen building.
     */
    private void handlePlacement(float mouseX, float mouseY) {
        PlayerBuildMessage message = new PlayerBuildMessage();
        message.id = selectedVariation;
        int[] coordinates = mouseToIsoCoordinates(mouseX, mouseY);
        message.tileX = coordinates[0];
        message.tileY = coordinates[1];
        message.worldX = (int) mouseX;
        message.worldY = (int) mouseY;

        ServerConnection
                .getInstance()
                .getClient()
                .sendTCP(message);
    }

    /**
     * Converts mouse coordinates to tile coordinates
     */
    private int[] mouseToIsoCoordinates(float mouseX, float mouseY) {
        float x = mouseX - layer.getRenderOffsetX() - 16;
        float y = -(mouseY - layer.getRenderOffsetY() - 16);

        float halfTileWidth = layer.getTileWidth() / 2f;
        float halfTileHeight = layer.getTileHeight() / 2f;

        int tileX = (int) Math.floor((x / halfTileWidth + y / halfTileHeight) / 2f);
        int tileY = (int) -Math.floor((y / halfTileHeight - x / halfTileWidth) / 2f);

        return new int[]{tileX, tileY};
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
