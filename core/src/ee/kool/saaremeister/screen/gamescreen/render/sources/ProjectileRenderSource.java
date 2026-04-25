package ee.kool.saaremeister.screen.gamescreen.render.sources;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import ee.kool.saaremeister.screen.gamescreen.render.renderables.DepthRenderable;
import ee.kool.saaremeister.screen.gamescreen.render.utility.RenderSortKey;
import ee.kool.saaremeister.utility.IsoMath;
import ee.kool.saaremeister.utility.Sprites;
import message.dto.BulletState;
import message.dto.RocketState;

import java.util.ArrayList;
import java.util.List;

import static constant.Constants.BULLET_HEIGHT_IN_PIXELS;
import static constant.Constants.BULLET_WIDTH_IN_PIXELS;
import static constant.Constants.ROCKET_HEIGHT_IN_PIXELS;
import static constant.Constants.ROCKET_WIDTH_IN_PIXELS;

/**
 * Converts projectile state into depth-sortable renderables.
 */
public class ProjectileRenderSource {

    /**
     * Builds renderables for bullets and rockets using their current world positions.
     */
    public List<DepthRenderable> build(List<BulletState> bullets, List<RocketState> rockets) {
        List<DepthRenderable> renderables = new ArrayList<>();

        if (bullets != null) {
            for (BulletState bullet : bullets) {
                GridPoint2 base = IsoMath.worldToTile(bullet.getX(), bullet.getY());
                renderables.add(new DepthRenderable() {
                    private final RenderSortKey sortKey = new RenderSortKey(base.x, base.y, 3);

                    @Override
                    public RenderSortKey getSortKey() {
                        return sortKey;
                    }

                    @Override
                    public void render(SpriteBatch batch) {
                        batch.draw(
                                Sprites.bulletTexture,
                                bullet.getX(),
                                bullet.getY(),
                                BULLET_WIDTH_IN_PIXELS,
                                BULLET_HEIGHT_IN_PIXELS
                        );
                    }
                });
            }
        }

        if (rockets != null) {
            for (RocketState rocket : rockets) {
                GridPoint2 base = IsoMath.worldToTile(rocket.getX(), rocket.getY());
                renderables.add(new DepthRenderable() {
                    private final RenderSortKey sortKey = new RenderSortKey(base.x, base.y, 3);

                    @Override
                    public RenderSortKey getSortKey() {
                        return sortKey;
                    }

                    @Override
                    public void render(SpriteBatch batch) {
                        batch.draw(
                                rocket.isFalling() ? Sprites.rocketDownTexture : Sprites.rocketTexture,
                                rocket.getX(),
                                rocket.getY() + rocket.getHeight(),
                                ROCKET_WIDTH_IN_PIXELS,
                                ROCKET_HEIGHT_IN_PIXELS
                        );
                    }
                });
            }
        }

        return renderables;
    }
}
