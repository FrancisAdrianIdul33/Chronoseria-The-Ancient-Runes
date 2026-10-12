package runes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/** TITLE any-key/click (after 2s) -> MENU -> GAME; GAME reads held-key state. */
public final class InputManager extends KeyAdapter {
    private final GamePanel panel;
    private final Set<Integer> held = new HashSet<>();
    private boolean talkQueued;

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
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            held.clear();
            panel.startMenu();
            return;
        }
        if (held.add(code) && (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_Z)) {
            talkQueued = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        held.remove(e.getKeyCode());
    }

    /** True while the key is physically down (for smooth walking). */
    public boolean isHeld(int keyCode) {
        return held.contains(keyCode);
    }

    /** One-shot talk press; auto-repeats are filtered by the held set. */
    public boolean drainTalkPressed() {
        boolean t = talkQueued;
        talkQueued = false;
        return t;
    }

    public MouseAdapter mouse() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (panel.isTitle()) {
                    panel.acceptAnyKey();
                    return;
                }
                if (panel.isMenu()) {
                    int i = panel.menuItemAt(e.getX(), e.getY());
                    if (i >= 0) {
                        panel.menuSelectIndex(i);
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (panel.isMenu()) {
                    panel.menuSetHover(panel.menuItemAt(e.getX(), e.getY()));
                } else {
                    panel.menuSetHover(-1);
                }
            }
        };
    }
}
