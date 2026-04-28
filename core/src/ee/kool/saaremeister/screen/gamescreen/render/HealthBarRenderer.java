package ee.kool.saaremeister.screen.gamescreen.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import common.HasHealth;
import ee.kool.saaremeister.game.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthBarRenderer {

    private final Map<Integer, Long> visibleUntil = new HashMap<>();
    private final Map<Integer, Integer> previousHealth = new HashMap<>();

    private static final int BAR_WIDTH = 20;
    private static final int BAR_HEIGHT = 3;
    private static final int Y_OFFSET_PLAYER = 20;
    private static final int Y_OFFSET_BUILDING = 40;
    private static final int DURATION_MS = 3000;

    public void update(List<? extends HasHealth> entities) {
        for (HasHealth entity : entities) {
            if (entity.getMaxHealth() == 0) continue;

            int id = entity.getId();

            if (!visibleUntil.containsKey(id)) visibleUntil.put(id, 0L);
            if (!previousHealth.containsKey(id)) previousHealth.put(id, entity.getHealth());

            if (previousHealth.containsKey(id) && entity.getHealth() < previousHealth.get(id)) {
                notifyDamage(entity);
            }

            previousHealth.put(id, entity.getHealth());
        }
    }

    public void notifyDamage(HasHealth entity) {
        visibleUntil.put(
                entity.getId(),
                System.currentTimeMillis() + DURATION_MS
        );

    }

    public void render(ShapeRenderer shapeRenderer, List<? extends HasHealth> entities) {
        for (HasHealth entity : entities) {
            Long until = visibleUntil.get(entity.getId());
            if (until == null || System.currentTimeMillis() > until) continue;

            float ratio = (float) entity.getHealth() / entity.getMaxHealth();

            float x = entity.getWorldX() - BAR_WIDTH / 2f;
            float y = entity.getWorldY()
                    + (entity instanceof Player ? Y_OFFSET_PLAYER : Y_OFFSET_BUILDING);

            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

            shapeRenderer.setColor(
                    ratio > 0.5f ? Color.GREEN :
                    ratio > 0.25f ? Color.ORANGE :
                                    Color.RED
            );

            shapeRenderer.rect(x, y, BAR_WIDTH * ratio, BAR_HEIGHT);
        }
    }
}
