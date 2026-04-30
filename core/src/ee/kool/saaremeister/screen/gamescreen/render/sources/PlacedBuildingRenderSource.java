package ee.kool.saaremeister.screen.gamescreen.render.sources;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.DepthRenderable;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.TileRenderPiece;
import message.dto.BuildingState;
import ee.kool.saaremeister.utility.IsoMath;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts server building state into depth-sortable renderables for placed buildings.
 */
public class PlacedBuildingRenderSource {

    private final TiledMap map;
    private final List<DepthRenderable> renderables = new ArrayList<>();

    public PlacedBuildingRenderSource(TiledMap map) {
        this.map = map;
    }

    /**
     * Rebuilds the placed-building renderables from the latest server state.
     */
    public void update(List<BuildingState> buildingStates) {
        renderables.clear();
        if (buildingStates == null) {
            return;
        }

        List<BuildingState> wallTiles = new ArrayList<>();
        for (BuildingState state : buildingStates) {
            if (isWallTile(state)) {
                wallTiles.add(state);
            } else {
                addSingleTileBuilding(state);
            }
            System.out.println("Added building at " + state.getX() + "," + state.getY());
        }

        for (BuildingState state : wallTiles) {
            addSingleTileBuilding(state);
        }

    }

    /**
     * Returns the current placed-building renderables for the frame.
     */
    public List<DepthRenderable> getRenderables() {
        return Collections.unmodifiableList(renderables);
    }

    /**
     * Adds a single-tile building to the render list.
     */
    private void addSingleTileBuilding(BuildingState state) {
        TiledMapTile tile = map.getTileSets().getTile(state.getTileId());
        if (tile == null) {
            return;
        }


        TextureRegion region = tile.getTextureRegion();

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Overlay");

        Vector2 drawPos = IsoMath.tileLayerCellToWorldBottomLeft(
                layer,
                (int) state.getX(),
                (int) state.getY()
        );


        renderables.add(new TileRenderPiece(
                region,
                drawPos.x,
                drawPos.y,
                (float) region.getRegionWidth(),
                (float) region.getRegionHeight(),
                (int) state.getX(),
                (int) state.getY(),
                1
        ));
    }

    /**
     * Returns whether the given building state is one tile of a wall structure.
     */
    private boolean isWallTile(BuildingState state) {
        return state.getTileId() >= 284 && state.getTileId() < 290;
    }
}
