package runes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified interactive village test scene.
 * WASD + arrow keys move, ENTER/Z talks, ESC returns to menu (handled by InputManager).
 * Inn sits just inside the south gate so it is the first building visible on entry.
 */
public final class VillageScene {
    static final int T = 16;
    static final int MW = 40;
    static final int MH = 30;
    static final int WORLD_W = MW * T;
    static final int WORLD_H = MH * T;
    static final double SPEED = 2.4;
    static final int MSG_TICKS = 300;

    /** Integer pixel rect used for solids. */
    static final class Rect {
        final int x, y, w, h;
        Rect(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean hits(double px, double py, int pw, int ph) {
            return px < x + w && px + pw > x && py < y + h && py + ph > y;
        }
    }

    static final class Building {
        final String name;
        final int tx, ty, tw, th;
        final int doorTx, doorTy;
        final String blurb;
        Building(String name, int tx, int ty, int tw, int th, int doorTx, int doorTy, String blurb) {
            this.name = name; this.tx = tx; this.ty = ty; this.tw = tw; this.th = th;
            this.doorTx = doorTx; this.doorTy = doorTy; this.blurb = blurb;
        }
    }

    static final class Npc {
        final String name;
        final int x, y;
        final String line;
        final Color color;
        Npc(String name, int tileX, int tileY, String line, Color color) {
            this.name = name;
            this.x = tileX * T + T / 2;
            this.y = tileY * T + T / 2;
            this.line = line;
            this.color = color;
        }
    }

    private final List<Rect> solids = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final List<Npc> npcs = new ArrayList<>();

    private double px, py;
    private static final int PW = 10;
    private static final int PH = 12;

    private String message = "";
    private int msgTimer;

    public VillageScene() {
        buildMap();
        reset();
    }

    /** Spawn just inside the gate on the entrance road, clear of the Inn. */
    public void reset() {
        px = 19.5 * T - PW / 2.0;
        py = 28 * T - PH;
        message = "Follow the road north — the Inn is the first building.";
        msgTimer = MSG_TICKS;
    }

    private void solid(int x, int y, int w, int h) {
        solids.add(new Rect(x, y, w, h));
    }

    private void building(String name, int tx, int ty, int tw, int th,
                          int doorTx, int doorTy, String blurb) {
        buildings.add(new Building(name, tx, ty, tw, th, doorTx, doorTy, blurb));
        solid(tx * T, ty * T, tw * T, th * T);
    }

    private void buildMap() {
        // Perimeter walls (1 tile thick). South row has a 4-tile gate opening (x 18-21).
        solid(0, 0, WORLD_W, T);                       // north
        solid(0, 0, T, WORLD_H);                       // west
        solid((MW - 1) * T, 0, T, WORLD_H);            // east
        solid(0, (MH - 1) * T, 18 * T, T);             // south-west of gate
        solid(22 * T, (MH - 1) * T, 18 * T, T);        // south-east of gate
        solid(18 * T, (MH - 1) * T, T, T);             // gate post west
        solid(21 * T, (MH - 1) * T, T, T);             // gate post east
        // Gate passage between posts (x 19-20) stays walkable.

        // Buildings around the central square. Inn is just inside the gate.
        building("INN", 15, 24, 7, 3, 18, 27, "Inn — beds for the night. (Test: doors are solid.)");
        building("TAVERN", 9, 15, 4, 3, 13, 16, "Tavern — rumors over a drink.");
        building("SANCTUARY", 27, 9, 4, 3, 28, 12, "Sanctuary — quiet revival hall.");
        building("BLACKSMITH", 28, 16, 4, 3, 28, 19, "Blacksmith — clang of the forge.");
        building("WIZARD", 8, 7, 4, 3, 10, 10, "Wizard's Domain — mind the wards.");
        building("MARKET", 16, 10, 6, 2, 19, 12, "Market — supplies for the road.");
        building("MENTOR", 25, 21, 4, 3, 26, 24, "Mentor's House — skills are taught here.");

        // Blacksmith outdoor work area: anvil next to the shop.
        solid(25 * T + 2, 16 * T + 4, 12, 8);

        // Trees (solid bases) and rocks.
        int[][] trees = {{5, 5}, {33, 6}, {6, 25}, {34, 25}, {12, 22}, {30, 24}, {4, 13}};
        for (int[] t : trees) {
            solid(t[0] * T + 3, t[1] * T + 8, 10, 6);
        }
        int[][] rocks = {{24, 8}, {7, 19}};
        for (int[] r : rocks) {
            solid(r[0] * T + 2, r[1] * T + 4, 12, 10);
        }

        // Villagers standing on paths (solid, talkable).
        npcs.add(new Npc("MIRA", 20, 16, "Mira: \"Inn's right by the gate — you can't miss it.\"", GamePalette.ARCANE_BLUE));
        npcs.add(new Npc("Bram", 13, 18, "Bram: \"Market's north of the square. Mind the walls.\"", GamePalette.MYSTIC_VIOLET));
        npcs.add(new Npc("ODD", 18, 13, "Odd: \"Sanctuary's quiet. Good.\"", GamePalette.ARCANE_BLUE));
        for (Npc n : npcs) {
            solid(n.x - 5, n.y - 5, 10, 10);
        }
    }

