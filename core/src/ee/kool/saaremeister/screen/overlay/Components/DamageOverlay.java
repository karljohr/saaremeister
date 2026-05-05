package ee.kool.saaremeister.screen.overlay.Components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class DamageOverlay implements UIComponent<Float> {

    private final Image damageOverlay;
    private float flashTime;
    private int previousHealth;
    private float maxAlpha;
    private boolean deathTriggered;

    private static final float FLASH_DURATION = 0.3f;
    private static final float DEATH_FLASH_DURATION = 0.5f;

    public  DamageOverlay() {
        flashTime = 0;
        maxAlpha = 0.3f;

        damageOverlay = new Image(
                new TextureRegionDrawable(new TextureRegion(new Texture("white.png")))
        );

        damageOverlay.setColor(1, 0, 0, 0);
        damageOverlay.setFillParent(true);
    }

    public Image getActor() {
        return damageOverlay;
    }

    public void checkDamage(int health) {
        // Death hit
        if (health <= 0 && previousHealth > 0 && !deathTriggered) {
            triggerDeathFlash();
            deathTriggered = true;
        }

        // Normal hit
        if (previousHealth > health && health > 0) {
            triggerDamageFlash();
        }

        if (health > 0) {
            deathTriggered = false;
        }

        previousHealth = health;
    }

    public void update(Float delta) {
        if (flashTime > 0) {
            flashTime -= delta;

            float duration = maxAlpha > 0.5f ? DEATH_FLASH_DURATION : FLASH_DURATION;
            float alpha = Math.max(0, flashTime / duration);
            damageOverlay.setColor(1, 0, 0, alpha * maxAlpha);
        } else {
            damageOverlay.setColor(1, 0, 0, 0);
        }
    }

    public void triggerDeathFlash() {
        flashTime = DEATH_FLASH_DURATION;
        maxAlpha = 0.8f;
    }

    public void triggerDamageFlash() {
        flashTime = FLASH_DURATION;
        maxAlpha = 0.3f;
    }
}
