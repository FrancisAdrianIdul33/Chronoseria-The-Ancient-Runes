package runes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Village music + effects. MP3s decode via JLayer (JDK sampled has no MP3
 * SPI — verified UnsupportedAudioFileException) into PCM, then a Clip loops
 * gaplessly with LOOP_CONTINUOUSLY. JLayer loads by reflection: plain
 * `javac` with no extra classpath compiles fine, and the game runs silent
 * when the jar is absent — music just needs lib/jlayer-1.0.1.jar at runtime
 * (see README). Decoding runs on a daemon thread so game start never hitches;
 * with no audio device the game runs silent instead of crashing.
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
     * Start looped music for an id ("village" -> bamboo.mp3).
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
        if ("village".equals(id)) return "/assets/music/bamboo.mp3";
        throw new IllegalArgumentException("Unknown music id: " + id);
    }

    private static void startLoop(String id) {
        try {
            byte[] pcm;
            AudioFormat fmt;
            try (InputStream in = AssetLoader.open(resourceFor(id))) {
                Decoded decoded = decodeMp3(in);
                pcm = decoded.bytes;
                fmt = decoded.format;
            }
            Clip clip = AudioSystem.getClip();
            clip.open(fmt, pcm, 0, pcm.length);
            synchronized (LOCK) {
                if (!id.equals(currentId)) {
                    clip.close();
                    return; // superseded while decoding
                }
                currentClip = clip;
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("[audio] music loop: " + id);
        } catch (Exception e) {
            System.out.println("[audio] music unavailable: " + id + " (" + e + ")");
        }
    }

    static final class Decoded {
        final byte[] bytes;
        final AudioFormat format;
        Decoded(byte[] bytes, AudioFormat format) {
            this.bytes = bytes;
            this.format = format;
        }
    }

    /**
     * Whole-file MP3 -> 16-bit little-endian PCM (fits a 300KB theme in RAM).
     * JLayer loads by reflection so the game still compiles and runs silent
     * with no lib on the classpath; music just needs the jar at runtime.
     */
    static Decoded decodeMp3(InputStream in) throws Exception {
        final Class<?> bitstreamCls;
        final Class<?> decoderCls;
        final Class<?> headerCls;
        try {
            bitstreamCls = Class.forName("javazoom.jl.decoder.Bitstream");
            decoderCls = Class.forName("javazoom.jl.decoder.Decoder");
            headerCls = Class.forName("javazoom.jl.decoder.Header");
            Class.forName("javazoom.jl.decoder.SampleBuffer");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("lib/jlayer-1.0.1.jar missing from classpath", e);
        }
        Object bitstream = bitstreamCls.getConstructor(InputStream.class).newInstance(in);
        Object decoder = decoderCls.getConstructor().newInstance();
        Method readFrame = bitstreamCls.getMethod("readFrame");
        Method closeFrame = bitstreamCls.getMethod("closeFrame");
        Method decodeFrame = decoderCls.getMethod("decodeFrame", headerCls, bitstreamCls);
        ByteArrayOutputStream pcm = new ByteArrayOutputStream(1 << 20);
        int sampleRate = 0;
        int channels = 0;
        while (true) {
            Object header;
            try {
                header = readFrame.invoke(bitstream);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw new IllegalStateException("MP3 frame read failed", e.getCause());
            }
            if (header == null) break;
            Object out;
            try {
                out = decodeFrame.invoke(decoder, header, bitstream);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw new IllegalStateException("MP3 frame decode failed", e.getCause());
            }
            Class<?> outCls = out.getClass();
            if (sampleRate == 0) {
                sampleRate = ((Number) outCls.getMethod("getSampleFrequency").invoke(out)).intValue();
                channels = ((Number) outCls.getMethod("getChannelCount").invoke(out)).intValue();
            }
            short[] s = (short[]) outCls.getMethod("getBuffer").invoke(out);
            int len = ((Number) outCls.getMethod("getBufferLength").invoke(out)).intValue();
            for (int i = 0; i < len; i++) {
                pcm.write(s[i] & 0xFF);
                pcm.write((s[i] >> 8) & 0xFF);
            }
            closeFrame.invoke(bitstream);
        }
        if (sampleRate == 0) throw new IllegalStateException("Empty MP3 stream");
        return new Decoded(pcm.toByteArray(),
                new AudioFormat(sampleRate, 16, channels, true, false));
    }
}
