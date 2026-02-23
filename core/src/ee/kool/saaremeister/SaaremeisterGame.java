package ee.kool.saaremeister;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SaaremeisterGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture img;
    private ShapeRenderer shapes;

    private final Vector2 playerPos = new Vector2(40, 40);
    private final float playerSize = 20f;
    private final float playerSpeed = 200f;

    private final Rectangle goal = new Rectangle(500, 300, 100, 50);
    private boolean won = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        shapes = new ShapeRenderer();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) dy += 1f;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) dy -= 1f;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) dx -= 1f;
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) dx += 1f;

        if (!won) {
            playerPos.x += dx * playerSpeed * dt;
            playerPos.y += dy * playerSpeed * dt;

            playerPos.x = MathUtils.clamp(playerPos.x, 0f, 600f - playerSize);
            playerPos.y = MathUtils.clamp(playerPos.y, 0f, 400f - playerSize);

            Rectangle playerRect = new Rectangle(playerPos.x, playerPos.y, playerSize, playerSize);
            if (goal.overlaps(playerRect)) {
                won = true;
                System.out.println("You won!");
            }
        }

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0, 0, 0, 0);
        shapes.rect(goal.x, goal.y, goal.width, goal.height);

        shapes.setColor(0, 1, 0, 1);
        shapes.rect(playerPos.x, playerPos.y, playerSize, playerSize);

        shapes.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
