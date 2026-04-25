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
        }

        addWallGroups(wallTiles);
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
        TiledMapTile tile = map.getTileSets().getTile(state.getId());
        if (tile == null) {
            return;
        }

        Log.info("Tile at: " + state.getX() + ", " + state.getY());

        TextureRegion region = tile.getTextureRegion();
        // Vector2 drawPos = IsoMath.tileToWorldBottomLeft(state.getX(), state.getY());

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Overlay");

        Vector2 drawPos = IsoMath.tileLayerCellToWorldBottomLeft(
                layer,
                state.getX(),
                state.getY()
        );

        Log.info("Drawpos at: " + String.valueOf(drawPos));

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
     * Groups wall tiles into connected structures so all tiles in a wall share one base sort key.
     */
    private void addWallGroups(List<BuildingState> wallTiles) {
        Map<GridPoint2, BuildingState> byPosition = new HashMap<>();
        for (BuildingState wallTile : wallTiles) {
            byPosition.put(new GridPoint2(wallTile.getX(), wallTile.getY()), wallTile);
        }

        Set<GridPoint2> visited = new HashSet<>();
        for (GridPoint2 start : byPosition.keySet()) {
            if (visited.contains(start)) {
                continue;
            }

            List<BuildingState> wallGroup = collectWallGroup(start, byPosition, visited);
            GridPoint2 base = findBaseTile(wallGroup);
            addWallGroupRenderables(wallGroup, base);
        }
    }

    /**
     * Collects one connected wall group using four-direction adjacency.
     */
    private List<BuildingState> collectWallGroup(
            GridPoint2 start,
            Map<GridPoint2, BuildingState> byPosition,
            Set<GridPoint2> visited
    ) {
        List<BuildingState> wallGroup = new ArrayList<>();
        ArrayDeque<GridPoint2> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            GridPoint2 current = queue.removeFirst();
            if (visited.contains(current)) {
                continue;
            }

            BuildingState wallTile = byPosition.get(current);
            if (wallTile == null) {
                continue;
            }

            visited.add(current);
            wallGroup.add(wallTile);

            queue.add(new GridPoint2(current.x + 1, current.y));
            queue.add(new GridPoint2(current.x - 1, current.y));
            queue.add(new GridPoint2(current.x, current.y + 1));
            queue.add(new GridPoint2(current.x, current.y - 1));
        }

        return wallGroup;
    }

    /**
     * Adds renderables for one already-grouped wall using a shared sort base.
     */
    private void addWallGroupRenderables(List<BuildingState> wallGroup, GridPoint2 base) {
        for (BuildingState wallTile : wallGroup) {
            TiledMapTile tile = map.getTileSets().getTile(wallTile.getId());
            if (tile == null) {
                continue;
            }

            TextureRegion region = tile.getTextureRegion();
            //Vector2 drawPos = IsoMath.tileToWorldBottomLeft(wallTile.getX(), wallTile.getY());

            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Overlay");

            Vector2 drawPos = IsoMath.tileLayerCellToWorldBottomLeft(
                    layer,
                    wallTile.getX(),
                    wallTile.getY()
            );

            renderables.add(new TileRenderPiece(
                    region,
                    drawPos.x,
                    drawPos.y,
                    region.getRegionWidth(),
                    region.getRegionHeight(),
                    base.x,
                    base.y,
                    1
            ));
        }
    }

    /**
     * Returns whether the given building state is one tile of a wall structure.
     */
    private boolean isWallTile(BuildingState state) {
        return state.getId() >= 156 && state.getId() < 163;
    }

    /**
     * Finds the footprint tile that is lowest on screen and should anchor depth sorting.
     */
    private GridPoint2 findBaseTile(List<BuildingState> tiles) {
        BuildingState best = tiles.getFirst();
        for (BuildingState tile : tiles) {
            int bestDiagonal = best.getX() + best.getY();
            int tileDiagonal = tile.getX() + tile.getY();
            if (tileDiagonal > bestDiagonal
                    || (tileDiagonal == bestDiagonal && tile.getX() > best.getX())) {
                best = tile;
            }
        }
        return new GridPoint2(best.getX(), best.getY());
    }
}
