package runes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

/**
 * FINAL village walk test: Coneria-style 32x32 town.
 *
 * <p>Grid rules: TILE=16, 2x whole scale, cell layers (ground/solid),
 * one-cell steps with tween, doors bottom-center with an entrance cell
 * in front, E works only from the entrance facing up, F3 debug overlay,
 * build-time validation that never gets removed.
 */
public final class VillageScene {
    // ---- 14. Constants in one place ----
    static final int TILE = 16;
    static final int MAP_W = 32;
    static final int MAP_H = 32;
    static final int WORLD_W = MAP_W * TILE;
    static final int WORLD_H = MAP_H * TILE;
    static final int VIEW_TX = 20;
    static final int VIEW_TY = 12;
    static final int SCALE = 2; // whole-number scale only
    static final int STEP_TICKS = 12; // ~0.2s per cell at 60 UPS
    static final int ANIM_TICKS = 6; // walk clock, separate from movement
    static final int MSG_TICKS = 300;
    static final int SPAWN_C = 15;
    static final int SPAWN_R = 29;
    static final int DOOR_COUNT = 11;

    /** Ground layer tile kinds. */
    enum Ground { GRASS, PATH, ROAD, SQUARE, TREE, GATE }

    /** Building kinds double as roof/window color keys. */
    enum Kind { INN, HOUSE, ITEM, WEAPON, ARMOR, MAGIC, BIG }

    /** 4 movement directions only. */
    enum Dir {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx, dy;
        Dir(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    static final class Building {
        final String name;
        final Kind kind;
        final int tx, ty, tw, th;
        final int doorX, doorY; // bottom-center facade cell, walkable
        final int enterX, enterY; // walkable cell directly in front
        final String blurb;
        final boolean shop; // lit windows + sign
        final boolean chimney;
        Building(String name, Kind kind, int tx, int ty, int tw, int th, String blurb,
                 boolean shop, boolean chimney) {
            this.name = name; this.kind = kind;
            this.tx = tx; this.ty = ty; this.tw = tw; this.th = th;
            this.doorX = tx + tw / 2; // w is odd, so this is the center
            this.doorY = ty + th - 1; // always the bottom row
            this.enterX = doorX;
            this.enterY = doorY + 1;
            this.blurb = blurb;
            this.shop = shop;
            this.chimney = chimney;
        }
    }

    static final class Npc {
        final String name;
        final int cx, cy;
        final String line;
        final Color clothes;
        Npc(String name, int cx, int cy, String line, Color clothes) {
            this.name = name; this.cx = cx; this.cy = cy;
            this.line = line; this.clothes = clothes;
        }
    }

    // ---- Layers ----
    private final Ground[][] ground = new Ground[MAP_W][MAP_H];
    private final boolean[][] solid = new boolean[MAP_W][MAP_H];
    private final boolean[][] isBuilding = new boolean[MAP_W][MAP_H];
    private final java.util.List<Building> buildings = new java.util.ArrayList<>();
    private final java.util.List<Npc> npcs = new java.util.ArrayList<>();
    private final java.util.List<int[]> trees = new java.util.ArrayList<>();
    private final java.util.List<int[]> fences = new java.util.ArrayList<>();
    private int fountainX = 15, fountainY = 15; // 2x2 top-left

    // ---- Player (grid based) ----
    private int col, row;
    private Dir facing = Dir.UP;
    private boolean stepping;
    private int fromC, fromR, toC, toR, stepT;
    private double renderCx, renderCy;
    private int animT;
    private int walkFrame;

    private String message = "";
    private int msgTimer;

    // ---- Input edge tracking (held-state, no key repeat) ----
    private boolean prevUp, prevDown, prevLeft, prevRight, prevF3;
    private Dir preferred;

    // ---- Debug + FPS ----
    private boolean debug;
    private int fpsFrames;
    private long fpsLast = System.currentTimeMillis();
    private int fps;

    // ---- Camera (whole pixels) ----
    private int camX, camY;

    public VillageScene() {
        buildMap();
        reset();
    }

    // ---- 1. Grid helpers ----
    static int worldToTile(int worldPx) {
        return Math.floorDiv(worldPx, TILE);
    }

    static int tileToWorld(int tile) {
        return tile * TILE;
    }

    static boolean isInsideMap(int c, int r) {
        return c >= 0 && r >= 0 && c < MAP_W && r < MAP_H;
    }

    public void reset() {
        col = SPAWN_C;
        row = SPAWN_R;
        facing = Dir.UP;
        stepping = false;
        snapRender();
        animT = 0;
        walkFrame = 0;
        message = "North, past the fountain — the Inn has a blue roof.";
        msgTimer = MSG_TICKS;
    }

    private void snapRender() {
        renderCx = col * TILE + TILE / 2.0;
        renderCy = row * TILE + TILE / 2.0;
    }

    private void fillGround(int x0, int y0, int w, int h, Ground g) {
        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                ground[x][y] = g;
            }
        }
    }

