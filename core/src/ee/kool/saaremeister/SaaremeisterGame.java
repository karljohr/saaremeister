package ee.kool.saaremeister;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryonet.Client;
import messages.DTO.Input;

public class SaaremeisterGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Client client;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		Input input = new Input();

		input.up = Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W);
		input.down = Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.S);
		input.left = Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.A);
		input.right = Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D);



		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
