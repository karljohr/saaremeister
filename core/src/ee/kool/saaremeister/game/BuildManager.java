package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import ee.kool.saaremeister.network.ServerConnection;
import message.PlayerBuildMessage;
import message.dto.BuildingState;

import java.util.List;

public class BuildManager {

    private TiledMapTileLayer layer;
    private TiledMap map;
    private TiledMapTile tile;
    private Integer lastTileX;
    private Integer lastTileY;

    public BuildManager(TiledMap map) {
        lastTileX = null;
        lastTileY = null;
        this.map = map;
        layer = (TiledMapTileLayer) map.getLayers().get("temp");
    }

    public void handleInput(float mouseX, float mouseY) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handlePlacement(mouseX, mouseY);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            // Change building type
        }
    }

    /**
     * Renders a projection of the chosen building under players mouse
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     */
    public void renderProjection(float mouseX, float mouseY) {
        tile = map.getTileSets().getTileSet("offensive_buildings").getTile(148);

        if (lastTileX != null && lastTileY != null) {
            layer.setCell(lastTileX, lastTileY, null);
        }

        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(tile);

        int[] coordinates = mouseToIsoCoordinates(mouseX, mouseY);
        int tileX = coordinates[0];
        int tileY = coordinates[1];

        layer.setCell(tileX ,tileY, cell);

        lastTileX = tileX;
        lastTileY = tileY;

    }

    public void renderBatch(List<BuildingState> buildingStates) {
        for  (BuildingState building : buildingStates) {
            tile = map.getTileSets().getTileSet("offensive_buildings").getTile(building.id);

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tile);

            layer.setCell(building.x, building.y, cell);
        }
    }

    private void handlePlacement(float mouseX, float mouseY) {
        // TODO: Send Box2D body info to server
        PlayerBuildMessage message = new PlayerBuildMessage();
        message.id = 148;
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

}
