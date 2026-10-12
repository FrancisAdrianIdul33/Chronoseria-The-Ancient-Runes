package runes;

import java.util.Random;

/** Step-counter + 0-255 roll from doc 12-encounters-spawns. */
public final class EncounterSystem {
    private final Random random = new Random();
    private int steps;

    public void step() {
        steps++;
    }

    public boolean roll(int rate0to255) {
        return random.nextInt(256) < rate0to255;
    }

    public int getSteps() {
        return steps;
    }
}
