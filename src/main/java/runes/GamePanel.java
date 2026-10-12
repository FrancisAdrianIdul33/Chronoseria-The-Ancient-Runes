package runes;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Chronoseria: The Ancient Runes. TITLE (fade) -> MENU -> GAME. */
public final class GamePanel extends JPanel {
    public enum Screen { TITLE, MENU, GAME }

    private static final int PROMPT_TICKS = 120;

    private int tick;
    private Screen screen = Screen.TITLE;
    private int menuIndex;
    private final String[] menuItems = {"New Game", "Load Game", "Continue", "Chronicles", "Bestiary", "Settings"};

    public void tick() {
        tick++;
        repaint();
    }

    public boolean isTitle() {
        return screen == Screen.TITLE;
    }

    public boolean isMenu() {
        return screen == Screen.MENU;
    }

    public boolean isPromptReady() {
        return tick >= PROMPT_TICKS;
    }

    public void acceptAnyKey() {
        if (screen == Screen.TITLE && isPromptReady()) {
            screen = Screen.MENU;
        }
    }

    public void startMenu() {
        screen = Screen.MENU;
    }

    public void startGame() {
        screen = Screen.GAME;
    }

    public void showTitle() {
        screen = Screen.TITLE;
        tick = 0;
    }

    public void menuUp() {
        menuIndex = (menuIndex + menuItems.length - 1) % menuItems.length;
    }

    public void menuDown() {
        menuIndex = (menuIndex + 1) % menuItems.length;
    }

    public void menuSelect() {
        screen = Screen.GAME;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            switch (screen) {
                case TITLE -> drawTitle(g2);
                case MENU -> drawMenu(g2);
                default -> drawGame(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawGame(Graphics2D g2) {
        int w = getWidth();
        g2.setColor(GamePalette.ROYAL_DEEP);
        g2.fillRect(0, 0, w, getHeight());
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.fillRect(0, 0, w, 60);
        g2.setColor(GamePalette.PARCHMENT);
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 14));
        g2.drawString("Chronoseria: The Ancient Runes  (ESC=menu)", 16, 36);
        g2.setColor(GamePalette.ROYAL_BLUE.brighter());
        for (int x = 0; x < w; x += 16) {
            g2.drawLine(x, 70, x, getHeight());
        }
        for (int y = 70; y < getHeight(); y += 16) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void drawMenu(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        g2.setColor(GamePalette.PARCHMENT);
        g2.fillRect(0, 0, w, h);
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.fillRect(0, 0, w, 90);
        drawCentered(g2, "CHRONOSERIA", w / 2, 38, new Font(Font.SERIF, Font.BOLD, 22), GamePalette.PARCHMENT, 1f);
        drawCentered(g2, "THE ANCIENT RUNES", w / 2, 62, new Font(Font.SANS_SERIF, Font.PLAIN, 12), GamePalette.LEGENDARY_GOLD, 1f);
        int y = 140;
        for (int i = 0; i < menuItems.length; i++) {
            boolean sel = i == menuIndex;
            g2.setColor(sel ? GamePalette.LEGENDARY_GOLD : GamePalette.PARCHMENT_DARK);
            g2.fillRect(60, y - 22, w - 120, 38);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.setStroke(new BasicStroke(sel ? 3f : 1f));
            g2.drawRect(60, y - 22, w - 120, 38);
            if (sel) {
                g2.setColor(GamePalette.LEGENDARY_GOLD);
                g2.fillPolygon(new int[]{38, 38, 50}, new int[]{y - 12, y + 2, y - 5}, 3);
            }
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.setFont(new Font(Font.SERIF, Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(menuItems[i], 80, y + (fm.getAscent() - fm.getDescent()) / 2 + 1);
            y += 52;
        }
        drawCentered(g2, "UP/DOWN + ENTER   ESC=title", w / 2, h - 24, new Font(Font.MONOSPACED, Font.PLAIN, 11), GamePalette.ROYAL_BLUE, 1f);
    }

    private void drawTitle(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, GamePalette.ROYAL_DEEP, 0, h, GamePalette.ROYAL_BLUE));
        g2.fillRect(0, 0, w, h);

        int cx = w / 2;
        int cy = h / 2 - 20;
        float titleAlpha = clamp01(tick / 60f);
        float subAlpha = clamp01((tick - 30) / 60f);

        drawCentered(g2, "CHRONOSERIA", cx, cy - 40, new Font(Font.SERIF, Font.BOLD, 38), GamePalette.PARCHMENT, titleAlpha);
        drawSpaced(g2, "THE ANCIENT RUNES", cx, cy - 8, new Font(Font.SANS_SERIF, Font.PLAIN, 15), GamePalette.LEGENDARY_GOLD, subAlpha);

        if (tick >= PROMPT_TICKS) {
            float glow = 0.45f + 0.25f * (float) Math.sin(tick * 0.08);
            drawCentered(g2, "click any key", cx, cy + 120, new Font(Font.SANS_SERIF, Font.PLAIN, 16), GamePalette.LEGENDARY_GOLD, glow * 0.5f + 0.2f);
            drawCentered(g2, "click any key", cx, cy + 120, new Font(Font.SANS_SERIF, Font.PLAIN, 16), GamePalette.PARCHMENT, Math.min(1f, (tick - PROMPT_TICKS) / 30f));
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.25f));
            g2.setColor(GamePalette.LEGENDARY_GOLD);
            FontMetrics fm = g2.getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            int tw = fm.stringWidth("click any key");
            g2.fillRect(cx - tw / 2 - 8, cy + 108, tw + 16, 24);
            g2.setComposite(old);
        }
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static void drawCentered(Graphics2D g2, String text, int cx, int y, Font font, java.awt.Color color, float alpha) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp01(alpha)));
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, y);
        g2.setComposite(old);
    }

    private static void drawSpaced(Graphics2D g2, String text, int cx, int y, Font font, java.awt.Color color, float alpha) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp01(alpha)));
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        StringBuilder spaced = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            spaced.append(text.charAt(i));
            if (i < text.length() - 1) {
                spaced.append(' ');
            }
        }
        String s = spaced.toString();
        g2.drawString(s, cx - fm.stringWidth(s) / 2, y);
        g2.setComposite(old);
    }
}