    private void tree(int c, int r) {
        trees.add(new int[]{c, r});
        solid[c][r] = true;
        ground[c][r] = Ground.TREE;
    }

    private void building(String name, Kind kind, int tx, int ty, int tw, int th,
                          String blurb, boolean shop, boolean chimney) {
        Building b = new Building(name, kind, tx, ty, tw, th, blurb, shop, chimney);
        buildings.add(b);
        for (int y = ty; y < ty + th; y++) {
            for (int x = tx; x < tx + tw; x++) {
                isBuilding[x][y] = true;
                if (x == b.doorX && y == b.doorY) {
                    continue; // door cell stays walkable
                }
                solid[x][y] = true;
            }
        }
    }

    private void buildMap() {
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                ground[x][y] = Ground.GRASS;
            }
        }

        // ---- 4. Paths: straight runs, clean 90-degree corners ----
        fillGround(15, 8, 2, 23, Ground.ROAD);   // main road cols 15-16 rows 8-30
        fillGround(3, 8, 26, 2, Ground.ROAD);    // north lane rows 8-9
        fillGround(3, 26, 26, 2, Ground.ROAD);   // south lane rows 26-27
        fillGround(11, 13, 10, 6, Ground.SQUARE); // plaza cols 11-20 rows 13-18
        fillGround(18, 12, 1, 3, Ground.PATH);   // market spur x18 rows 12-14
        fillGround(4, 17, 7, 1, Ground.PATH);    // west spur row 17 cols 4-10
        fillGround(21, 17, 5, 1, Ground.PATH);   // east spur row 17 cols 21-25
        fillGround(30, 20, 1, 7, Ground.PATH);   // east connector x30 rows 20-26
        ground[15][30] = Ground.GATE; // gate floor, rows 30-31 stay walkable
        ground[16][30] = Ground.GATE;
        ground[15][31] = Ground.GATE;
        ground[16][31] = Ground.GATE;

        // Fountain 2x2, solid, walked around inside the plaza.
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                solid[fountainX + x][fountainY + y] = true;
            }
        }

        // ---- 5. Buildings: 11 doors, bottom-center, facing south ----
        building("INN", Kind.INN, 3, 3, 5, 4, "Inn — rest up for the road ahead.", true, true);
        building("HOUSE", Kind.HOUSE, 10, 4, 3, 3, "A quiet house. The door is locked.", false, true);
        building("WEAPON", Kind.WEAPON, 17, 4, 5, 3, "Weapon shop — blades over the door.", true, false);
        building("ARMOR", Kind.ARMOR, 24, 4, 5, 3, "Armor shop — plate and mail.", true, false);
        building("ITEM", Kind.ITEM, 3, 13, 3, 3, "Item shop — odds and ends.", true, false);
        building("HOUSE", Kind.HOUSE, 7, 13, 3, 3, "A quiet house. The door is locked.", false, false);
        building("HOUSE", Kind.BIG, 23, 13, 5, 3, "A big house. Someone important lives here.", false, true);
        building("MAGIC", Kind.MAGIC, 7, 22, 3, 3, "Magic shop — it hums.", true, false);
        building("HOUSE", Kind.HOUSE, 11, 22, 3, 3, "A quiet house. The door is locked.", false, false);
        building("HOUSE", Kind.HOUSE, 18, 22, 3, 3, "A quiet house. The door is locked.", false, false);
        building("HOUSE", Kind.HOUSE, 22, 22, 3, 3, "A quiet house. The door is locked.", false, false);

        // ---- 7. Trees: 2-deep border + corner forests + groups, fixed layout ----
        for (int x = 0; x < MAP_W; x++) {
            for (int r = 0; r < 2; r++) {
                tree(x, r); // north band rows 0-1
                if ((x < 15 || x > 16) && r < 2) {
                    tree(x, MAP_H - 1 - r); // south band rows 30-31, gate stays open
                }
            }
        }
        for (int y = 2; y < MAP_H - 2; y++) {
            tree(0, y); tree(1, y); // west band
            tree(MAP_W - 1, y); tree(MAP_W - 2, y); // east band
        }
        // Corner forests (inside corners, off paths).
        for (int x = 22; x <= 29; x++) tree(x, 2);
        for (int y = 3; y <= 7; y++) tree(29, y);
        for (int x = 2; x <= 12; x++) tree(x, 2);
        for (int y = 3; y <= 7; y++) tree(2, y);
        for (int x = 2; x <= 6; x++) { tree(x, 24); tree(x, 25); tree(x, 28); tree(x, 29); }
        for (int x = 24; x <= 29; x++) { tree(x, 28); tree(x, 29); }
        // Small groups in open grass.
        tree(12, 10); tree(13, 10); tree(12, 11);
        tree(24, 19); tree(25, 19); tree(25, 20);
        tree(5, 20); tree(6, 20); tree(6, 21);

        // Fences (solid props).
        fences.add(new int[]{10, 20}); fences.add(new int[]{11, 20});
        fences.add(new int[]{29, 22}); fences.add(new int[]{29, 23});
        for (int[] f : fences) solid[f[0]][f[1]] = true;

        // NPCs near paths, never on entrances (solid cells).
        npcs.add(new Npc("MIRA", 14, 19, "Mira: \"The blue roof is the Inn.\"", VillagePalette.WATER));
        npcs.add(new Npc("Bram", 21, 12, "Bram: \"Fountain's cold. Shops are warm.\"", VillagePalette.ROOF_WEAPON));
        npcs.add(new Npc("ODD", 17, 12, "Odd: \"Stay on the road.\"", VillagePalette.ROOF_MAGIC));
        npcs.add(new Npc("Pip", 22, 28, "Pip: \"South lane goes everywhere.\"", VillagePalette.ROOF_ITEM));
        npcs.add(new Npc("Sol", 9, 7, "Sol: \"Knock twice at the Inn.\"", VillagePalette.ROOF_HOUSE));
        for (Npc n : npcs) solid[n.cx][n.cy] = true;

        // ---- 12. Validation (never remove) ----
        assertDoorsReachable();
        assertPathGap();
        assertDoorOnFacade();
        assertInsideMap();
        assertNoOverlap();
        if (buildings.size() != DOOR_COUNT) {
            throw new IllegalStateException("Need " + DOOR_COUNT + " doors, have " + buildings.size());
        }
    }

    /** Flood fill from spawn over walkable cells; every entrance must reach. */
    private void assertDoorsReachable() {
        boolean[][] seen = new boolean[MAP_W][MAP_H];
        int[] q = new int[MAP_W * MAP_H * 2];
        int head = 0, tail = 0;
        q[tail++] = SPAWN_C; q[tail++] = SPAWN_R;
        seen[SPAWN_C][SPAWN_R] = true;
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (head < tail) {
            int cx = q[head++];
            int cy = q[head++];
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (isInsideMap(nx, ny) && !seen[nx][ny] && !solid[nx][ny]) {
                    seen[nx][ny] = true;
                    q[tail++] = nx; q[tail++] = ny;
                }
            }
        }
        StringBuilder missing = new StringBuilder();
        for (Building b : buildings) {
            if (!seen[b.doorX][b.doorY] || !seen[b.enterX][b.enterY]) {
                missing.append(b.name).append('@').append(b.tx).append(',').append(b.ty).append(' ');
            }
        }
        if (missing.length() > 0) {
            throw new IllegalStateException("Unreachable doors: " + missing);
        }
    }

    /** No path cell touches a building except through its entrance cell. */
    private void assertPathGap() {
        StringBuilder bad = new StringBuilder();
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                Ground g = ground[x][y];
                if (g != Ground.PATH && g != Ground.ROAD && g != Ground.SQUARE && g != Ground.GATE) {
                    continue;
                }
                int[][] adj = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
                for (int[] a : adj) {
                    if (isInsideMap(a[0], a[1]) && isBuilding[a[0]][a[1]] && !isEntranceTouch(x, y)) {
                        bad.append('(').append(x).append(',').append(y).append(") ");
                        break;
                    }
                }
            }
        }
        if (bad.length() > 0) {
            throw new IllegalStateException("Path touches building: " + bad);
        }
    }

    private boolean isEntranceTouch(int x, int y) {
        for (Building b : buildings) {
            if (x == b.enterX && y == b.enterY) return true;
        }
        return false;
    }

    /** Every door is the bottom-center cell of its building. */
    private void assertDoorOnFacade() {
        for (Building b : buildings) {
            if (b.tw % 2 == 0 || (b.th != 3 && b.th != 4)) {
                throw new IllegalStateException("Bad size " + b.name + " " + b.tw + "x" + b.th);
            }
            if (b.doorX != b.tx + b.tw / 2 || b.doorY != b.ty + b.th - 1) {
                throw new IllegalStateException("Door off facade: " + b.name);
            }
            if (!isInsideMap(b.enterX, b.enterY) || solid[b.enterX][b.enterY]) {
                throw new IllegalStateException("Entrance blocked: " + b.name);
            }
        }
    }

    /** Everything placed lives inside the map on whole cells. */
    private void assertInsideMap() {
        for (Building b : buildings) {
            if (!isInsideMap(b.tx, b.ty) || !isInsideMap(b.tx + b.tw - 1, b.ty + b.th - 1)) {
                throw new IllegalStateException("Building outside map: " + b.name);
            }
        }
        for (int[] t : trees) {
            if (!isInsideMap(t[0], t[1])) throw new IllegalStateException("Tree outside map");
        }
        for (Npc n : npcs) {
            if (!isInsideMap(n.cx, n.cy)) throw new IllegalStateException("NPC outside map");
        }
    }

    /** No two buildings overlap; nothing sits on a path or entrance. */
    private void assertNoOverlap() {
        for (int i = 0; i < buildings.size(); i++) {
            Building a = buildings.get(i);
            for (int j = i + 1; j < buildings.size(); j++) {
                Building b = buildings.get(j);
                if (a.tx < b.tx + b.tw && b.tx < a.tx + a.tw
                        && a.ty < b.ty + b.th && b.ty < a.ty + a.th) {
                    throw new IllegalStateException("Overlap: " + a.name + " vs " + b.name);
                }
            }
        }
        for (int[] t : trees) assertNotOnPath(t[0], t[1], "tree");
        for (Npc n : npcs) {
            for (int[] t : trees) {
                if (n.cx == t[0] && n.cy == t[1]) {
                    throw new IllegalStateException("npc " + n.name + " on tree at " + n.cx + "," + n.cy);
                }
            }
        }
        for (int[] f : fences) assertNotOnPath(f[0], f[1], "fence");
        for (Npc n : npcs) assertNotOnPath(n.cx, n.cy, "npc " + n.name);
    }

    private void assertNotOnPath(int x, int y, String what) {
        Ground g = ground[x][y];
        if (g == Ground.PATH || g == Ground.ROAD || g == Ground.SQUARE || g == Ground.GATE) {
            throw new IllegalStateException(what + " on path at " + x + "," + y);
        }
        for (Building b : buildings) {
            if ((x == b.doorX && y == b.doorY) || (x == b.enterX && y == b.enterY)) {
                throw new IllegalStateException(what + " on door/entrance at " + x + "," + y);
            }
        }
    }

    // ---- 2. Grid movement + key-state input ----
    public void update(InputManager in) {
        boolean up = in.isHeld(KeyEvent.VK_W) || in.isHeld(KeyEvent.VK_UP);
        boolean down = in.isHeld(KeyEvent.VK_S) || in.isHeld(KeyEvent.VK_DOWN);
        boolean left = in.isHeld(KeyEvent.VK_A) || in.isHeld(KeyEvent.VK_LEFT);
        boolean right = in.isHeld(KeyEvent.VK_D) || in.isHeld(KeyEvent.VK_RIGHT);

        if (up && !prevUp) preferred = Dir.UP;
        else if (down && !prevDown) preferred = Dir.DOWN;
        else if (left && !prevLeft) preferred = Dir.LEFT;
        else if (right && !prevRight) preferred = Dir.RIGHT;
        prevUp = up; prevDown = down; prevLeft = left; prevRight = right;

        Dir want = null;
        if (preferred != null) {
            if ((preferred == Dir.UP && up) || (preferred == Dir.DOWN && down)
                    || (preferred == Dir.LEFT && left) || (preferred == Dir.RIGHT && right)) {
                want = preferred;
            }
        }
        if (want == null) {
            if (up) want = Dir.UP;
            else if (down) want = Dir.DOWN;
            else if (left) want = Dir.LEFT;
            else if (right) want = Dir.RIGHT;
        }
        if (want != null) preferred = want;

        if (!stepping) {
            if (want == null) {
                walkFrame = 0;
            } else if (want != facing) {
                facing = want; // turn first, move next tick
            } else {
                startStep(want);
            }
        } else {
            stepT++;
            double t = Math.min(1.0, stepT / (double) STEP_TICKS);
            renderCx = (fromC * TILE + TILE / 2.0) + (toC - fromC) * TILE * t;
            renderCy = (fromR * TILE + TILE / 2.0) + (toR - fromR) * TILE * t;
            animT++;
            if (animT >= ANIM_TICKS) {
                animT = 0;
                walkFrame = (walkFrame + 1) % 2;
            }
            if (stepT >= STEP_TICKS) {
                col = toC;
                row = toR;
                snapRender();
                stepping = false;
            }
        }

        if (in.drainTalkPressed()) interact();
        boolean f3 = in.isHeld(KeyEvent.VK_F3);
        if (f3 && !prevF3) debug = !debug;
        prevF3 = f3;

        if (msgTimer > 0 && --msgTimer == 0) message = "";
        fpsFrames++;
        long now = System.currentTimeMillis();
        if (now - fpsLast >= 1000) {
            fps = fpsFrames;
            fpsFrames = 0;
            fpsLast = now;
        }
    }

    private void startStep(Dir d) {
        int tc = col + d.dx;
        int tr = row + d.dy;
        if (!isInsideMap(tc, tr) || solid[tc][tr]) return; // blocked
        stepping = true;
        fromC = col; fromR = row;
        toC = tc; toR = tr;
        stepT = 0;
        animT = ANIM_TICKS;
        walkFrame = 1;
    }

    /** E works only on the entrance cell, facing up at the door. */
    private void interact() {
        for (Building b : buildings) {
            if (col == b.enterX && row == b.enterY && facing == Dir.UP
                    && col + facing.dx == b.doorX && row + facing.dy == b.doorY) {
                message = b.blurb;
                msgTimer = MSG_TICKS;
                return;
            }
        }
        int fx = col + facing.dx;
        int fy = row + facing.dy;
        for (Npc n : npcs) {
            if (fx == n.cx && fy == n.cy) {
                message = n.line;
                msgTimer = MSG_TICKS;
                return;
            }
        }
        message = "Stand on a doorstep facing the door, then press E.";
        msgTimer = MSG_TICKS / 2;
    }

    // ---- 3. Camera ----
    private void updateCamera(int viewW, int viewH) {
        int viewTilesW = viewW / (TILE * SCALE);
        int viewTilesH = viewH / (TILE * SCALE);
        if (MAP_W <= viewTilesW) camX = (WORLD_W - viewTilesW * TILE) / 2;
        else camX = (int) Math.round(clamp(renderCx - viewTilesW * TILE / 2.0, 0, WORLD_W - viewTilesW * TILE));
        if (MAP_H <= viewTilesH) camY = (WORLD_H - viewTilesH * TILE) / 2;
        else camY = (int) Math.round(clamp(renderCy - viewTilesH * TILE / 2.0, 0, WORLD_H - viewTilesH * TILE));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // ---- 10. Rendering ----
    public void draw(Graphics2D g2, int viewW, int viewH) {
        Object oldInterp = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        try {
            updateCamera(viewW, viewH);
            int offX = (viewW - VIEW_TX * TILE * SCALE) / 2;
            int offY = (viewH - VIEW_TY * TILE * SCALE) / 2;

            g2.setColor(GamePalette.ROYAL_DEEP);
            g2.fillRect(0, 0, viewW, viewH);

            int c0 = Math.max(0, camX / TILE);
            int r0 = Math.max(0, camY / TILE);
            int c1 = Math.min(MAP_W - 1, (camX + VIEW_TX * TILE) / TILE + 1);
            int r1 = Math.min(MAP_H - 1, (camY + VIEW_TY * TILE) / TILE + 1);

            // Ground + paths with darker edges (corner/T/cross fall out of neighbors).
            for (int r = r0; r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    paintGround(g2, offX, offY, c, r);
                }
            }
            for (int r = r0; r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    paintPathEdge(g2, offX, offY, c, r);
                }
            }

            // Fountain water + rim.
            paintFountain(g2, offX, offY);

            // Y-sorted: buildings, trees, fences, NPCs, player.
            java.util.TreeMap<Integer, java.util.List<Runnable>> order = new java.util.TreeMap<>();
            for (Building b : buildings) {
                int baseY = (b.ty + b.th) * TILE;
                order.computeIfAbsent(baseY * 4 + 1, k -> new java.util.ArrayList<>())
                        .add(() -> drawBuilding(g2, offX, offY, b));
            }
            for (int[] t : trees) {
                if (t[0] < c0 - 1 || t[0] > c1 + 1 || t[1] < r0 - 1 || t[1] > r1 + 1) continue;
                int baseY = (t[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawTree(g2, offX, offY, t[0], t[1]));
            }
            for (int[] f : fences) {
                int baseY = (f[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawFence(g2, offX, offY, f[0], f[1]));
            }
            for (Npc n : npcs) {
                int baseY = (n.cy + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 3, k -> new java.util.ArrayList<>())
                        .add(() -> drawNpc(g2, offX, offY, n));
            }
            int playerBase = (int) (renderCy + 6);
            order.computeIfAbsent(playerBase * 4 + 3, k -> new java.util.ArrayList<>())
                    .add(() -> drawPlayer(g2, offX, offY));
            for (java.util.List<Runnable> batch : order.values()) {
                for (Runnable d : batch) d.run();
            }

            if (debug) drawDebug(g2, offX, offY, viewW, viewH, c0, r0, c1, r1);
            drawHud(g2, viewW, viewH);
        } finally {
            if (oldInterp != null) g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
            if (oldAA != null) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }

    private void paintGround(Graphics2D g2, int offX, int offY, int c, int r) {
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        switch (ground[c][r]) {
            case GRASS -> {
                // Fixed two-tone checker + fixed dark patches (same every run).
                if ((c + r) % 2 == 0) g2.setColor(VillagePalette.GRASS_TONE2);
                else g2.setColor(VillagePalette.GRASS);
                g2.fillRect(x, y, s, s);
                if ((c * 7 + r * 13) % 29 == 0) {
                    g2.setColor(VillagePalette.GRASS_DARK);
                    g2.fillRect(x + s / 4, y + s / 4, s / 2, s / 2);
                }
            }
            case PATH -> g2.setColor(VillagePalette.PATH);
            case ROAD -> g2.setColor(VillagePalette.PATH);
            case SQUARE -> g2.setColor(VillagePalette.PLAZA);
            case TREE -> g2.setColor(VillagePalette.GRASS);
            case GATE -> g2.setColor(VillagePalette.PATH);
        }
        if (ground[c][r] != Ground.GRASS && ground[c][r] != Ground.TREE) {
            g2.fillRect(x, y, s, s);
        }
    }

    private boolean isPathKind(int c, int r) {
        if (!isInsideMap(c, r)) return false;
        Ground g = ground[c][r];
        return g == Ground.PATH || g == Ground.ROAD || g == Ground.SQUARE || g == Ground.GATE;
    }

    private void paintPathEdge(Graphics2D g2, int offX, int offY, int c, int r) {
        if (!isPathKind(c, r)) return;
        Color edge = ground[c][r] == Ground.SQUARE ? VillagePalette.PLAZA_EDGE : VillagePalette.PATH_EDGE;
        g2.setColor(edge);
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        int e = 2; // edge thickness in screen px
        if (!isPathKind(c, r - 1)) g2.fillRect(x, y, s, e);
        if (!isPathKind(c, r + 1)) g2.fillRect(x, y + s - e, s, e);
        if (!isPathKind(c - 1, r)) g2.fillRect(x, y, e, s);
        if (!isPathKind(c + 1, r)) g2.fillRect(x + s - e, y, e, s);
    }

    private void paintFountain(Graphics2D g2, int offX, int offY) {
        int s = TILE * SCALE;
        g2.setColor(VillagePalette.RIM);
        g2.fillRect(offX + (fountainX * TILE - camX) * SCALE,
                offY + (fountainY * TILE - camY) * SCALE, s * 2, s * 2);
        g2.setColor(VillagePalette.WATER);
        g2.fillRect(offX + (fountainX * TILE - camX) * SCALE + 3,
                offY + (fountainY * TILE - camY) * SCALE + 3, s * 2 - 6, s * 2 - 6);
    }

    private Color roofColor(Building b, boolean dark) {
        return switch (b.kind) {
            case INN -> dark ? VillagePalette.ROOF_INN_DARK : VillagePalette.ROOF_INN;
            case WEAPON -> dark ? VillagePalette.ROOF_WEAPON_DARK : VillagePalette.ROOF_WEAPON;
            case ARMOR -> dark ? VillagePalette.ROOF_ARMOR_DARK : VillagePalette.ROOF_ARMOR;
            case ITEM -> dark ? VillagePalette.ROOF_ITEM_DARK : VillagePalette.ROOF_ITEM;
            case MAGIC -> dark ? VillagePalette.ROOF_MAGIC_DARK : VillagePalette.ROOF_MAGIC;
            case HOUSE, BIG -> dark ? VillagePalette.ROOF_HOUSE_DARK : VillagePalette.ROOF_HOUSE;
        };
    }

    private void drawBuilding(Graphics2D g2, int offX, int offY, Building b) {
        int s = TILE * SCALE;
        int x = offX + (b.tx * TILE - camX) * SCALE;
        int y = offY + (b.ty * TILE - camY) * SCALE;
        int w = b.tw * s;
        int h = b.th * s;
        int roofRows = (b.kind == Kind.INN) ? 2 : 1;

        // Walls.
        g2.setColor(VillagePalette.WALL);
        g2.fillRect(x, y + roofRows * s, w, h - roofRows * s);
        // Soft darker line under the roof.
        g2.setColor(VillagePalette.WALL_SHADE);
        g2.fillRect(x, y + roofRows * s, w, 2);
        // Roof.
        g2.setColor(roofColor(b, false));
        g2.fillRect(x, y, w, roofRows * s - 2);
        g2.setColor(roofColor(b, true));
        g2.fillRect(x, y + roofRows * s - 2, w, 2);
        // 1px dark outline.
        g2.setColor(VillagePalette.OUTLINE_DARK);
        g2.drawRect(x, y, w - 1, h - 1);
        if (b.chimney) {
            g2.setColor(VillagePalette.WALL_SHADE);
            g2.fillRect(x + w - 8, y + 2, 5, s - 2);
        }
        // Door bottom-center + frame.
        int dw = 8 * SCALE / 2;
        int dx = offX + (b.doorX * TILE - camX) * SCALE + (s - dw) / 2;
        int dy = offY + (b.doorY * TILE - camY) * SCALE + (s - dw) / 2;
        g2.setColor(VillagePalette.DOOR_FRAME);
        g2.fillRect(dx - 1, dy - 1, dw + 2, dw + 2);
        g2.setColor(VillagePalette.DOOR);
        g2.fillRect(dx, dy, dw, dw);
        if (b.shop) { // small sign above shop doors
            g2.setColor(VillagePalette.WINDOW_LIT);
            g2.fillRect(dx, dy - 6, dw, 4);
            g2.setColor(VillagePalette.OUTLINE_DARK);
            g2.drawRect(dx, dy - 6, dw - 1, 3);
        }
        // Windows on the middle wall row only (never roof or door).
        drawWindows(g2, offX, offY, b);
        // Name label.
        g2.setColor(VillagePalette.WALL);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        int labelW = g2.getFontMetrics().stringWidth(b.name);
        g2.drawString(b.name, x + (w - labelW) / 2, y - 4);
    }

    private void drawWindows(Graphics2D g2, int offX, int offY, Building b) {
        int s = TILE * SCALE;
        int wallRow = b.ty + 1; // middle wall row for h=3; upper wall row for the inn
        if (b.kind == Kind.INN) wallRow = b.ty + 2;
        Color glass = b.shop ? VillagePalette.WINDOW_LIT : VillagePalette.WINDOW_DAY;
        int[] cols;
        if (b.tw == 3) {
            cols = new int[]{b.tx, b.tx + 2}; // left and right wall tiles
        } else {
            cols = new int[]{b.tx, b.tx + b.tw / 2, b.tx + b.tw - 1}; // mirrored + center
        }
        for (int c : cols) {
            int x = offX + (c * TILE - camX) * SCALE + s / 4;
            int y = offY + (wallRow * TILE - camY) * SCALE + s / 4;
            int w = s / 2;
            int h = s / 2 - 2;
            g2.setColor(VillagePalette.WINDOW_FRAME);
            g2.fillRect(x - 1, y - 1, w + 2, h + 2);
            g2.setColor(glass);
            g2.fillRect(x, y, w, h);
            g2.setColor(VillagePalette.WINDOW_FRAME); // 1px cross bar
            g2.fillRect(x, y + h / 2, w, 1);
        }
    }

    private void drawTree(Graphics2D g2, int offX, int offY, int c, int r) {
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.setColor(VillagePalette.TRUNK);
        g2.fillRect(x + s / 2 - 2, y + s / 2, 4, s / 2);
        g2.setColor(VillagePalette.CANOPY_DARK);
        g2.fillOval(x - 2, y - s / 2, s + 4, s);
        g2.setColor(VillagePalette.CANOPY_LIGHT);
        g2.fillOval(x + 1, y - s / 2 + 2, s - 4, s - 5);
    }

    private void drawFence(Graphics2D g2, int offX, int offY, int c, int r) {
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.setColor(VillagePalette.FENCE);
        g2.fillRect(x + 2, y + s / 2 - 1, s - 4, 3);
        g2.fillRect(x + 3, y + 4, 3, s - 8);
        g2.fillRect(x + s - 6, y + 4, 3, s - 8);
    }

    private void drawNpc(Graphics2D g2, int offX, int offY, Npc n) {
        int x = (int) (offX + (n.cx * TILE + TILE / 2.0 - camX) * SCALE);
        int y = (int) (offY + (n.cy * TILE + TILE / 2.0 - camY) * SCALE);
        int s = 5 * SCALE;
        g2.setColor(VillagePalette.SKIN);
        g2.fillOval(x - 3, y - s / 2 - 2, 6, 6);
        g2.setColor(n.clothes);
        g2.fillRect(x - 4, y - s / 2 + 4, 8, s / 2);
        g2.setColor(VillagePalette.OUTLINE_DARK);
        g2.drawRect(x - 4, y - s / 2 + 4, 7, s / 2 - 1);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g2.drawString(n.name, x - 10, y - s / 2 - 6);
    }

    /** 11. Four directions, two walk frames, idle when stopped. */
    private void drawPlayer(Graphics2D g2, int offX, int offY) {
        int x = (int) (offX + (renderCx - camX) * SCALE);
        int y = (int) (offY + (renderCy - camY) * SCALE);
        int s = 5 * SCALE;
        if (stepping) {
            g2.setColor(VillagePalette.OUTLINE_DARK);
            int swing = (walkFrame == 0) ? -2 : 2;
            g2.fillRect(x - 4, y + 2, 3, 4 + swing);
            g2.fillRect(x + 1, y + 2, 3, 4 - swing);
        }
        g2.setColor(VillagePalette.SKIN);
        g2.fillOval(x - 3, y - s / 2 - 2, 6, 6);
        g2.setColor(VillagePalette.PLAYER_TUNIC);
        g2.fillRect(x - 4, y - s / 2 + 4, 8, s / 2);
        g2.setColor(VillagePalette.OUTLINE_OFFWHITE);
        g2.drawRect(x - 4, y - s / 2 + 4, 7, s / 2 - 1);
        int nx = x, ny = y - 2;
        switch (facing) {
            case UP -> ny -= s / 2 + 1;
            case DOWN -> ny += s / 2 + 1;
            case LEFT -> nx -= s / 2 + 1;
            case RIGHT -> nx += s / 2 + 1;
        }
        g2.fillRect(nx - 1, ny - 1, 2, 2);
    }

    private void drawDebug(Graphics2D g2, int offX, int offY,
                           int viewW, int viewH, int c0, int r0, int c1, int r1) {
        int s = TILE * SCALE;
        g2.setColor(new Color(240, 230, 200, 60));
        for (int c = c0; c <= c1 + 1; c++) {
            int x = offX + (c * TILE - camX) * SCALE;
            g2.drawLine(x, 0, x, viewH);
        }
        for (int r = r0; r <= r1 + 1; r++) {
            int y = offY + (r * TILE - camY) * SCALE;
            g2.drawLine(0, y, viewW, y);
        }
        g2.setColor(new Color(255, 0, 0, 90));
        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                if (solid[c][r]) {
                    g2.fillRect(offX + (c * TILE - camX) * SCALE,
                            offY + (r * TILE - camY) * SCALE, s, s);
                }
            }
        }
        g2.setColor(VillagePalette.WINDOW_LIT);
        for (Building b : buildings) {
            g2.drawRect(offX + (b.doorX * TILE - camX) * SCALE,
                    offY + (b.doorY * TILE - camY) * SCALE, s - 1, s - 1);
        }
        g2.setColor(VillagePalette.CANOPY_LIGHT);
        for (Building b : buildings) {
            g2.drawRect(offX + (b.enterX * TILE - camX) * SCALE,
                    offY + (b.enterY * TILE - camY) * SCALE, s - 1, s - 1);
        }
        // Tile numbers on building cells.
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));
        for (Building b : buildings) {
            for (int y = b.ty; y < b.ty + b.th; y++) {
                for (int x = b.tx; x < b.tx + b.tw; x++) {
                    if (x < c0 || x > c1 || y < r0 || y > r1) continue;
                    g2.drawString(x + "," + y,
                            offX + (x * TILE - camX) * SCALE + 2,
                            offY + (y * TILE - camY) * SCALE + 9);
                }
            }
        }
        g2.setColor(VillagePalette.WALL);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g2.drawString("col=" + col + " row=" + row + " facing=" + facing, 8, 38);
        g2.drawString("cam=" + camX + "," + camY + " fps=" + fps, 8, 52);
    }

    private void drawHud(Graphics2D g2, int viewW, int viewH) {
        g2.setColor(GamePalette.ROYAL_BLUE);
        g2.fillRect(0, 0, viewW, 22);
        g2.setColor(GamePalette.PARCHMENT);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g2.drawString("CONERIA TEST - WASD/arrows move  E/ENTER talk  F3 debug  ESC menu", 8, 15);
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

    /** Prints the map as text, one char per tile, for the build summary. */
    public void printMap() {
        char[][] pic = new char[MAP_H][MAP_W];
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                pic[y][x] = switch (ground[x][y]) {
                    case GRASS -> '.';
                    case PATH -> 'p';
                    case ROAD -> 'r';
                    case SQUARE -> 's';
                    case TREE -> 'T';
                    case GATE -> 'G';
                };
            }
        }
        for (Building b : buildings) {
            char mark = switch (b.kind) {
                case INN -> 'I';
                case HOUSE -> 'H';
                case ITEM -> 'i';
                case WEAPON -> 'W';
                case ARMOR -> 'A';
                case MAGIC -> 'M';
                case BIG -> 'B';
            };
            for (int y = b.ty; y < b.ty + b.th; y++) {
                for (int x = b.tx; x < b.tx + b.tw; x++) {
                    pic[y][x] = mark;
                }
            }
            pic[b.doorY][b.doorX] = 'D';
            pic[b.enterY][b.enterX] = 'd';
        }
        for (int y = fountainY; y < fountainY + 2; y++) {
            for (int x = fountainX; x < fountainX + 2; x++) pic[y][x] = 'F';
        }
        for (Npc n : npcs) pic[n.cy][n.cx] = 'N';
        pic[SPAWN_R][SPAWN_C] = 'P';
        System.out.println("MAP 32x32 (col 0..31):");
        for (int y = 0; y < MAP_H; y++) {
            System.out.println(String.valueOf(pic[y]));
        }
        System.out.println("Legend: .=grass r=road p=path s=plaza G=gate T=tree F=fountain");
        System.out.println("I=inn H=house i=item W=weapon A=armor M=magic B=bighouse D=door d=entrance N=npc P=spawn");
        int treeCount = trees.size();
        System.out.println("Trees: " + treeCount + " Buildings: " + buildings.size() + " NPCs: " + npcs.size());
    }
}
