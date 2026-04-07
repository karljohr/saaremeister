package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import constant.BuildingInfo;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.GameScreen;
import message.PlayerBuildMessage;
import message.dto.BuildingState;

import java.util.ArrayList;
import java.util.List;

public class BuildManager {

    private TiledMapTileLayer layer;
    private TiledMap map;
    private TiledMapTile tile;
    private List<BuildingState> lastTiles;
    private int selectedBuilding;
    private int selectedVariation;
    private GameScreen game;


    public BuildManager(TiledMap map, GameScreen game) {
        lastTiles =  new ArrayList<>();
        this.map = map;
        this.game = game;
        layer = (TiledMapTileLayer) map.getLayers().get("temp");
    }

    public void handleInput(float mouseX, float mouseY) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handlePlacement(mouseX, mouseY);
            game.leaveBuildMode();
            deleteProjection();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            List<Integer> variations = BuildingInfo.getBuildingVariations(selectedBuilding);
            selectedVariation = variations.get(
                    variations.indexOf(selectedVariation) != variations.size() - 1
                            ? variations.indexOf(selectedVariation) + 1 : 0
            );
        }
    }

    /**
     * Renders a projection of the chosen building under players mouse
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

        if (BuildingInfo.getBuildingVariations(156).contains(selectedVariation)) {
            for (BuildingState building :  BuildingInfo.getWallTileLocations(new BuildingState(selectedVariation, tileX, tileY))) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                tile = map.getTileSets().getTile(building.id);
                cell.setTile(tile);
                layer.setCell(building.getX() ,building.getY(), cell);
                lastTiles.add(building);
            }
        } else {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tile);
            layer.setCell(tileX ,tileY, cell);
            lastTiles.add(new BuildingState(selectedVariation, tileX, tileY));
        }

    }

    private void deleteProjection() {
        for (BuildingState lastTile : lastTiles) {
            layer.setCell((int) lastTile.x, (int) lastTile.y, null);
        }
    }

    public void renderBatch(List<BuildingState> buildingStates) {
        if (buildingStates == null || buildingStates.isEmpty()) return;
        for  (BuildingState building : buildingStates) {
            tile = map.getTileSets().getTile(building.getId());

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tile);

            layer.setCell(building.getX(), building.getY(), cell);
        }
    }

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

    private int[] mouseToIsoCoordinates(float mouseX, float mouseY) {
        float x = mouseX - layer.getRenderOffsetX() - 16;
        float y = -(mouseY - layer.getRenderOffsetY() - 16);

        float halfTileWidth = layer.getTileWidth() / 2f;
        float halfTileHeight = layer.getTileHeight() / 2f;

        int tileX = (int) Math.floor((x / halfTileWidth + y / halfTileHeight) / 2f);
        int tileY = (int) -Math.floor((y / halfTileHeight - x / halfTileWidth) / 2f);

        return new int[]{tileX, tileY};
    }

    public void setSelectedBuilding(int id) {
        selectedBuilding = BuildingInfo.getTileId(id);
        selectedVariation = selectedBuilding;
    }

    private void handleWallPlacement() {

    }



}
