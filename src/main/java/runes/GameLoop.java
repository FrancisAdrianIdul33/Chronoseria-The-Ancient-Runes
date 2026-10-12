package runes;

/** Fixed-step loop. Replaces Canvas run(). */
public final class GameLoop implements Runnable {
    private final GamePanel panel;
    private volatile boolean running = true;

    public GameLoop(GamePanel panel) {
        this.panel = panel;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        final long stepNanos = 16_666_666L;
        long next = System.nanoTime();
        while (running) {
            panel.tick();
            next += stepNanos;
            long sleepMs = (next - System.nanoTime()) / 1_000_000L;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                next = System.nanoTime();
            }
        }
    }
}
