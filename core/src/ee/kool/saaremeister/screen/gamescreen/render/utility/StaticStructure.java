package ee.kool.saaremeister.screen.gamescreen.render.utility;

import com.badlogic.gdx.math.GridPoint2;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.TileRenderPiece;

import java.util.List;

/**
 * Stores one extracted static structure footprint and its prebuilt render pieces.
 */
public record StaticStructure(List<GridPoint2> footprint, List<TileRenderPiece> pieces) {
}
