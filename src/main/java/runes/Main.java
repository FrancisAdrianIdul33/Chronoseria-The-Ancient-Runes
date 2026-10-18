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
            // Keep the 800x600 game area centered and unscaled (letterboxed) in fullscreen.
            javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.GridBagLayout());
            root.setBackground(java.awt.Color.BLACK);
            root.setPreferredSize(new java.awt.Dimension(800, 600));
            root.add(panel, new java.awt.GridBagConstraints());
            frame.setContentPane(root);
            InputManager input = new InputManager(panel);
            panel.addKeyListener(input); // listen on the focused component, not the frame
            panel.addMouseListener(input.mouse());
            frame.addMouseMotionListener(input.mouse());
            panel.attachInput(input);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
            new Thread(new GameLoop(panel), "game-loop").start();
        });
    }
}
