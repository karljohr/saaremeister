package ee.kool.saaremeister.screen.gamescreen.render.utility;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;

import ee.kool.saaremeister.screen.gamescreen.render.renderables.TileRenderPiece;
import ee.kool.saaremeister.utility.IsoMath;

/**
 * Extracts connected static structures from a tile layer for depth-sorted rendering.
 */
public class StaticStructureExtractor {

    private final IntPredicate staticTileIdFilter;

    public StaticStructureExtractor(IntPredicate staticTileIdFilter) {
        this.staticTileIdFilter = staticTileIdFilter;
    }

    /**
     * Finds connected static structures in the layer and converts them to render pieces.
     */
    public List<StaticStructure> extract(TiledMapTileLayer layer) {
        List<StaticStructure> result = new ArrayList<>();
        Set<GridPoint2> visited = new HashSet<>();

        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                GridPoint2 start = new GridPoint2(x, y);
                if (visited.contains(start) || !isStaticTile(layer, x, y)) {
                    continue;
                }

                List<GridPoint2> footprint = floodFill(layer, start, visited);
                if (footprint.isEmpty()) {
                    continue;
                }

                GridPoint2 base = findBaseTile(footprint);
                List<TileRenderPiece> pieces = new ArrayList<>(footprint.size());

                for (GridPoint2 tilePos : footprint) {
                    TiledMapTile tile = layer.getCell(tilePos.x, tilePos.y).getTile();
                    TextureRegion region = tile.getTextureRegion();
                    Vector2 drawPos = IsoMath.tileLayerCellToWorldBottomLeft(layer, tilePos.x, tilePos.y);

                    pieces.add(new TileRenderPiece(
                            region,
                            drawPos.x,
                            drawPos.y,
                            region.getRegionWidth(),
                            region.getRegionHeight(),
                            base.x,
                            base.y,
                            0
                    ));
                }

                result.add(new StaticStructure(footprint, pieces));
            }
        }

        return result;
    }

    /**
     * Flood-fills one connected structure using four-direction adjacency.
     */
    private List<GridPoint2> floodFill(TiledMapTileLayer layer, GridPoint2 start, Set<GridPoint2> visited) {
        List<GridPoint2> tiles = new ArrayList<>();
        ArrayDeque<GridPoint2> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            GridPoint2 current = queue.removeFirst();
            if (visited.contains(current) || !isStaticTile(layer, current.x, current.y)) {
                continue;
            }

            visited.add(current);
            tiles.add(current);

            queue.add(new GridPoint2(current.x + 1, current.y));
            queue.add(new GridPoint2(current.x - 1, current.y));
            queue.add(new GridPoint2(current.x, current.y + 1));
            queue.add(new GridPoint2(current.x, current.y - 1));
        }

        return tiles;
    }

    /**
     * Returns whether the cell contains a static-building tile that should be extracted.
     */
    private boolean isStaticTile(TiledMapTileLayer layer, int x, int y) {
        if (x < 0 || y < 0 || x >= layer.getWidth() || y >= layer.getHeight()) {
            return false;
        }

        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
        if (cell == null || cell.getTile() == null) {
            return false;
        }

        return staticTileIdFilter.test(cell.getTile().getId());
    }

    /**
     * Finds the footprint tile that is lowest on screen and should anchor depth sorting.
     */
    public static GridPoint2 findBaseTile(List<GridPoint2> footprint) {
        GridPoint2 best = footprint.getFirst();
        for (GridPoint2 tile : footprint) {
            int bestDiagonal = best.x + best.y;
            int tileDiagonal = tile.x + tile.y;
            if (tileDiagonal > bestDiagonal
                    || (tileDiagonal == bestDiagonal && tile.x > best.x)) {
                best = tile;
            }
        }
        return best;
    }
}
