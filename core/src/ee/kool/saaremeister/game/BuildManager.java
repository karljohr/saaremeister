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
import ee.kool.saaremeister.utility.SoundEffectManager;
import lombok.Getter;
import lombok.Setter;
import message.PlayerBuildMessage;
import message.RefundMessage;
import message.dto.BuildingState;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Handles the building process in the game.
 */
public class BuildManager {

    private int selectedBuilding;
    @Getter
    private int selectedVariation;
    private final GameScreen game;
    @Getter
    private List<BuildingState> currentBuildingStates;
    @Setter
    private PreviewRenderSource previewRenderSource;

    public BuildManager(GameScreen game) {
        this.game = game;
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
        SoundEffectManager.playBuild();
    }

    /**
     * Sets the selected building
     */
    public void setSelectedBuilding(int id) {
        selectedBuilding = BuildingInfo.getTileId(id);
        selectedVariation = selectedBuilding;
    }

    /**
     * Updates the building states and plays a sound effect if a new building is seen.
     */
    public void updateBuildingStates(List<BuildingState> buildingStates) {
        currentBuildingStates = buildingStates != null ? buildingStates : new ArrayList<>();
    }
}
