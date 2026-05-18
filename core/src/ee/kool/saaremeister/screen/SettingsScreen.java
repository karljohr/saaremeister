package ee.kool.saaremeister.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ee.kool.saaremeister.screen.gamescreen.GameScreen;
import ee.kool.saaremeister.utility.*;

public class SettingsScreen extends ScreenAdapter {

    private final Game game;
    private final Screen previous;
    private final Stage stage;
    private final Texture background;
    private final SpriteBatch spriteBatch;
    private final SettingsMode mode;
    private boolean closing = false;

    public enum SettingsMode {
        FULL,
        AUDIO_ONLY
    }

    public SettingsScreen(Game game, Skin skin, Screen previous, SettingsMode mode) {
        this.game = game;
        this.previous = previous;
        this.spriteBatch = new SpriteBatch();
        this.mode = mode;

        background = new Texture("assets/background/map.png");
        Label.LabelStyle descStyle = new Label.LabelStyle(
                skin.getFont("font"),
                Color.LIGHT_GRAY
        );

        stage = new Stage(Ui.createViewport());
        Gdx.input.setInputProcessor(stage);

        Settings settings = SettingsManager.get();

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        // Music volume
        Label volumeLabel = new Label("Music Volume", descStyle);
        Slider volume = new Slider(0f, 1f, 0.01f, false, skin);
        volume.setValue(settings.musicVolume);

        volume.addListener(e -> {
            settings.musicVolume = volume.getValue();

            AudioManager.updateVolume();
            return false;
        });

        // SFX volume
        Label sfxLabel = new Label("SFX Volume", descStyle);
        Slider sfx = new Slider(0f, 1f, 0.01f, false, skin);
        sfx.setValue(settings.sfxVolume);

        sfx.addListener(e -> {
            settings.sfxVolume = sfx.getValue();
            return false;
        });

        // Resolution
        Label resolutionLabel = new Label("Resolution", descStyle);
        TextButton res1 = new TextButton("1280x720", skin);
        SoundEffectManager.bindMenuClick(res1);
        TextButton res2 = new TextButton("1920x1080", skin);
        SoundEffectManager.bindMenuClick(res2);
        TextButton res3 = new TextButton("2560x1440", skin);
        SoundEffectManager.bindMenuClick(res3);

        res1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.width = 1280;
                settings.height = 720;
            }
        });

        res2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.width = 1920;
                settings.height = 1080;
            }
        });

        res3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.width = 2560;
                settings.height = 1440;
            }
        });

        // UI scaling
        Label uiScaleLabel = new Label("UI Scale", skin);

        Slider uiScaleSlider = new Slider(0.5f, 2f, 0.1f, false, skin);
        uiScaleSlider.setValue(settings.uiScale);

        uiScaleSlider.addListener(e -> {
            settings.uiScale = uiScaleSlider.getValue();
            return false;
        });

        // Fullscreen
        Label displayLabel = new Label("Display Mode", descStyle);
        TextButton fullscreen = new TextButton(settings.fullscreen ? "Fullscreen" : "Windowed", skin);
        SoundEffectManager.bindMenuClick(fullscreen);
        Label escHintLabel = new Label("Esc - save and exit", descStyle);

        fullscreen.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.fullscreen = !settings.fullscreen;

                fullscreen.setText(settings.fullscreen ? "Fullscreen" : "Windowed");
            }
        });


        Image blocker = new Image(new Texture("white.png"));
        blocker.setColor(0, 0, 0, 0.6f);
        blocker.setFillParent(true);
        blocker.setTouchable(Touchable.disabled);

        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                applySettingsAndReturn(settings);
            }
        });
        SoundEffectManager.bindMenuClick(back);

        root.add(new Label("Settings", skin)).row();

        root.add(volumeLabel).padTop(20).row();
        root.add(volume).width(300).pad(10).row();
        root.add(sfxLabel).padTop(12).row();
        root.add(sfx).width(300).pad(10).row();

        if (mode == SettingsMode.FULL) {
        root.add(resolutionLabel).padTop(20).row();
        root.add(res1).pad(10).row();
        root.add(res2).pad(10).row();
        root.add(res3).pad(10).row();

        root.add(uiScaleLabel).padTop(20).row();
        root.add(uiScaleSlider).width(300).pad(10).row();

        root.add(displayLabel).padTop(20).row();
        root.add(fullscreen).pad(10).row();
        }

        root.add(back).pad(10);

        Table hintContainer = new Table();
        hintContainer.setFillParent(true);
        hintContainer.top().left();
        hintContainer.pad(24);
        hintContainer.add(escHintLabel).left();

        stage.addActor(blocker);
        stage.addActor(root);
        stage.addActor(hintContainer);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        if (!closing && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            applySettingsAndReturn(SettingsManager.get());
        }

        spriteBatch.begin();
        spriteBatch.enableBlending();
        spriteBatch.setColor(1, 1, 1, 0.5f);
        spriteBatch.draw(background, 0, 0, Gdx.graphics.getWidth() , Gdx.graphics.getHeight());
        spriteBatch.setColor(1, 1, 1, 1f);
        spriteBatch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Saves valid settings, applies the display mode, and returns to the previous screen.
     */
    private void applySettingsAndReturn(Settings settings) {
        if (closing) {
            return;
        }

        closing = true;
        Gdx.app.postRunnable(() -> {
            settings.save();

            if (settings.fullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(settings.width, settings.height);
            }

            if (previous instanceof LobbyScreen p) {
                game.setScreen(new LobbyScreen(
                        game,
                        p.getLobbyId(),
                        p.getCurrentPlayerCount(),
                        p.getCurrentPlayerNames(),
                        p.getUsername()
                ));
                previous.dispose();
            } else if (previous instanceof GameScreen g) {
                game.setScreen(g);
            } else {
                game.setScreen(new TitleScreen(game));
                previous.dispose();
            }
        });
    }
}