    public void update(InputManager in) {
        int dx = 0, dy = 0;
        if (in.isHeld(KeyEvent.VK_A) || in.isHeld(KeyEvent.VK_LEFT)) dx -= 1;
        if (in.isHeld(KeyEvent.VK_D) || in.isHeld(KeyEvent.VK_RIGHT)) dx += 1;
        if (in.isHeld(KeyEvent.VK_W) || in.isHeld(KeyEvent.VK_UP)) dy -= 1;
        if (in.isHeld(KeyEvent.VK_S) || in.isHeld(KeyEvent.VK_DOWN)) dy += 1;
        if (dx != 0 && dy != 0) {
            double step = SPEED / Math.sqrt(2);
            tryMove(px + dx * step, py);
            tryMove(px, py + dy * step);
        } else if (dx != 0 || dy != 0) {
            tryMove(px + dx * SPEED, py);
            tryMove(px, py + dy * SPEED);
        }
        if (in.drainTalkPressed()) {
            interact();
        }
        if (msgTimer > 0) {
            msgTimer--;
            if (msgTimer == 0) {
                message = "";
            }
        }
    }

    private void tryMove(double nx, double ny) {
        double cx = Math.max(T, Math.min(WORLD_W - T - PW, nx));
        double cy = Math.max(T, Math.min(WORLD_H - T - PH, ny));
        for (Rect s : solids) {
            if (s.hits(cx, cy, PW, PH)) {
                return; // blocked on this axis
            }
        }
        px = cx;
        py = cy;
    }

    private void interact() {
        double cx = px + PW / 2.0;
        double cy = py + PH / 2.0;
        String best = null;
        double bestD = 34 * 34;
        for (Building b : buildings) {
            double dx = b.doorTx * T + T / 2.0;
            double dy = b.doorTy * T + T / 2.0;
            double d = (cx - dx) * (cx - dx) + (cy - dy) * (cy - dy);
            if (d < bestD) {
                bestD = d;
                best = b.blurb;
            }
        }
        for (Npc n : npcs) {
            double d = (cx - n.x) * (cx - n.x) + (cy - n.y) * (cy - n.y);
            if (d < bestD) {
                bestD = d;
                best = n.line;
            }
        }
        if (best != null) {
            message = best;
            msgTimer = MSG_TICKS;
        } else {
            message = "No one here. Try a door or a villager.";
            msgTimer = MSG_TICKS / 2;
        }
    }

