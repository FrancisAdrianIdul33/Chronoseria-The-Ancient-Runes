package runes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Temporary village test scene for Chronoseria: The Ancient Runes.
 * Simple cell grid map: 16x12 cells of 40px = exactly 640x480 (no scrolling).
 * Scenery and actors are temporary emoji glyphs; replace with AssetLoader sprites later.
 * Controls: ARROWS/WASD move, ENTER/Z talk, ESC menu.
 */
public final class VillageScene {
    static final int COLS = 16;
    static final int ROWS = 12;
    static final int CELL = 40;

    private static final int GRASS = 0;
    private static final int PATH = 1;
    private static final int TREE = 2;
    private static final int WATER = 3;
    private static final int HOUSE = 4;
    private static final int DOOR = 5;

    /** . grass, , path, T tree, W water, H house, D door. */
    private static final String[] LAYOUT = {
        "TTTTTTTTTTTTTTTT",
        "T..HHH...HHH...T",
        "T..HDH...HDH...T",
        "T...,.....,....T",
        "T...,.....,....T",
        "T...,.....,....T",
        "T,,,,,,,,,,,,,,,T",
        "T...,..,...WW..T",
        "T...,..,...WW..T",
        "T..HDH.,.......T",
        "T..HHH.,.......T",
        "TTTTTTTTTTTTTTTT"
    };

    private static final float SPEED = 2.2f;
    private static final float NPC_SPEED = 0.9f;
    private static final String PLAYER_EMOJI = "\uD83E\uDDD9"; // wizard

    private final int[][] map = new int[ROWS][COLS];
    private final Random rng = new Random(20261012L);
    private final List<Npc> npcs = new ArrayList<>();

    private float px = 7 * CELL + CELL / 2f;
    private float py = 6 * CELL + CELL / 2f;
    private int walkTimer;
    private boolean moving;
    private int talkNpc = -1;
    private int talkLine;

    private static final class Npc {
        final String name;
        final String emoji;
        final String[] lines;
        final float homeX;
        final float homeY;
        float x;
        float y;
        float targetX;
        float targetY;
        int pause;

        Npc(String name, String emoji, String[] lines, int cellX, int cellY) {
            this.name = name;
            this.emoji = emoji;
            this.lines = lines;
            this.homeX = cellX * CELL + CELL / 2f;
            this.homeY = cellY * CELL + CELL / 2f;
            this.x = homeX;
            this.y = homeY;
            this.targetX = homeX;
            this.targetY = homeY;
        }
    }

    private static final class Actor {
        final float x;
        final float y;
        final String emoji;
        final int bob;

        Actor(float x, float y, String emoji, int bob) {
            this.x = x;
            this.y = y;
            this.emoji = emoji;
            this.bob = bob;
        }
    }

    public VillageScene() {
        reset();
    }

