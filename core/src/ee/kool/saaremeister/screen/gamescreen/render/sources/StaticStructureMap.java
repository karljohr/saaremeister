package ee.kool.saaremeister.screen.gamescreen.render.sources;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.DepthRenderable;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.StaticStructureRenderable;
import ee.kool.saaremeister.screen.gamescreen.render.utility.StaticStructure;
import ee.kool.saaremeister.screen.gamescreen.render.utility.StaticStructureExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import static ee.kool.saaremeister.screen.gamescreen.render.utility.StaticStructureExtractor.findBaseTile;

/**
 * Caches all extracted static structures used by the depth renderer.
 */
public class StaticStructureMap {

    private final List<DepthRenderable> renderables = new ArrayList<>();

    /**
     * Extracts static structures from the given layer and removes their tiles from the TMX layer.
     */
    public StaticStructureMap(TiledMap map, String layerName, IntPredicate staticTileIdFilter) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) {
            return;
        }

        StaticStructureExtractor extractor = new StaticStructureExtractor(staticTileIdFilter);
        List<StaticStructure> structures = extractor.extract(layer);

        for (StaticStructure structure : structures) {
            for (var tilePosition : structure.footprint()) {
                layer.setCell(tilePosition.x, tilePosition.y, null);
            }
            GridPoint2 base = findBaseTile(structure.footprint());

            Log.info("Static at: " + base);

            renderables.add(new StaticStructureRenderable(
                    structure.pieces(),
                    base.x,
                    base.y
            ));
        }
    }

    /**
     * Returns all cached static-structure renderables.
     */
    public List<DepthRenderable> getRenderables() {
        return renderables;
    }
}
