package ee.kool.saaremeister.screen.gamescreen.render.sources;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import constant.BuildingInfo;
import constant.Constants;
import ee.kool.saaremeister.game.BuildManager;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.DepthRenderable;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.TileRenderPiece;
import ee.kool.saaremeister.utility.IsoMath;
import message.dto.BuildingState;

import java.util.*;

/**
 * Converts player mouse location and chosen building into depth-sortable renderables.
 */
public class PreviewRenderSource {
    private final List<DepthRenderable> renderables;
    private final TiledMap map;
    private final BuildManager buildManager;

    public PreviewRenderSource(TiledMap map, BuildManager buildManager) {
        this.map = map;
        renderables = new ArrayList<>();
        this.buildManager = buildManager;
        this.buildManager.setPreviewRenderSource(this);
    }

    /**
     * Rebuilds the building preview renderables from the BuildManager.
     */
    public void update(int mouseX, int mouseY) {
        renderables.clear();
        int selectedVariation = buildManager.getSelectedVariation();

        GridPoint2 tileCoordinates = IsoMath.worldToTile(mouseX, mouseY);

        if (isWallTile(selectedVariation)) {
            List<BuildingState> wallTiles = BuildingInfo.getWallTileLocations(
                    new BuildingState(
                            selectedVariation,
                            tileCoordinates.x,
                            tileCoordinates.y
                    )
            );
            for (BuildingState state : wallTiles) {
                addSingleTileBuilding(state);
            }
        } else {
            addSingleTileBuilding(
                    new BuildingState(
                            selectedVariation,
                            tileCoordinates.x,
                            tileCoordinates.y
                    )
            );
        }

    }

    /**
     * Adds a single-tile building to the render list.
     */
    private void addSingleTileBuilding(BuildingState state) {
        TiledMapTile tile = map.getTileSets().getTile(state.getId());
        if (tile == null) {
            return;
        }

        TextureRegion region = tile.getTextureRegion();
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Overlay");

        Vector2 drawPos = IsoMath.tileLayerCellToWorldBottomLeft(
                layer,
                state.getX(),
                state.getY()
        );

        renderables.add(new TileRenderPiece(
                region,
                drawPos.x,
                drawPos.y,
                region.getRegionWidth(),
                region.getRegionHeight(),
                state.getX(),
                state.getY(),
                1
        ));
    }

    /**
     * Returns whether the given building state is one tile of a wall structure.
     */
    private boolean isWallTile(int tileId) {
        return tileId >= BuildingInfo.getBuildingId(BuildingInfo.BuildingName.WALLS)
                && tileId < BuildingInfo.getBuildingId(BuildingInfo.BuildingName.WALLS) + 6;
    }


    /**
     * Returns the current building preview renderables for the frame.
     */
    public List<DepthRenderable> getRenderables() {
        return Collections.unmodifiableList(renderables);
    }


    public void clearPreview() {
        renderables.clear();
    }
}