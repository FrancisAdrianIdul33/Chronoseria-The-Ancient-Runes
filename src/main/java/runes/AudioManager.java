package runes;

/** Stub for javax.sound.sampled. Replaces Player[]. */
public final class AudioManager {
    private AudioManager() {}

    public static void playEffect(String id) {
        System.out.println("[audio] effect: " + id);
    }

    public static void playMusic(String id) {
        System.out.println("[audio] music: " + id);
    }
}
