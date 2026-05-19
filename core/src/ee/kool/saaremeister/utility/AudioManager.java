package ee.kool.saaremeister.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class AudioManager {
    private static Music music;
    private static String currentTrackPath;

    public static void playMusic(String trackPath, boolean looping) {
        if (trackPath == null || trackPath.isBlank()) {
            return;
        }

        // If same track is already loaded, keep playing without restarting.
        if (music != null && trackPath.equals(currentTrackPath)) {
            music.setLooping(looping);
            updateVolume();
            if (!music.isPlaying()) {
                music.play();
            }
            return;
        }

        // Switch to a different track.
        dispose();

        music = Gdx.audio.newMusic(Gdx.files.internal(trackPath));
        currentTrackPath = trackPath;
        music.setLooping(looping);
        updateVolume();
        music.play();
    }

    public static void updateVolume() {
        if (music != null) {
            music.setVolume(SettingsManager.get().musicVolume);
        }
    }

    public static void stop() {
        if (music != null) {
            music.stop();
        }
    }

    public static void dispose() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
            currentTrackPath = null;
        }
    }
}