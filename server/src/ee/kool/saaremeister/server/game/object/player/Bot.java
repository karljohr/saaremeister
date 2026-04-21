package ee.kool.saaremeister.server.game.object.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ee.kool.saaremeister.server.game.BuildHandler;
import ee.kool.saaremeister.server.game.GameInstance;
import ee.kool.saaremeister.server.game.object.buildings.Building;
import message.dto.PlayerRole;
import java.util.List;
import static constant.Constants.PPM;

public class Bot extends Player {
    private static final float BOT_SPEED = 100f;

    public Bot(int id, GameInstance gameInstance, World world, BuildHandler buildHandler) {
        super(null, id, gameInstance, world, buildHandler, 500, 3, PlayerRole.OFFICER);
        this.setUsername("BOT_" + id);
    }

    public void updateAI(List<Player> players, List<Building> buildings) {
        if (!isAlive() || getBody() == null) return;

        Vector2 botPos = getBody().getPosition();
        Vector2 targetPos = null;
        float minDistance = Float.MAX_VALUE;

        // Find the closest master.
        for (Player p : players) {
            if (p.getRole() == PlayerRole.MASTER && p.isAlive()) {
                float dist = botPos.dst(p.getBody().getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    targetPos = p.getBody().getPosition();
                }
            }
        }

        // If no master, go towards buildings.
        if (targetPos == null) {
            for (Building b : buildings) {
                float dist = botPos.dst(b.getBody().getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    targetPos = b.getBody().getPosition();
                }
            }
        }

        // Move towards target
        if (targetPos != null) {
            Vector2 direction = new Vector2(targetPos).sub(botPos).nor();
            getBody().setLinearVelocity(direction.scl(BOT_SPEED / PPM));
        } else {
            getBody().setLinearVelocity(0, 0);
        }
    }

    @Override
    public void dispose() {
        // Bot doesn't have listeners to remove.
    }
}