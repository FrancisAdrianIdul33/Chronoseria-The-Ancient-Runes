package runes;

/**
 * Dialogue panel module: one open conversation at a time.
 *
 * <p>Flow: {@link #open} on first E facing a villager, {@link #advance} on each
 * further E. Advancing past the final line closes the panel (returns null):
 * that E is the "done" that shuts it. Walking more than two tiles away also
 * closes it (the scene calls {@link #close}).
 */
public final class Dialogue {
    private VillageScene.Npc speaker;
    private int shown; // lines shown this conversation
    private boolean open;

    public void open(VillageScene.Npc npc) {
        speaker = npc;
        shown = 0;
        open = true;
    }

    /**
     * Show the next line.
     *
     * @return text to display, or null when the panel just closed (E was done).
     */
    public String advance() {
        if (!open || speaker == null) return null;
        if (shown >= speaker.lines.length) {
            close();
            return null;
        }
        String line = speaker.lines[speaker.stage % speaker.lines.length];
        speaker.stage++;
        shown++;
        return line;
    }

    public void close() {
        open = false;
        speaker = null;
        shown = 0;
    }

    public boolean isOpen() {
        return open;
    }

    /** True once every line has been shown: the next E closes the panel. */
    public boolean isLast() {
        return open && speaker != null && shown >= speaker.lines.length;
    }

    /** Prompt hint, e.g. "E: next 1/3" or "E: done". */
    public String hint() {
        if (!open || speaker == null) return "";
        if (isLast()) return "E: done";
        return "E: next " + shown + "/" + speaker.lines.length;
    }

    public int speakerCol() {
        return open && speaker != null ? speaker.col : -1;
    }

    public int speakerRow() {
        return open && speaker != null ? speaker.row : -1;
    }
}
