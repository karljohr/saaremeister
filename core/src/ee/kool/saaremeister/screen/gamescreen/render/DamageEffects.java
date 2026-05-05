package ee.kool.saaremeister.screen.gamescreen.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import message.GameStateMessage;
import message.dto.PlayerState;

public class DamageEffects {

    private float shakeTime;
    private int previousHealth;

    private static float SHAKE_DURATION = 0.3f;
    private static float SHAKE_INTENSITY = 5f;

    public DamageEffects() {
        shakeTime = 0;
        previousHealth = 10; // TODO: change after merging with branch "133-elude-riba-loomine".
    }

    public void checkDamage(GameStateMessage message, int myId) {
        PlayerState p = message
                .getPlayerStates()
                .stream()
                .filter(
                        player -> player.getId() == myId
                )
                .findFirst()
                .orElse(null);

        if (p == null) {
            previousHealth = 10;
            return;
        }

        if (p.getLives() < previousHealth && previousHealth > 0) {
            shakeTime = SHAKE_DURATION;
            previousHealth = p.getLives();
        }
    }

    public void affectCamera(float delta, OrthographicCamera camera) {
        if (shakeTime > 0) {

            shakeTime -= delta;

            float currentIntensity = SHAKE_INTENSITY * (shakeTime / SHAKE_DURATION);

            float offsetX = MathUtils.random(-1f, 1f) * currentIntensity;
            float offsetY = MathUtils.random(-1f, 1f) * currentIntensity;

            camera.position.add(offsetX, offsetY, 0);
            camera.update();
        }
    }
}
