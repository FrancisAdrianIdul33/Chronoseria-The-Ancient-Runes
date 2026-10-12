package runes;

import java.io.IOException;
import java.io.InputStream;

/** Loads arc/asset blobs. Replaces a.a(String) DataInputStream loader. */
public final class AssetLoader {
    private AssetLoader() {}

    public static InputStream open(String resourcePath) throws IOException {
        InputStream in = AssetLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Missing resource: " + resourcePath);
        }
        return in;
    }
}
