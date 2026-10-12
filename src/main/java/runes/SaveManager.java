package runes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** File-based saves. Replaces RecordStore. */
public final class SaveManager {
    private SaveManager() {}

    public static Path saveFile() {
        return Paths.get(System.getProperty("user.home"), ".ancient-runes", "save.json");
    }

    public static void write(String json) throws IOException {
        Path file = saveFile();
        Files.createDirectories(file.getParent());
        Files.writeString(file, json);
    }

    public static String read() throws IOException {
        return Files.readString(saveFile());
    }
}
