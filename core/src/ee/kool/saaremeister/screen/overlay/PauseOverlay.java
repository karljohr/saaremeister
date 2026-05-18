package ee.kool.saaremeister.screen.overlay;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ee.kool.saaremeister.screen.SettingsScreen;
import ee.kool.saaremeister.utility.SoundEffectManager;
import ee.kool.saaremeister.utility.Ui;
import lombok.Getter;

/**
 * Pause overlay for a game instance so that pressing ESC wouldn't immediately disconnect the player.
 */
public class PauseOverlay implements Disposable {

    @Getter
    private final Stage stage;
    private final Table root;

    private final Texture dimTexture;

    private static final Color WINDOW_BACKGROUND = new Color(192 / 255f, 192 / 255f, 192 / 255f, 1f);
    private static final Color DIM = new Color(0f, 0f, 0f, 0.65f);

    /**
     * Creates a new pause overlay.
     */
    public PauseOverlay(SpriteBatch spriteBatch,
                        Game game,
                        Screen currentGameScreen,
                        Runnable onResume,
                        Runnable onLeave,
                        Skin skin) {
        stage = new Stage(Ui.createViewport(), spriteBatch);

        dimTexture = createDimTexture();

        root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(new TextureRegion(dimTexture)));

        Window panel = new Window("", skin);
        panel.setColor(WINDOW_BACKGROUND);
        panel.pad(24);

        root.add(panel).width(400).pad(20);
        stage.addActor(root);
        setOpen(false);


        Label title = new Label("Pausil", skin, "title");
        title.setAlignment(Align.center);

        TextButton resumeButton = new TextButton("J2tka", skin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setOpen(false);
                onResume.run();
            }
        });
        SoundEffectManager.bindMenuClick(resumeButton);

        TextButton leaveButton = new TextButton("Lahku", skin);
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onLeave.run();
            }
        });
        SoundEffectManager.bindMenuClick(leaveButton);

        // Settings button
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(
                        game,
                        skin,
                        currentGameScreen,
                        SettingsScreen.SettingsMode.AUDIO_ONLY
                )
                );
            }
        });
        SoundEffectManager.bindMenuClick(settingsButton);

        panel.defaults().width(220).pad(8);
        panel.add(title).center().padBottom(16).row();
        panel.add(resumeButton).height(56).row();
        panel.add(settingsButton).height(56).row();
        panel.add(leaveButton).height(56).row();
    }

    public void setOpen(boolean open) {
        root.setVisible(open);
        root.setTouchable(open ? Touchable.childrenOnly : Touchable.disabled);
    }

    public void render(float delta) {
        if (!root.isVisible()) return;
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Creates a texture for the dimmed background.
     * @return background texture.
     */
    private static Texture createDimTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(DIM);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    @Override
    public void dispose() {
        stage.dispose();
        dimTexture.dispose();
    }
}
