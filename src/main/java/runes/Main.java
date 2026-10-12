package runes;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Chronoseria: The Ancient Runes (JDK 26). */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chronoseria: The Ancient Runes");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel panel = new GamePanel();
            panel.setFocusable(true);
            frame.setContentPane(panel);
            InputManager input = new InputManager(panel);
            frame.addKeyListener(input);
            panel.addMouseListener(input.mouse());
            panel.attachInput(input);
            frame.setSize(640, 480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
            new Thread(new GameLoop(panel), "game-loop").start();
        });
    }
}