    public void reset() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                map[r][c] = cellFrom(LAYOUT[r].charAt(c));
            }
        }
        npcs.clear();
        npcs.add(new Npc("Mira", "\uD83D\uDC69", new String[]{
            "Rest at the Inn to recover your strength.",
            "The Tavern is lively after dusk."}, 4, 3));
        npcs.add(new Npc("Orin", "\uD83E\uDD94", new String[]{
            "Fresh remedies at the Conventional Market!",
            "Bring your TIC and trade well."}, 10, 3));
        npcs.add(new Npc("Kael", "\uD83D\uDC74", new String[]{
            "The runes remember what we forget.",
            "Wander well, traveler of Chronoseria."}, 10, 6));
        for (Npc n : npcs) {
            n.pause = 40 + rng.nextInt(80);
        }
        px = 7 * CELL + CELL / 2f;
        py = 6 * CELL + CELL / 2f;
        walkTimer = 0;
        moving = false;
        talkNpc = -1;
        talkLine = 0;
    }

    private static int cellFrom(char ch) {
        return switch (ch) {
            case ',' -> PATH;
            case 'T' -> TREE;
            case 'W' -> WATER;
            case 'H' -> HOUSE;
            case 'D' -> DOOR;
            default -> GRASS;
        };
    }

    public void update(InputManager in) {
        if (in.drainTalkPressed()) {
            if (talkNpc >= 0) {
                talkLine++;
                if (talkLine >= npcs.get(talkNpc).lines.length) {
                    talkNpc = -1;
                }
            } else {
                tryTalk();
            }
        }
        if (talkNpc >= 0) {
            moving = false;
        } else {
            movePlayer(in);
        }
        updateNpcs();
    }

    private void movePlayer(InputManager in) {
        float dx = 0;
        float dy = 0;
        if (in.isHeld(KeyEvent.VK_LEFT) || in.isHeld(KeyEvent.VK_A)) dx -= SPEED;
        if (in.isHeld(KeyEvent.VK_RIGHT) || in.isHeld(KeyEvent.VK_D)) dx += SPEED;
        if (in.isHeld(KeyEvent.VK_UP) || in.isHeld(KeyEvent.VK_W)) dy -= SPEED;
        if (in.isHeld(KeyEvent.VK_DOWN) || in.isHeld(KeyEvent.VK_S)) dy += SPEED;
        if (dx != 0 && dy != 0) {
            dx *= 0.7071f;
            dy *= 0.7071f;
        }
        moving = dx != 0 || dy != 0;
        if (moving) {
            walkTimer++;
        }
        if (dx != 0 && !blocked(px + dx, py, null)) {
            px += dx;
        }
        if (dy != 0 && !blocked(px, py + dy, null)) {
            py += dy;
        }
    }

    private boolean blocked(float cx, float cy, Npc self) {
        float left = cx - 12;
        float right = cx + 12;
        float top = cy - 8;
        float bottom = cy + 12;
        int c0 = (int) Math.floor(left / CELL);
        int c1 = (int) Math.floor((right - 0.01f) / CELL);
        int r0 = (int) Math.floor(top / CELL);
        int r1 = (int) Math.floor((bottom - 0.01f) / CELL);
        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                if (r < 0 || c < 0 || r >= ROWS || c >= COLS || isSolid(map[r][c])) {
                    return true;
                }
            }
        }
        if (self == null) {
            for (Npc n : npcs) {
                if (Math.abs(n.x - cx) < 22 && Math.abs(n.y - cy) < 26) {
                    return true;
                }
            }
        } else if (Math.abs(px - cx) < 22 && Math.abs(py - cy) < 26) {
            return true;
        }
        return false;
    }

    private static boolean isSolid(int tile) {
        return tile == TREE || tile == WATER || tile == HOUSE || tile == DOOR;
    }

    private void updateNpcs() {
        for (Npc n : npcs) {
            if (n.pause > 0) {
                n.pause--;
                continue;
            }
            float dx = n.targetX - n.x;
            float dy = n.targetY - n.y;
            float dist = (float) Math.hypot(dx, dy);
            if (dist < 1f) {
                pickTarget(n);
                continue;
            }
            float step = Math.min(NPC_SPEED, dist);
            float nx = n.x + dx / dist * step;
            float ny = n.y + dy / dist * step;
            if (blocked(nx, ny, n)) {
                pickTarget(n);
                continue;
            }
            n.x = nx;
            n.y = ny;
        }
    }

    private void pickTarget(Npc n) {
        int homeC = (int) (n.homeX / CELL);
        int homeR = (int) (n.homeY / CELL);
        for (int tries = 0; tries < 12; tries++) {
            int c = homeC + rng.nextInt(7) - 3;
            int r = homeR + rng.nextInt(7) - 3;
            if (c < 1 || r < 1 || c >= COLS - 1 || r >= ROWS - 1) {
                continue;
            }
            if (isSolid(map[r][c])) {
                continue;
            }
            n.targetX = c * CELL + CELL / 2f;
            n.targetY = r * CELL + CELL / 2f;
            n.pause = 20 + rng.nextInt(70);
            return;
        }
        n.pause = 60;
    }

    private void tryTalk() {
        for (int i = 0; i < npcs.size(); i++) {
            Npc n = npcs.get(i);
            if (Math.abs(n.x - px) < 56 && Math.abs(n.y - py) < 56) {
                talkNpc = i;
                talkLine = 0;
                return;
            }
        }
    }

    public void render(Graphics2D g2, int w, int h, int tick) {
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 26);
        // --- cells ---
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = c * CELL;
                int y = r * CELL;
                int t = map[r][c];
                g2.setColor(t == PATH ? new Color(0xC2AE84) : new Color(0x5E9C55));
                g2.fillRect(x, y, CELL, CELL);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRect(x, y, CELL - 1, CELL - 1);
                String glyph = glyph(t);
                if (glyph != null) {
                    drawGlyph(g2, emojiFont, glyph, x + CELL / 2f, y + CELL / 2f, 0);
                }
            }
        }
        // --- actors (y-sorted so the player can stand behind NPCs) ---
        List<Actor> actors = new ArrayList<>();
        for (Npc n : npcs) {
            boolean walking = n.pause <= 0
                    && (Math.abs(n.targetX - n.x) > 1 || Math.abs(n.targetY - n.y) > 1);
            int bob = walking && (tick / 8) % 2 == 0 ? -2 : 0;
            actors.add(new Actor(n.x, n.y, n.emoji, bob));
        }
        int playerBob = moving && (walkTimer / 6) % 2 == 0 ? -2 : 0;
        actors.add(new Actor(px, py, PLAYER_EMOJI, playerBob));
        actors.sort(Comparator.comparingDouble(a -> a.y));
        for (Actor a : actors) {
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillOval((int) a.x - 12, (int) a.y + 8, 24, 9);
            drawGlyph(g2, emojiFont, a.emoji, a.x, a.y, a.bob);
        }
        // --- HUD ---
        g2.setColor(new Color(0x1A, 0x25, 0x38, 210));
        g2.fillRect(0, 0, w, 24);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2.setColor(GamePalette.PARCHMENT);
        g2.drawString("Village (test scene)    ARROWS/WASD: move    ENTER/Z: talk    ESC: menu", 8, 16);
        // --- dialogue ---
        if (talkNpc >= 0) {
            drawDialogue(g2, w, h, emojiFont);
        }
    }

    private static String glyph(int tile) {
        return switch (tile) {
            case TREE -> "\uD83C\uDF33";
            case WATER -> "\uD83C\uDF0A";
            case HOUSE -> "\uD83C\uDFE0";
            case DOOR -> "\uD83D\uDEAA";
            default -> null;
        };
    }

    private static void drawGlyph(Graphics2D g2, Font font, String glyph, float cx, float cy, int bob) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int x = Math.round(cx) - fm.stringWidth(glyph) / 2;
        int y = Math.round(cy) + (fm.getAscent() - fm.getDescent()) / 2 - 3 + bob;
        g2.drawString(glyph, x, y);
    }

    private void drawDialogue(Graphics2D g2, int w, int h, Font emojiFont) {
        Npc n = npcs.get(talkNpc);
        int bx = 16;
        int by = h - 100;
        int bw = w - 32;
        int bh = 84;
        g2.setColor(GamePalette.PARCHMENT);
        g2.fillRect(bx, by, bw, bh);
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(bx + 1, by + 1, bw - 2, bh - 2);
        g2.setColor(GamePalette.LEGENDARY_GOLD);
        int s = 10;
        g2.fillRect(bx + 3, by + 3, s, s);
        g2.fillRect(bx + bw - 3 - s, by + 3, s, s);
        g2.fillRect(bx + 3, by + bh - 3 - s, s, s);
        g2.fillRect(bx + bw - 3 - s, by + bh - 3 - s, s, s);
        drawGlyph(g2, emojiFont, n.emoji, bx + 42, by + 34, 0);
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 15));
        g2.setColor(GamePalette.LEGENDARY_GOLD);
        g2.drawString(n.name, bx + 76, by + 30);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.drawString(n.lines[talkLine], bx + 76, by + 58);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        g2.drawString("ENTER/Z: continue", bx + bw - 130, by + bh - 10);
    }
}
