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
        // same logic as renderProjection
        // but create TileRenderPiece instead of layer cells
        int selectedVariation = buildManager.getSelectedVariation();

//        GridPoint2 tileCoordinates = IsoMath.worldToTile(mouseX, mouseY);
        GridPoint2 tileCoordinates = mouseToIsoCoordinates(mouseX, mouseY);

        if (isWallTile(selectedVariation)) {
            List<BuildingState> wallTiles = BuildingInfo.getWallTileLocations(
                    new BuildingState(
                            selectedVariation,
                            tileCoordinates.x,
                            tileCoordinates.y
                    )
            );
            addWallGroups(wallTiles);
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
        //Vector2 drawPos = IsoMath.tileToWorldBottomLeft(state.getX(), state.getY());
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

    /**
     * Returns whether the given building state is one tile of a wall structure.
     */
    private boolean isWallTile(int tileId) {
        return tileId >= 156 && tileId < 163;
    }


    /**
     * Returns the current building preview renderables for the frame.
     */
    public List<DepthRenderable> getRenderables() {
        return Collections.unmodifiableList(renderables);
    }

    /**
     * Converts mouse coordinates to tile coordinates
     */
    private GridPoint2 mouseToIsoCoordinates(float mouseX, float mouseY) {
        float x = mouseX - Constants.RENDER_OFFSETX - 16;
        float y = -(mouseY - 16);

        float halfTileWidth = Constants.TILE_WIDTH / 2f;
        float halfTileHeight = Constants.TILE_HEIGHT / 2f;

        int tileX = (int) Math.floor((x / halfTileWidth + y / halfTileHeight) / 2f);
        int tileY = (int) -Math.floor((y / halfTileHeight - x / halfTileWidth) / 2f);

        return new GridPoint2(tileX, tileY);
    }

    public void clearPreview() {
        renderables.clear();
    }
}