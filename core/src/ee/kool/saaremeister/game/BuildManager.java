package ee.kool.saaremeister.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import constant.ShopToTile;
import ee.kool.saaremeister.network.ServerConnection;
import ee.kool.saaremeister.screen.GameScreen;
import lombok.Setter;
import message.PlayerBuildMessage;
import message.dto.BuildingState;
import message.dto.ShopItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildManager {

    private TiledMapTileLayer layer;
    private TiledMap map;
    private TiledMapTile tile;
    private Integer lastTileX;
    private Integer lastTileY;
    private int selectedBuilding;
    private GameScreen game;

    public BuildManager(TiledMap map, GameScreen game) {
        lastTileX = null;
        lastTileY = null;
        this.map = map;
        layer = (TiledMapTileLayer) map.getLayers().get("temp");
        this.game = game;
    }

    public void handleInput(float mouseX, float mouseY) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handlePlacement(mouseX, mouseY);
            game.leaveBuildMode();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            // Change building type
        }
    }

    /**
     * Renders a projection of the chosen building under players mouse.
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     */
    public void renderProjection(float mouseX, float mouseY) {
        tile = map.getTileSets().getTileSet("offensive_buildings").getTile(selectedBuilding);

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
        message.id = selectedBuilding;
        int[] coordinates = mouseToIsoCoordinates(mouseX, mouseY);
        message.tileX = coordinates[0];
        message.tileY = coordinates[1];

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
        selectedBuilding = ShopToTile.getTileId(id);
    }

}
