package runes;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Village music + effects. Plays WAVs with stock javax.sound.sampled — no
 * libraries needed (MP3s can't play on plain Java, so the theme ships as a
 * WAV converted once from the MP3 upload; see assets/music/). The Clip loops
 * gaplessly with LOOP_CONTINUOUSLY. Loading runs on a daemon thread so game
 * start never hitches; with no audio device the game runs silent instead of
 * crashing.
 */
public final class AudioManager {
    private AudioManager() {}

    private static final Object LOCK = new Object();
    private static String currentId;
    private static Clip currentClip;

    /** One-shot effect hook (no SFX assets yet — logs only). */
    public static void playEffect(String id) {
        System.out.println("[audio] effect: " + id);
    }

    /**
     * Start looped music for an id ("village" -> bamboo.wav).
     * Re-calling with the same id while it plays is a no-op.
     */
    public static void playMusic(String id) {
        synchronized (LOCK) {
            if (id.equals(currentId) && currentClip != null && currentClip.isRunning()) return;
            stopLocked();
            currentId = id;
            Thread loader = new Thread(() -> startLoop(id), "music-loader");
            loader.setDaemon(true);
            loader.start();
        }
    }

    /** True while the looped clip is audible (useful for tests). */
    public static boolean isPlaying() {
        synchronized (LOCK) {
            return currentClip != null && currentClip.isRunning();
        }
    }

    /** Stop music (title screen, scene change). */
    public static void stopMusic() {
        synchronized (LOCK) {
            stopLocked();
            currentId = null;
        }
    }

    private static void stopLocked() {
        if (currentClip != null) {
            try {
                currentClip.stop();
            } catch (Exception ignored) {
            }
            try {
                currentClip.close();
            } catch (Exception ignored) {
            }
            currentClip = null;
        }
    }

    private static String resourceFor(String id) {
        if ("village".equals(id)) return "/assets/music/Village Theme.wav";
        throw new IllegalArgumentException("Unknown music id: " + id);
    }

    private static void startLoop(String id) {
        try (InputStream raw = AssetLoader.open(resourceFor(id));
                AudioInputStream in = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(raw))) {
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            synchronized (LOCK) {
                if (!id.equals(currentId)) {
                    clip.close();
                    return; // superseded while loading
                }
                currentClip = clip;
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("[audio] music loop: " + id);
        } catch (Exception e) {
            System.out.println("[audio] music unavailable: " + id + " (" + e + ")");
        }
    }
}
