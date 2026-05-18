package ee.kool.saaremeister.utility;

import com.badlogic.gdx.audio.Music;

public class AudioManager {
    private static Music music;

    public static void setMusic(Music m) {
        music = m;
        updateVolume();
    }

    public static void updateVolume() {
        if (music != null) {
            music.setVolume(SettingsManager.get().musicVolume);
        }
    }

    public static void play() {
        if (music != null) {
            music.play();
        }
    }

    public static void stop() {
        if (music != null) {
            music.stop();
        }
    }

    public static void dispose() {
        if (music != null) {
            music.dispose();
        }
    }
}