    public void draw(Graphics2D g2, int viewW, int viewH) {
        int camX = (int) Math.max(0, Math.min(WORLD_W - viewW, px + PW / 2.0 - viewW / 2.0));
        int camY = (int) Math.max(0, Math.min(WORLD_H - viewH, py + PH / 2.0 - viewH / 2.0));
        if (WORLD_W <= viewW) camX = 0;
        if (WORLD_H <= viewH) camY = 0;

        // Ground: outside-wall void, grass inside, paths, square, road.
        g2.setColor(GamePalette.ROYAL_DEEP);
        g2.fillRect(0, 0, viewW, viewH);
        g2.setColor(GamePalette.FOREST_GREEN);
        g2.fillRect(T - camX, T - camY, WORLD_W - 2 * T, WORLD_H - 2 * T);
        g2.setColor(GamePalette.PARCHMENT_DARK);
        g2.fillRect(13 * T - camX, 14 * T - camY, 14 * T, 6 * T);   // square
        g2.setColor(GamePalette.PARCHMENT);
        g2.fillRect(18 * T - camX, 19 * T - camY, 4 * T, 10 * T);   // entrance road
        g2.fillRect(18 * T - camX, 12 * T - camY, 4 * T, 2 * T);    // road spur to market

        // Walls.
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.fillRect(0 - camX, 0 - camY, WORLD_W, T);
        g2.fillRect(0 - camX, 0 - camY, T, WORLD_H);
        g2.fillRect((MW - 1) * T - camX, 0 - camY, T, WORLD_H);
        g2.fillRect(0 - camX, (MH - 1) * T - camY, 18 * T, T);
        g2.fillRect(22 * T - camX, (MH - 1) * T - camY, 18 * T, T);
        g2.setColor(GamePalette.LEGENDARY_GOLD);
        g2.fillRect(18 * T - camX, (MH - 1) * T - camY, T, T);
        g2.fillRect(21 * T - camX, (MH - 1) * T - camY, T, T);
        g2.setColor(GamePalette.PARCHMENT);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g2.drawString("GATE", (int) (19 * T - camX + 4), (MH - 1) * T - camY - 4);

        // Buildings: parchment body, blue roof band, gold door, label.
        for (Building b : buildings) {
            int x = b.tx * T - camX;
            int y = b.ty * T - camY;
            int w = b.tw * T;
            int h = b.th * T;
            boolean inn = "INN".equals(b.name);
            g2.setColor(GamePalette.PARCHMENT);
            g2.fillRect(x, y, w, h);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.fillRect(x, y, w, 8);
            g2.drawRect(x, y, w - 1, h - 1);
            if (inn) {
                g2.setColor(GamePalette.LEGENDARY_GOLD);
                g2.drawRect(x - 2, y - 2, w + 3, h + 3);
            }
            int doorX = b.doorTx * T - camX + 3;
            int doorY = (b.doorTy * T - camY);
            if (b.doorTy >= b.ty + b.th) {
                doorY = y + h - 2;
            } else if (b.doorTy < b.ty) {
                doorY = y - 8;
            }
            g2.setColor(GamePalette.LEGENDARY_GOLD);
            g2.fillRect(doorX, Math.min(doorY, y + h - 10), 10, 10);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, inn ? 12 : 10));
            int labelW = g2.getFontMetrics().stringWidth(b.name);
            g2.drawString(b.name, x + (w - labelW) / 2, y - 4);
        }

        // Trees, rocks, anvil.
        int[][] trees = {{5, 5}, {33, 6}, {6, 25}, {34, 25}, {12, 22}, {30, 24}, {4, 13}};
        for (int[] t : trees) {
            int x = t[0] * T - camX;
            int y = t[1] * T - camY;
            g2.setColor(GamePalette.PARCHMENT_DARK);
            g2.fillRect(x + 6, y + 8, 4, 6);
            g2.setColor(GamePalette.FOREST_GREEN);
            g2.fillOval(x, y - 4, 16, 14);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.drawOval(x, y - 4, 16, 14);
        }
        int[][] rocks = {{24, 8}, {7, 19}};
        g2.setColor(GamePalette.PARCHMENT_DARK);
        for (int[] r : rocks) {
            g2.fillRect(r[0] * T - camX + 2, r[1] * T - camY + 4, 12, 10);
        }
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.fillRect(25 * T + 2 - camX, 16 * T + 4 - camY, 12, 8);

        // NPCs.
        for (Npc n : npcs) {
            int x = n.x - camX;
            int y = n.y - camY;
            g2.setColor(n.color);
            g2.fillOval(x - 6, y - 8, 12, 14);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.drawOval(x - 6, y - 8, 12, 14);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            g2.drawString(n.name, x - 10, y - 12);
        }

        // Player.
        int pxx = (int) (px - camX);
        int pyy = (int) (py - camY);
        g2.setColor(GamePalette.LEGENDARY_GOLD);
        g2.fillOval(pxx - 1, pyy - 3, PW + 2, PH + 2);
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.drawOval(pxx - 1, pyy - 3, PW + 2, PH + 2);

        // HUD: status bar + dialogue box.
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.fillRect(0, 0, viewW, 22);
        g2.setColor(GamePalette.PARCHMENT);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g2.drawString("VILLAGE TEST - WASD/arrows move  ENTER talk  ESC menu", 8, 15);
        if (!message.isEmpty()) {
            int boxH = 44;
            g2.setColor(GamePalette.PARCHMENT);
            g2.fillRect(12, viewH - boxH - 10, viewW - 24, boxH);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.drawRect(12, viewH - boxH - 10, viewW - 24, boxH);
            g2.setColor(GamePalette.LEGENDARY_GOLD);
            g2.fillRect(12, viewH - boxH - 10, 5, 5);
            g2.fillRect(viewW - 17, viewH - boxH - 10, 5, 5);
            g2.fillRect(12, viewH - 15, 5, 5);
            g2.fillRect(viewW - 17, viewH - 15, 5, 5);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g2.drawString(message, 24, viewH - 24);
        }
    }
}
