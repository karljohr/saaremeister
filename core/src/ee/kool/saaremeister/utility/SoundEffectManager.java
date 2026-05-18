package ee.kool.saaremeister.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.minlog.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to manage sound effects in the game.
 */
public final class SoundEffectManager {
    private static boolean initialized = false;
    private static Sound[] bulletShots;
    private static Sound[] rocketShots;
    private static Sound rocketExplosion;
    private static Sound[] deaths;
    private static Sound spawn;
    private static Sound drink;
    private static Sound armorBuy;
    private static Sound[] builds;
    private static Sound capture;
    private static Sound lose;
    private static Sound menuClick;

    private SoundEffectManager() {
    }

    /**
     * Loads all sound effects.
     */
    public static synchronized void init() {
        if (initialized) return;

        bulletShots = loadMany(
                "sound_effects/shoot_1.mp3",
                "sound_effects/shoot_2.mp3",
                "sound_effects/shoot_3.mp3",
                "sound_effects/shoot_4.mp3",
                "sound_effects/shoot_5.mp3"
        );
        rocketShots = loadMany(
                "sound_effects/rocket_1.mp3",
                "sound_effects/rocket_2.mp3"
        );
        rocketExplosion = load("sound_effects/explosion.mp3");
        deaths = loadMany(
                "sound_effects/death_1.mp3",
                "sound_effects/death_2.mp3",
                "sound_effects/death_3.mp3",
                "sound_effects/death_4.mp3",
                "sound_effects/death_5.mp3",
                "sound_effects/death_6.mp3"
        );
        spawn = load("sound_effects/spawn.mp3");
        drink = load("sound_effects/drink.mp3");
        armorBuy = load("sound_effects/armor.mp3");
        builds = loadMany(
                "sound_effects/build_1.mp3",
                "sound_effects/build_2.mp3",
                "sound_effects/build_3.mp3"
        );
        capture = load("sound_effects/capture.mp3");
        lose = load("sound_effects/lose.mp3");
        menuClick = load("sound_effects/click.mp3");

        initialized = true;
    }

    /**
     * Binds a button to play the menu click sound effect when clicked.
     */
    public static void bindMenuClick(Button button) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playMenuClick();
            }
        });
    }

    // Plays the given sound effect or a random sound effect from the given array.
    public static void playBulletShot() { playRandom(bulletShots, 0.5f); }
    public static void playRocketShot() { playRandom(rocketShots, 0.8f); }
    public static void playRocketExplosion() { play(rocketExplosion, 1.0f); }
    public static void playDeath() { playRandom(deaths, 1.0f); }
    public static void playSpawn() { play(spawn, 1.0f); }
    public static void playDrink() { play(drink, 1.0f); }
    public static void playArmorBought() { play(armorBuy, 1.0f); }
    public static void playBuild() { playRandom(builds, 1.0f); }
    public static void playCapture() { play(capture, 1.0f); }
    public static void playLose() { play(lose, 1.0f); }
    public static void playMenuClick() { play(menuClick, 1.0f); }

    /**
     * Disposes all sound effects.
     */
    public static synchronized void dispose() {
        if (!initialized) return;
        disposeMany(bulletShots);
        disposeMany(rocketShots);
        disposeMany(deaths);
        disposeMany(builds);
        disposeOne(rocketExplosion);
        disposeOne(spawn);
        disposeOne(drink);
        disposeOne(armorBuy);
        disposeOne(capture);
        disposeOne(lose);
        disposeOne(menuClick);
        initialized = false;
    }

    /**
     * Loads multiple sound effects.
     */
    private static Sound[] loadMany(String... paths) {
        List<Sound> loaded = new ArrayList<>();
        for (String p : paths) {
            Sound s = load(p);
            if (s != null) loaded.add(s);
        }
        return loaded.toArray(new Sound[0]);
    }

    /**
     * Loads a single sound effect.
     */
    private static Sound load(String relativePath) {
        try {
            return Gdx.audio.newSound(Gdx.files.internal(relativePath));
        } catch (Exception first) {
            try {
                return Gdx.audio.newSound(Gdx.files.internal("assets/" + relativePath));
            } catch (Exception second) {
                Log.warn("SoundEffectManager: failed to load " + relativePath);
                return null;
            }
        }
    }

    /**
     * Plays a single sound effect.
     */
    private static void play(Sound sound, float baseVolume) {
        if (!initialized) init();
        if (sound == null) return;
        sound.play(getVolume(baseVolume));
    }

    /**
     * Plays a random sound effect from the given array.
     */
    private static void playRandom(Sound[] variants, float baseVolume) {
        if (!initialized) init();
        if (variants == null || variants.length == 0) return;
        variants[MathUtils.random(variants.length - 1)].play(getVolume(baseVolume));
    }

    /**
     * Returns the volume for the given base volume.
     */
    private static float getVolume(float baseVolume) {
        return MathUtils.clamp(baseVolume * SettingsManager.get().sfxVolume, 0f, 1f);
    }

    /**
     * Disposes a single sound effect.
     */
    private static void disposeOne(Sound sound) {
        if (sound != null) sound.dispose();
    }

    /**
     * Disposes multiple sound effects.
     */
    private static void disposeMany(Sound[] sounds) {
        if (sounds == null) return;
        for (Sound s : sounds) {
            if (s != null) s.dispose();
        }
    }
}
