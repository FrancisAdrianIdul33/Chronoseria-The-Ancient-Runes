package runes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** TITLE any-key/click (after 2s) -> MENU -> GAME, ESC steps back. */
public final class InputManager extends KeyAdapter {
    private final GamePanel panel;

    public InputManager(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (panel.isTitle()) {
            panel.acceptAnyKey();
            return;
        }
        if (panel.isMenu()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP -> panel.menuUp();
                case KeyEvent.VK_DOWN -> panel.menuDown();
                case KeyEvent.VK_ENTER -> panel.menuSelect();
                case KeyEvent.VK_ESCAPE -> panel.showTitle();
                default -> { }
            }
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            panel.startMenu();
        }
    }

    public MouseAdapter mouse() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (panel.isTitle()) {
                    panel.acceptAnyKey();
                }
            }
        };
    }
}
