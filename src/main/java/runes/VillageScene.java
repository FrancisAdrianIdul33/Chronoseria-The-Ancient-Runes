package runes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * FINAL village walk test: Coneria-style 32x32 town.
 *
 * <p>Grid rules: TILE=16, 2x whole scale, cell layers (ground/solid),
 * one-cell steps with tween, doors on the facade bottom row with an entrance cell
 * in front (INN door off-center per spec), E works only from the entrance facing up,
 * F3 debug overlay, build-time validation that never gets removed.
 */
public final class VillageScene {
    // ---- 14. Constants in one place ----
    static final int TILE = 16;
    static final int MAP_W = 32;
    static final int MAP_H = 35;
    static final int WORLD_W = MAP_W * TILE;
    static final int WORLD_H = MAP_H * TILE;
    static final int VIEW_TX = 20;
    static final int VIEW_TY = 12;
    static final int SCALE = 2; // whole-number scale only
    static final int STEP_TICKS = 12; // ~0.2s per cell at 60 UPS
    static final int NPC_STEP_TICKS = 18; // villagers stroll slower than the player
    static final int NPC_BLOCK_DIST = 2; // Manhattan range that counts as "in the way"
    static final int NPC_FOLLOW_DIST = 4; // tailing range: same line behind counts as pressing
    static final int NPC_RESUME_DIST = 4; // player must be this far before resuming
    static final int ANIM_TICKS = 6; // walk clock, separate from movement
    static final int MSG_TICKS = 300;
    static final int SPAWN_C = 13;
    static final int SPAWN_R = 29;
    static final int DOOR_COUNT = 8;

    // Inn interior constants
    private static final int INN_INT_W = 12;
    private static final int INN_INT_H = 8;
    private static final int INN_DOOR_X = 5;
    private static final int INN_DOOR_Y = 7;
    private static final int INN_SPAWN_X = 5;
    private static final int INN_SPAWN_Y = 5;

    // Interior mode
    private boolean inInnInterior = false;
    private Npc innkeeper;
    private int[][] innWalls; // 12x8: 0=floor, 1=wall, 2=counter, 3=bed, 4=door
    private boolean[][] innSolid;

    // Fade transition (300 ms each way at 60 UPS)
    private static final int FADE_TICKS = 18;
    private int fadePhase; // 0 = idle, 1 = fading out, 2 = fading in
    private float fadeAlpha;
    private boolean pendingInn; // true = switch to the inn at full black
    private int pendingCol, pendingRow;
    private Dir pendingFacing;

    /**
     * Forest map (locked schematic dark-green cells, hand-verified): 159 trees
     * in clumps with grass gaps — northfield thickets, ridge bands behind the
     * north buildings, west ridge slivers, corner/dead-space fills, south
     * exterior woods. Single tile each: solid trunk, trunk Y-sorted, canopy
     * overhead. Same pattern every run (no RNG).
     */
    static final int[][] TREES = {
        // Northfield thickets (outside the top wall).
        {0,0},{1,0},{2,0},{9,0},{21,0},{22,0},{23,0},{27,0},{28,0},{29,0},
        {0,1},{1,1},{2,1},{3,1},{7,1},{11,1},{12,1},{17,1},{18,1},{19,1},{23,1},{24,1},{30,1},
        {3,2},{4,2},{5,2},{6,2},{19,2},{27,2},{30,2},{31,2},
        // West block behind TAVERN/MENTOR + east band (row 5).
        {1,5},{2,5},{3,5},{4,5},{5,5},{6,5},{13,5},{14,5},{22,5},{23,5},{24,5},{25,5},{26,5},{27,5},{28,5},{29,5},{30,5},
        // NW + NE forest bands (row 6).
        {1,6},{2,6},{3,6},{4,6},{5,6},{19,6},{20,6},{21,6},{22,6},{23,6},{24,6},{25,6},{26,6},{27,6},{28,6},{29,6},{30,6},
        // NW corner tails + NE corner block (row 7).
        {1,7},{2,7},{3,7},{26,7},{27,7},{28,7},{29,7},{30,7},
        // West ridge slivers + east singles (row 8).
        {1,8},{10,8},{16,8},{29,8},{30,8},
        // West ridge + mid gaps (row 9).
        {1,9},{10,9},{16,9},{17,9},{23,9},{24,9},{30,9},
        // Lane gap singles (rows 10-11).
        {18,10},{30,10},
        {18,11},
        // West dead-space fill behind HOUSE (row 13).
        {5,13},{6,13},{7,13},{8,13},{9,13},
        // West fill + mid clumps (row 14).
        {7,14},{8,14},{9,14},{10,14},{20,14},{21,14},{26,14},
        // Mid run clump (row 15).
        {19,15},{20,15},{21,15},
        // Lane-side single (row 17).
        {9,17},
        // East singles (row 18).
        {22,18},{29,18},
        // South-lane gaps (row 19).
        {12,19},{25,19},{29,19},
        // Corridor mouth single (row 20).
        {1,20},
        // West + corridor/inner singles (row 21).
        {1,21},{11,21},{12,21},
        // West + east lane frame + south band (row 22).
        {1,22},{2,22},{19,22},{25,22},{26,22},{27,22},{28,22},{29,22},{30,22},
        // South-interior pair + stem-east pair (row 25).
        {2,25},{3,25},{11,25},{26,25},{27,25},
        // SW exterior block + inner singles (row 26).
        {3,26},{4,26},{5,26},{6,26},{11,26},{16,26},{20,26},{26,26},{31,26},
        // Corridor-east singles (row 27).
        {20,27},{28,27},
        // SW corner + gate-approach frame (row 28).
        {0,28},{1,28},{8,28},{10,28},{19,28},{20,28},{31,28},
        // West lane-side + inn-east + gate column gap (row 29).
        {1,29},{2,29},{5,29},{10,29},{11,29},{12,29},{20,29},
        // SW grass + far-east corner (row 30).
        {5,30},{6,30},{31,30},
        // South edge singles (row 31).
        {2,31},{23,31},{24,31},{26,31},
        // South exterior extension (rows 32-34), scattered, clearing the gate corridor.
        {8,32},{10,33},{22,32},{24,33},{6,34},{26,34},
    };

    /** Ground layer tile kinds. */
    enum Ground { GRASS, PATH, ROAD, SQUARE, TREE, GATE, WATER }

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
        final int doorX, doorY; // facade cell, walkable (INN off-center per spec)
        final int enterX, enterY; // walkable cell directly in front
        final String blurb;
        final boolean shop; // lit windows + sign (fallback only)
        final boolean chimney;
        final boolean locked; // bump never opens it (HOUSE)
        int upperA = 2; // window count, first upper row (floor 2)
        int upperB = 2; // window count, second upper row (INN 4-tall only)
        int baseC = 1; // window count on ground/base row (door cell excluded)
        Building(String name, Kind kind, int tx, int ty, int tw, int th, String blurb,
                 boolean shop, boolean chimney, boolean locked, int doorDX) {
            this.name = name; this.kind = kind;
            this.tx = tx; this.ty = ty; this.tw = tw; this.th = th;
            this.doorX = tx + doorDX;
            this.doorY = ty + th - 1; // always the bottom row
            this.enterX = doorX;
            this.enterY = doorY + 1;
            this.blurb = blurb;
            this.shop = shop;
            this.chimney = chimney;
            this.locked = locked;
        }
    }

    /** Villager mind: ordinary routine, yields only when blocking the player. */
    enum NpcState { IDLE, WANDER, TALKING, MOVE_ASIDE, BACK_UP, PASS_THROUGH }

    static final class Npc {
        final String name;
        final String[] lines; // dialogue stages, cycles 0..n
        final Color clothes;
        final boolean mind; // true = wanders/yields (pilot), false = stands still
        final int[][] zone; // home territory haunts; keeps villagers scattered
        int col, row; // grid cell (spawn = build position)
        Dir facing = Dir.DOWN;
        NpcState state = NpcState.IDLE;
        boolean stepping;
        int fromC, fromR, toC, toR, stepT;
        double renderCx, renderCy;
        int idleT; // ticks left before choosing a wander target
        int asideCd; // cooldown after yielding, stops jitter
        int avoidCd; // minute gate: one dodge per 3600 ticks max
        int pushT; // sustained-pressure accumulator; break-away at threshold
        boolean breaking; // committed escape flight, don't reroute mid-way
        int stage; // dialogue stage
        java.util.ArrayDeque<int[]> path; // BFS steps to wander target
        java.util.ArrayDeque<int[]> trail = new java.util.ArrayDeque<>(); // recent cells
        int safeC, safeR; // last wide cell (a free side neighbor exists)
        Npc(String name, int cx, int cy, Color clothes, boolean mind, int[][] zone, String... lines) {
            this.name = name; this.col = cx; this.row = cy;
            this.safeC = cx; this.safeR = cy;
            this.clothes = clothes; this.mind = mind; this.zone = zone; this.lines = lines;
            snap();
        }
        void snap() {
            renderCx = col * TILE + TILE / 2.0;
            renderCy = row * TILE + TILE / 2.0;
        }
    }

    // ---- Layers ----
    private final Ground[][] ground = new Ground[MAP_W][MAP_H];
    private final boolean[][] solid = new boolean[MAP_W][MAP_H];
    private final boolean[][] isBuilding = new boolean[MAP_W][MAP_H];
    private final java.util.List<Building> buildings = new java.util.ArrayList<>();
    private final java.util.List<Npc> npcs = new java.util.ArrayList<>();
    private final java.util.List<int[]> trees = new java.util.ArrayList<>();
    private final java.util.List<int[]> treeUppers = new java.util.ArrayList<>();    private final java.util.List<int[]> fences = new java.util.ArrayList<>();
    private final java.util.List<int[]> wallTops = new java.util.ArrayList<>();
    private final java.util.List<int[]> wallBodies = new java.util.ArrayList<>();
    private final java.util.List<int[]> invisibleBounds = new java.util.ArrayList<>();
    private final java.util.List<int[]> bridges = new java.util.ArrayList<>();
    private final java.util.List<int[]> wells = new java.util.ArrayList<>();
    private final java.util.List<int[]> bushes = new java.util.ArrayList<>();    private final java.util.List<int[]> fountains = new java.util.ArrayList<>();

    // ---- Tile sprites (32x32 source art, drawn 1:1 to a scaled cell) ----
    private BufferedImage grassTile;
    private BufferedImage wallBrickTile;
    private BufferedImage wallTopTile;
    private BufferedImage treeBaseTile;
    private BufferedImage treeUpperTile;
    private BufferedImage fenceTile;
    private BufferedImage wellTile;
    private BufferedImage bushTile;
    private BufferedImage waterTile;
    private BufferedImage pathTile;
    private BufferedImage houseRoof;
    private BufferedImage houseWallWindow;
    private BufferedImage houseWallPlain;
    private BufferedImage houseBaseWindow;
    private BufferedImage houseBasePlain;
    private BufferedImage houseDoorClosed;
    private BufferedImage houseDoorOpen;
    private BufferedImage sewerTile;
    private BufferedImage fountainBase;
    private BufferedImage fountainUpper;
    private final java.util.List<int[]> sewers = new java.util.ArrayList<>();
    private final java.util.List<int[]> fountainUppers = new java.util.ArrayList<>();
    private boolean spritesTried;

    // ---- Dynamic wall mask: designer intent, stamped to T/W by neighbor rule ----
    private final boolean[][] wallMask = new boolean[MAP_W][MAP_H];

    // ---- Player (grid based) ----
    private int col, row;
    private Dir facing = Dir.UP;
    private boolean stepping;
    private int fromC, fromR, toC, toR, stepT;
    private double renderCx, renderCy;
    private int animT;
    private int walkFrame;

    private String message = "";

    // ---- Doors: 2-tap entry (tap 1 opens, tap 2 steps through) ----
    /** The one open doorway, or null when all doors are shut. */
    private Building openDoor;
    /**
     * Click latch: the key press that swung the door open must be released
     * before anyone steps through — holding never auto-enters. Cleared once
     * no movement key is held.
     */
    private boolean doorLatch;
    private int msgTimer;
    private Npc talkingNpc; // villager currently in dialogue, or null
    private final Dialogue dialogue = new Dialogue(); // panel module
    private int stuckT; // ticks the player pushes into a block with no progress
    private int lastPc = -1, lastPr = -1;
    private final java.util.Random rng = new java.util.Random(7); // fixed routine seed

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
        inInnInterior = false;
        if (innkeeper != null) {
            npcs.remove(innkeeper);
            if (talkingNpc == innkeeper) talkingNpc = null;
        }
        fadePhase = 0;
        fadeAlpha = 0f;
        col = SPAWN_C;
        row = SPAWN_R;
        facing = Dir.UP;
        stepping = false;
        if (openDoor != null) shutDoor(openDoor); // spawn is never a door cell
        doorLatch = false;
        snapRender();
        animT = 0;
        walkFrame = 0;
        message = "North past the fountain — the Inn has a red roof.";
        msgTimer = MSG_TICKS;
    }

    private void snapRender() {
        renderCx = col * TILE + TILE / 2.0;
        renderCy = row * TILE + TILE / 2.0;
    }

    /**
     * Scene change: fade to ROYAL_DEEP black (300 ms), swap mode/spawn while
     * fully covered, then fade back in. Input stays frozen until phase 0.
     */
    private void beginTransition(boolean toInn, int pc, int pr, Dir pf) {
        if (fadePhase != 0) return;
        pendingInn = toInn;
        pendingCol = pc;
        pendingRow = pr;
        pendingFacing = pf;
        fadePhase = 1;
        fadeAlpha = 0f;
        stepping = false;
        walkFrame = 0;
        animT = 0;
        doorLatch = false;
        dialogue.close();
        talkingNpc = null;
        message = "";
        msgTimer = 0;
    }

    private void updateTransition() {
        if (fadePhase == 1) {
            fadeAlpha += 1f / FADE_TICKS;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                applyPending();
                fadePhase = 2;
            }
        } else { // phase 2: fade back in
            fadeAlpha -= 1f / FADE_TICKS;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                fadePhase = 0;
            }
        }
    }

    /** Full black: swap the mode, respawn, put doors/villagers back in place. */
    private void applyPending() {
        if (pendingInn) {
            if (innkeeper == null) buildInnInterior();
            if (!npcs.contains(innkeeper)) npcs.add(innkeeper);
            inInnInterior = true; // (re)enter even when the layout is already built
            // Stepping through shuts the door behind you.
            if (openDoor != null) shutDoor(openDoor);
            col = pendingCol;
            row = pendingRow;
            facing = pendingFacing;
        } else {
            inInnInterior = false;
            if (innkeeper != null) {
                npcs.remove(innkeeper);
                if (talkingNpc == innkeeper) talkingNpc = null;
            }
            // Stepping through shuts the door behind you.
            if (openDoor != null) shutDoor(openDoor);
            col = pendingCol;
            row = pendingRow;
            facing = pendingFacing;
        }
        stepping = false;
        walkFrame = 0;
        animT = 0;
        preferred = null;
        snapRender();
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

    /** Wall cap: solid, blocks movement. Serves as the top of the wall. */
    private void wallTop(int c, int r) {
        wallTops.add(new int[]{c, r});
        solid[c][r] = true;
        // Keep GRASS underneath so the grass tile still shows at seams.
    }

    /** Wall body: front face of the top wall, solid — blocks movement. */
    private void wallBrick(int c, int r) {
        wallBodies.add(new int[]{c, r});
        solid[c][r] = true;
    }

    /** Sewer grate: replaces a wall brick where the river passes under, solid. */
    private void sewer(int c, int r) {
        wallBodies.removeIf(a -> a[0] == c && a[1] == r);
        sewers.add(new int[]{c, r});
        solid[c][r] = true;
    }

    /** Invisible map edge: solid, no texture. Keeps player inside without visuals. */
    private void invisibleBound(int c, int r) {
        invisibleBounds.add(new int[]{c, r});
        solid[c][r] = true;
    }

    // ---- 7a. Top band: full-width rows 3-4 ----
    // Designers add footprint cells to wallMask; stampWallsDynamic() resolves T/W.
    private void wall(int c, int r) {
        if (isInsideMap(c, r)) wallMask[c][r] = true;
    }

    private void wallRect(int x0, int y0, int w, int h) {
        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) wall(x, y);
        }
    }

    private void buildTopWall() {
        wallRect(0, 3, MAP_W, 2);
    }

    // ---- 7b. Side columns down to the bottom band ----
    private void buildSideCaps() {
        for (int y = 5; y <= 24; y++) {
            wall(0, y);
            wall(MAP_W - 1, y);
        }
    }

    // ---- 7c. Bottom band rows 23-24, opening cols 10-20 (B8 + lanes) ----
    private void buildBottomBand() {
        wallRect(0, 23, 10, 2); // cols 0-9 (col 0 already masked by the side)
        wallRect(21, 23, 11, 2); // cols 21-31 (col 31 already masked by the side)
    }

    // ---- 7d. Stem corridor walls col 9 / col 21, rows 25-29 ----
    private void buildStemWalls() {
        for (int y = 25; y <= 31; y++) {
            wall(9, y);
            wall(21, y);
        }
    }

    // ---- 7e. Front wall rows 30-31 cols 9-21, 1-wide gate at x15 ----
    private void buildFrontWall() {
        // Corners come from the stem mask; middle skips the gate.
        for (int x = 10; x <= 20; x++) {
            if (x != 15) {
                wall(x, 30);
                wall(x, 31);
            }
        }
    }

    /**
     * Dynamic T-wall stamp: every mask cell becomes a cap (T) when the cell south
     * of it is also wall, otherwise a south-facing face (W). This yields T-over-W
     * stacking, cap-to-cap corners and T-junctions, and face-wrapped outer corners
     * with no per-cell hand placement.
     */
    private void stampWallsDynamic() {
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                if (!wallMask[x][y]) continue;
                boolean southIsWall = isInsideMap(x, y + 1) && wallMask[x][y + 1];
                if (southIsWall) wallTop(x, y);
                else wallBrick(x, y);
            }
        }
    }

    /** Lazy-load 32x32 tile art from classpath; missing art fails fast (no procedural fallback). */
    private void ensureSprites() {
        if (spritesTried) return;
        spritesTried = true;
        grassTile = requireTile("/assets/tiles/grass.png");
        wallBrickTile = requireTile("/assets/tiles/wall_brick.png");
        wallTopTile = requireTile("/assets/tiles/wall_top.png");
        treeBaseTile = requireTile("/assets/tiles/tree_base.png");
        treeUpperTile = requireTile("/assets/tiles/tree_upper.png");
        fenceTile = requireTile("/assets/tiles/fence.png");
        wellTile = requireTile("/assets/tiles/well.png");
        bushTile = requireTile("/assets/tiles/bush.png");
        waterTile = requireTile("/assets/tiles/water.png");
        pathTile = requireTile("/assets/tiles/path.png");
        houseRoof = requireTile("/assets/tiles/house_roof.png");
        houseWallWindow = requireTile("/assets/tiles/house_wall_window.png");
        houseWallPlain = requireTile("/assets/tiles/house_wall_plain.png");
        houseBaseWindow = requireTile("/assets/tiles/house_wall_base_window.png");
        houseBasePlain = requireTile("/assets/tiles/house_wall_base_plain.png");
        houseDoorClosed = requireTile("/assets/tiles/house_door_closed.png");
        houseDoorOpen = requireTile("/assets/tiles/house_door_open.png");
        sewerTile = requireTile("/assets/tiles/sewer.png");
        fountainBase = requireTile("/assets/tiles/fountain_base.png");
        fountainUpper = requireTile("/assets/tiles/fountain_upper.png");
    }

    private BufferedImage requireTile(String resourcePath) {
        BufferedImage img = loadTile(resourcePath);
        if (img == null) throw new IllegalStateException("Missing tile art: " + resourcePath);
        return img;
    }

    /** River water with a bridge deck override (walkable crossing). */
    private void water(int c, int r) {
        ground[c][r] = Ground.WATER;
        solid[c][r] = true;
    }

    private void bridge(int c, int r) {
        bridges.add(new int[]{c, r});
        solid[c][r] = false; // overrides the river's impassable state
    }

    /** Stone well: 1x1 solid prop drawn with a dark mouth + rim. */
    private void well(int c, int r) {
        wells.add(new int[]{c, r});
        solid[c][r] = true;
        // Keep GRASS underneath so the grass tile still shows at seams.
    }

    /** Fence: 1x1 solid blocker. */
    private void fence(int c, int r) {
        fences.add(new int[]{c, r});
        solid[c][r] = true;
    }

    /** Bush: 1x1 solid grass prop. */
    private void bush(int c, int r) {
        bushes.add(new int[]{c, r});
        solid[c][r] = true;
    }

    private BufferedImage loadTile(String resourcePath) {
        try {
            InputStream in = AssetLoader.open(resourcePath);
            try (in) {
                return ImageIO.read(in);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void building(String name, Kind kind, int tx, int ty, int tw, int th,
                          String blurb, boolean shop, boolean chimney, boolean locked, int doorDX,
                          int upperA, int upperB, int baseC) {
        Building b = new Building(name, kind, tx, ty, tw, th, blurb, shop, chimney, locked, doorDX);
        b.upperA = Math.max(1, Math.min(upperA, tw));
        b.upperB = Math.max(1, Math.min(upperB, tw));
        // Base row excludes the door cell, so cap at tw-1.
        b.baseC = Math.max(1, Math.min(baseC, tw - 1));
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

    /** Centered window pick: columns nearest the middle first (symmetric facades). */
    private boolean upperHasWindow(Building b, int upperRow, int lx) {
        int want = (upperRow == 0) ? b.upperA : b.upperB;
        return centeredPick(b.tw, want, lx, -1);
    }

    private boolean baseHasWindow(Building b, int lx) {
        int doorLx = b.doorX - b.tx;
        return centeredPick(b.tw, b.baseC, lx, doorLx);
    }

    /** Pick `want` columns of width `w`, nearest-center first; skip `skip` (-1 = none). */
    private static boolean centeredPick(int w, int want, int lx, int skip) {
        if (lx == skip) return false;
        int order = 0;
        int selfRank = Integer.MAX_VALUE;
        // Rank by distance to center, ties prefer left of center for determinism.
        double center = (w - 1) / 2.0;
        java.util.TreeMap<Double, java.util.ArrayList<Integer>> byDist = new java.util.TreeMap<>();
        for (int i = 0; i < w; i++) {
            if (i == skip) continue;
            double d = Math.abs(i - center) + (i < center ? -0.001 * i : 0.001 * i);
            byDist.computeIfAbsent(d, k -> new java.util.ArrayList<>()).add(i);
        }
        for (java.util.ArrayList<Integer> grp : byDist.values()) {
            for (int i : grp) {
                if (i == lx) selfRank = order;
                order++;
            }
        }
        return selfRank < want;
    }

    private void buildMap() {
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                ground[x][y] = Ground.GRASS;
            }
        }

        // ---- 4. Paths: decoded loop network from new-village-map.png ----
        // Cobble everywhere; bridges (walkable) where paths cross the river.
        fillGround(4, 10, 14, 1, Ground.PATH);  // north lane row 10 cols 4-17
        fillGround(4, 11, 1, 10, Ground.PATH);  // west loop col 4 rows 11-20
        fillGround(17, 11, 1, 12, Ground.PATH); // middle loop col 17 rows 11-22
        fillGround(18, 12, 13, 1, Ground.PATH); // east lane row 12 cols 18-30
        fillGround(30, 13, 1, 8, Ground.PATH);  // east loop col 30 rows 13-20
        fillGround(5, 20, 12, 1, Ground.PATH);  // south lane row 20 cols 5-16
        fillGround(18, 20, 12, 1, Ground.PATH); // south lane row 20 cols 18-29
        fillGround(10, 21, 1, 7, Ground.PATH);  // corridor west col 10 rows 21-27
        fillGround(15, 21, 1, 9, Ground.PATH);  // gate column col 15 rows 21-29
        fillGround(16, 22, 1, 1, Ground.PATH);  // path nub (16,22)
        fillGround(11, 27, 4, 1, Ground.PATH);  // inn frontage row 27 cols 11-14
        ground[15][30] = Ground.GATE; // gate floor, 1-wide at x15, rows 30-31 walkable
        ground[15][31] = Ground.GATE;

        // ---- River: north edge entry, mid run, east moat, SE off-map exit ----
        for (int r = 0; r <= 2; r++) water(15, r);
        for (int r = 5; r <= 9; r++) water(15, r);
        water(15, 11);
        for (int c = 1; c <= 3; c++) water(c, 12);
        for (int c = 5; c <= 15; c++) water(c, 12);
        water(15, 13); water(15, 14); water(15, 15); water(15, 16); water(16, 16);
        for (int c = 18; c <= 23; c++) water(c, 16);
        water(23, 17); water(23, 18); water(23, 19);
        water(23, 21); water(23, 22);
        for (int r = 25; r <= 29; r++) water(23, r);
        for (int c = 24; c <= 29; c++) water(c, 29);
        water(29, 30); water(29, 31);
        // Extend river into the south exterior rows.
        water(29, 32); water(29, 33); water(29, 34);
        // Bridges: cobble path over water, walkable.
        bridge(15, 10); bridge(4, 12); bridge(17, 16); bridge(23, 20);

        // Fountain plaza: 3x3 cobble (cols 15-17, rows 20-22), walkable except
        // center (16,21) where the solid base sits. Upper art floats above at
        // (16,20), walkable, drawn overhead so the player goes behind it.
        // Layout:
        //   ccc
        //   cfc  (upper f = fountain_upper, no collision)
        //   cfc  (lower f = fountain_base, solid)
        //   ccc  (center 5 of the 3x3 is the solid base)
        // Read as 3x3 cobble with a 2-high fountain column centered on it.
        fillGround(15, 20, 3, 3, Ground.PATH);
        fountains.add(new int[]{16, 21});
        solid[16][21] = true;
        fountainUppers.add(new int[]{16, 20});
        // Upper stays walkable (no solid) so the player can step under it.

        // Forest map: schematic dark-green cells, single tile each (solid trunk,
        // trunk Y-sorted, canopy overhead). Fixed pattern, same every run.
        for (int[] t : TREES) tree(t[0], t[1]);
        // Upper canopy is a separate cell directly above the base. When that cell
        // is itself a tree base (vertical adjacency), the canopy overlays it.
        for (int[] t : TREES) {
            int ux = t[0], uy = t[1] - 1;
            if (!isInsideMap(ux, uy)) continue;
            boolean dup = false;
            for (int[] u : treeUppers) if (u[0] == ux && u[1] == uy) { dup = true; break; }
            if (!dup) treeUppers.add(new int[]{ux, uy});
        }
        // ---- 5. Buildings: 8 doors, 7 canonical + 1 house, all 3x4 ----
        // Single straight roof row + 2 floors (upper window/plain, ground base + door).
        building("TAVERN", Kind.HOUSE, 7, 6, 3, 4, "Tavern — rumors and drinks over the counter.", true, false, false, 1, 3, 2, 2);
        building("MENTOR", Kind.HOUSE, 12, 6, 3, 4, "Mentor's house — train field skills here.", false, false, false, 1, 2, 1, 1);
        building("BLACKSMITH", Kind.WEAPON, 19, 8, 3, 4, "Blacksmith — blades over the door.", true, false, false, 1, 3, 2, 2);
        building("MARKET", Kind.ITEM, 25, 8, 3, 4, "Market — potions and supplies.", true, false, false, 1, 1, 2, 1);
        building("HOUSE", Kind.HOUSE, 1, 13, 3, 4, "A quiet house. The door is locked.", false, false, true, 1, 2, 3, 1);
        building("WIZARD", Kind.MAGIC, 6, 16, 3, 4, "Wizard's domain — it hums.", true, false, false, 1, 1, 2, 2);
        building("SANCTUARY", Kind.HOUSE, 26, 16, 3, 4, "Sanctuary — the rite revives the fallen.", false, false, false, 1, 2, 1, 1);
        building("INN", Kind.INN, 12, 23, 3, 4, "Inn — rest up for the road ahead.", true, false, false, 1, 3, 2, 2);

        // ---- 7. Outer-rectangle perimeter walls (structured + dynamic stamp) ----
        // Top band full width, side columns to the bottom band, south stem corridor
        // to a stem-width front. Grass covers the whole map first, inside + outside.
        buildTopWall();
        buildSideCaps();
        buildBottomBand();
        buildStemWalls();
        buildFrontWall();
        stampWallsDynamic();
        // River passes under the top band at x15: brick becomes a sewer grate.
        // Pattern around (15,3-4): ggwgg / ttttt / ttstt / ggwgg (s = sewer).
        sewer(15, 4);
        // River passes under the bottom band at x23: same pattern at (23,23-24).
        sewer(23, 24);
        // Fences (orange cells) and wells (dark-grey cells) from the schematic.
        fence(4, 21); fence(5, 21); fence(6, 21);
        fence(7, 11); fence(8, 11);
        fence(12, 11); fence(13, 11);
        fence(14, 19); fence(15, 19); fence(16, 19);
        fence(16, 28); fence(17, 25); fence(17, 28);
        fence(22, 11); fence(23, 11);
        fence(26, 21); fence(27, 21);
        well(2, 8);                // Bushes: clustered clumps (outside + inside), varied shapes.
        bush(3, 0); bush(4, 0);
        bush(10, 0); bush(10, 1);
        bush(24, 0); bush(25, 0);
        bush(6, 6); bush(6, 7);
        bush(18, 13); bush(18, 14);
        bush(24, 15); bush(23, 15); bush(24, 16);
        bush(7, 26); bush(8, 26); bush(7, 25);
        bush(20, 23); bush(19, 23); bush(20, 24);
        // South exterior extension bushes, scattered, clearing the gate corridor.
        bush(9, 32); bush(21, 33); bush(7, 33); bush(25, 32); bush(5, 34); bush(27, 34);
        // Make the entire south exterior extension solid (collision) so the player
        // cannot pass; transition to other areas is deferred.
        for (int y = 32; y <= 34; y++) {
            for (int x = 0; x < MAP_W; x++) {
                invisibleBound(x, y);
            }
        }
        // NPCs rehomed to the new regions, each with a home zone.
        npcs.add(new Npc("Amara", 13, 18, VillagePalette.WATER, true,
                new int[][]{{13, 18}, {18, 18}, {13, 22}, {19, 22}},
                "Amara: \"Good day! The village is peaceful today.\"",
                "Amara: \"The inn is south, past the corridor.\"",
                "Amara: \"It is nice to see new faces around here.\""));
        npcs.add(new Npc("Darius", 22, 7, VillagePalette.ROOF_WEAPON, true,
                new int[][]{{22, 7}, {18, 7}, {28, 7}, {22, 9}},
                "Darius: \"Smithy's west, market's east.\"",
                "Darius: \"Shops line the north ridge. Mind the river.\"",
                "Darius: \"Kaelira says stay on the path. She's right.\""));
        npcs.add(new Npc("Kaelira", 16, 13, VillagePalette.ROOF_MAGIC, true,
                new int[][]{{16, 13}, {16, 14}, {12, 14}, {21, 14}},
                "Kaelira: \"Stay on the path.\"",
                "Kaelira: \"Bridges are one wide. Single file.\"",
                "Kaelira: \"Walls are solid. Don't bother pushing.\""));
        npcs.add(new Npc("Theron", 13, 28, VillagePalette.ROOF_ITEM, true,
                new int[][]{{13, 28}, {18, 28}, {13, 29}, {18, 29}},
                "Theron: \"South gate goes everywhere.\"",
                "Theron: \"I saw the fountain glittering yesterday.\"",
                "Theron: \"Inn's north, past the corridor.\""));
        npcs.add(new Npc("Elowen", 5, 7, VillagePalette.ROOF_HOUSE, true,
                new int[][]{{5, 7}, {3, 6}, {10, 7}, {5, 9}},
                "Elowen: \"Tavern's east, down the lane.\"",
                "Elowen: \"Mind the river north.\"",
                "Elowen: \"Quiet today. Good.\""));
        for (Npc n : npcs) {
            n.idleT = 120 + rng.nextInt(180);
            solid[n.col][n.row] = true;
        }

        // ---- 12. Validation (never remove) ----
        assertDoorsReachable();
        assertStreetFrontage();
        assertDoorOnFacade();
        assertInsideMap();
        assertNoOverlap();
        assertFountainPlaza();
        assertTreePlacement();
        assertTreeConnectivity();
        if (buildings.size() != DOOR_COUNT) {
            throw new IllegalStateException("Need " + DOOR_COUNT + " doors, have " + buildings.size());
        }
        // Doors start shut: stamp runtime collision after validation (flood
        // fill needs them walkable), then prove the shut invariant.
        for (Building b : buildings) solid[b.doorX][b.doorY] = true;
        assertDoorsShut();
    }

    /** Build the Inn interior map (12x8) - called when entering the Inn */
    private void buildInnInterior() {
        innWalls = new int[INN_INT_W][INN_INT_H];
        innSolid = new boolean[INN_INT_W][INN_INT_H];

        // Perimeter: north wall row 0, south wall row 7 (door gap at col 5), side walls.
        for (int x = 0; x < INN_INT_W; x++) {
            innWalls[x][0] = 1;
            innWalls[x][7] = (x == INN_DOOR_X) ? 4 : 1;
        }
        for (int y = 1; y < INN_INT_H - 1; y++) {
            innWalls[0][y] = 1;
            innWalls[INN_INT_W - 1][y] = 1;
        }

        // Counter across the north side (row 2, cols 3-8): solid, white top band.
        for (int x = 3; x <= 8; x++) innWalls[x][2] = 2;

        // Two red beds against the side walls.
        innWalls[1][3] = 3;
        innWalls[1][4] = 3;
        innWalls[9][3] = 3;
        innWalls[9][4] = 3;

        // Solidity from the layout (the door cell stays walkable).
        for (int y = 0; y < INN_INT_H; y++) {
            for (int x = 0; x < INN_INT_W; x++) {
                int w = innWalls[x][y];
                innSolid[x][y] = w == 1 || w == 2 || w == 3;
            }
        }

        // Innkeeper stands behind the counter (talk-only, never wanders).
        innkeeper = new Npc("Innkeeper", 5, 1, GamePalette.ARCANE_BLUE, false,
                new int[][]{{5, 1}},
                "Welcome to the Wandering Star Inn. Rest here for 10 gold?",
                "Rumors say the old well in the village hides a secret.",
                "The roads south have been dangerous lately. Stay safe.");
        innkeeper.facing = Dir.DOWN;
        innSolid[5][1] = true;

        inInnInterior = true;
    }

    /** Every door starts shut + solid with a walkable entrance; none open. */
    private void assertDoorsShut() {
        if (openDoor != null) {
            throw new IllegalStateException("a door starts open");
        }
        for (Building b : buildings) {
            if (!solid[b.doorX][b.doorY]) {
                throw new IllegalStateException("door starts open: " + b.name);
            }
            if (solid[b.enterX][b.enterY]) {
                throw new IllegalStateException("entrance blocked: " + b.name);
            }
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

    /**
     * Street frontage: paths may run flush against facades (urban street style),
     * but no path ground may cover a door cell and every entrance stays walkable
     * (checked in assertDoorOnFacade). Reachability is guarded by flood fill.
     */
    private void assertStreetFrontage() {
        for (Building b : buildings) {
            if (isPathKind(b.doorX, b.doorY)) {
                throw new IllegalStateException("Path on door: " + b.name);
            }
        }
    }

    /** Every door sits on the bottom facade row with an entrance south of it. */
    private void assertDoorOnFacade() {
        for (Building b : buildings) {
            if ((b.tw < 3 || b.tw > 5) || (b.th != 3 && b.th != 4)) {
                throw new IllegalStateException("Bad size " + b.name + " " + b.tw + "x" + b.th);
            }
            if (b.kind == Kind.INN && b.tw == 4) {
                // Legacy spec facade: 4x4 rrrr/wwww/wwww/wdww, door at tx+1 (off-center).
                if (b.th != 4 || b.doorX != b.tx + 1 || b.doorY != b.ty + b.th - 1) {
                    throw new IllegalStateException("Door off facade: " + b.name);
                }
            } else if (b.doorX != b.tx + b.tw / 2 || b.doorY != b.ty + b.th - 1) {
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
        for (int[] t : treeUppers) {
            if (!isInsideMap(t[0], t[1])) throw new IllegalStateException("Tree upper outside map");
        }
        for (int[] f : fences) {
            if (!isInsideMap(f[0], f[1])) throw new IllegalStateException("Fence outside map");
        }
        for (int[] w : wells) {
            if (!isInsideMap(w[0], w[1])) throw new IllegalStateException("Well outside map");
        }
        for (int[] b : bushes) {
            if (!isInsideMap(b[0], b[1])) throw new IllegalStateException("Bush outside map");
        }
        for (int[] w : wallTops) {
            if (!isInsideMap(w[0], w[1])) throw new IllegalStateException("Wall top outside map");
        }
        for (int[] w : wallBodies) {
            if (!isInsideMap(w[0], w[1])) throw new IllegalStateException("Wall brick outside map");
        }
        for (int[] w : invisibleBounds) {
            if (!isInsideMap(w[0], w[1])) throw new IllegalStateException("Bound outside map");
        }
        for (int[] f : fountains) {
            if (!isInsideMap(f[0], f[1])) throw new IllegalStateException("Fountain base outside map");
        }
        for (int[] f : fountainUppers) {
            if (!isInsideMap(f[0], f[1])) throw new IllegalStateException("Fountain upper outside map");
        }
        for (Npc n : npcs) {
            if (!isInsideMap(n.col, n.row)) throw new IllegalStateException("NPC outside map");
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
        // Top wall is solid FF1-style: never on a path/door. Invisible edges are also
        // solid but have no texture; they must not cover doors/entrances either.
        for (int[] w : wallTops) assertNotOnPath(w[0], w[1], "wallTop");
        for (int[] w : wallBodies) assertNotOnPath(w[0], w[1], "wallBrick");
        for (int[] w : sewers) assertNotOnPath(w[0], w[1], "sewer");
        for (int[] w : invisibleBounds) assertNotOnDoor(w[0], w[1], "bound");
        assertWallCollision();
        assertTopWallContinuous();
        for (Npc n : npcs) {
            for (int[] t : trees) {
                if (n.col == t[0] && n.row == t[1]) {
                    throw new IllegalStateException("npc " + n.name + " on tree at " + n.col + "," + n.row);
                }
            }
            for (int[] f : fountains) {
                if (n.col == f[0] && n.row == f[1]) {
                    throw new IllegalStateException("npc " + n.name + " on fountain at " + n.col + "," + n.row);
                }
            }
            for (int[] w : wallTops) {
                if (n.col == w[0] && n.row == w[1]) {
                    throw new IllegalStateException("npc " + n.name + " on wallTop at " + n.col + "," + n.row);
                }
            }
            for (int[] w : wallBodies) {
                if (n.col == w[0] && n.row == w[1]) {
                    throw new IllegalStateException("npc " + n.name + " on wallBrick at " + n.col + "," + n.row);
                }
            }
        }
        for (int[] f : fences) assertNotOnPath(f[0], f[1], "fence");
        for (int[] w : wells) assertNotOnPath(w[0], w[1], "well");
        for (int[] b : bushes) assertNotOnPath(b[0], b[1], "bush");
        for (Npc n : npcs) assertNotOnPath(n.col, n.row, "npc " + n.name);
        assertContainedInWalls();
    }

    /** Every structure must sit inside the walls (on walkable ground, never on a wall). */
    private void assertContainedInWalls() {
        for (Building b : buildings) {
            for (int y = b.ty; y < b.ty + b.th; y++) {
                for (int x = b.tx; x < b.tx + b.tw; x++) {
                    if (containsCell(wallTops, x, y) || containsCell(wallBodies, x, y)) {
                        throw new IllegalStateException("building " + b.name + " on wall at " + x + "," + y);
                    }
                }
            }
            if (solid[b.enterX][b.enterY]) {
                throw new IllegalStateException("entrance blocked: " + b.name);
            }
        }
        for (int[] t : trees) assertNotOnWall(t[0], t[1], "tree");
        for (int[] f : fences) assertNotOnWall(f[0], f[1], "fence");
        for (int[] w : wells) assertNotOnWall(w[0], w[1], "well");
        for (int[] b : bushes) assertNotOnWall(b[0], b[1], "bush");
        for (int[] f : fountains) assertNotOnWall(f[0], f[1], "fountain base");
        for (int[] f : fountainUppers) assertNotOnWall(f[0], f[1], "fountain upper");
        for (Npc n : npcs) assertNotOnWall(n.col, n.row, "npc " + n.name);
    }

    private void assertNotOnWall(int x, int y, String what) {
        if (containsCell(wallTops, x, y) || containsCell(wallBodies, x, y)) {
            throw new IllegalStateException(what + " on wall at " + x + "," + y);
        }
    }

    /** Top wall rows 0-1 solid + continuous, side caps + south edge solid, gate open. */
    private void assertWallCollision() {
        for (int[] w : wallTops) {
            if (!solid[w[0]][w[1]]) {
                throw new IllegalStateException("wallTop must be solid at " + w[0] + "," + w[1]);
            }
        }
        for (int[] w : wallBodies) {
            if (!solid[w[0]][w[1]]) {
                throw new IllegalStateException("wallBrick must be solid at " + w[0] + "," + w[1]);
            }
        }
        for (int[] w : invisibleBounds) {
            if (!solid[w[0]][w[1]]) {
                throw new IllegalStateException("bound must be solid at " + w[0] + "," + w[1]);
            }
        }
        // Sewers replace a brick where the river passes under a wall:
        // solid, with a cap directly above, on a valid brick row.
        for (int[] sw : sewers) {
            if (!solid[sw[0]][sw[1]]) {
                throw new IllegalStateException("sewer must be solid at " + sw[0] + "," + sw[1]);
            }
            if (!containsCell(wallTops, sw[0], sw[1] - 1)) {
                throw new IllegalStateException("sewer without cap above at " + sw[0] + "," + sw[1]);
            }
            boolean topRow = sw[1] == 4 && sw[0] >= 1 && sw[0] <= MAP_W - 2;
            boolean bottomBand = sw[1] == 24 && ((sw[0] <= 8) || (sw[0] >= 22));
            boolean frontRow = sw[1] == 31 && sw[0] >= 9 && sw[0] <= 21 && sw[0] != 15;
            if (!topRow && !bottomBand && !frontRow) {
                throw new IllegalStateException("sewer off brick row at " + sw[0] + "," + sw[1]);
            }
            if (containsCell(wallBodies, sw[0], sw[1])) {
                throw new IllegalStateException("sewer overlaps brick at " + sw[0] + "," + sw[1]);
            }
        }
        if (solid[15][30] || solid[15][31]) {
            throw new IllegalStateException("gate must stay walkable");
        }
    }

    /** Outer-rectangle wall continuity: caps/faces per band, 1-wide gate open. */
    private void assertTopWallContinuous() {
        // Top band: row 3 all caps; row 4 corners caps + x1..30 faces.
        for (int x = 0; x < MAP_W; x++) {
            if (!containsCell(wallTops, x, 3)) {
                throw new IllegalStateException("top cap gap at x=" + x);
            }
        }
        if (!containsCell(wallTops, 0, 4) || !containsCell(wallTops, MAP_W - 1, 4)) {
            throw new IllegalStateException("corner caps missing at (0,4)/(31,4)");
        }
        for (int x = 1; x < MAP_W - 1; x++) {
            if (isSewer(x, 4)) {
                if (!containsCell(wallTops, x, 3)) {
                    throw new IllegalStateException("top sewer cap gap at x=" + x);
                }
                continue;
            }
            if (!containsCell(wallBodies, x, 4)) {
                throw new IllegalStateException("top brick gap at x=" + x);
            }
        }
        // Sides col 0/31 rows 5..23 caps.
        for (int y = 5; y <= 23; y++) {
            if (!containsCell(wallTops, 0, y) || !containsCell(wallTops, MAP_W - 1, y)) {
                throw new IllegalStateException("side cap gap at y=" + y);
            }
        }
        // Bottom band rows 23-24: caps on row 23, faces/bricks on row 24 outside
        // the cols 10-20 opening (B8 roof + lanes pass through, no wall cells).
        for (int x = 0; x <= 9; x++) {
            if (!containsCell(wallTops, x, 23)) {
                throw new IllegalStateException("bottom-band west cap gap at x=" + x);
            }
        }
        for (int x = 21; x < MAP_W; x++) {
            if (!containsCell(wallTops, x, 23)) {
                throw new IllegalStateException("bottom-band east cap gap at x=" + x);
            }
        }
        for (int x = 10; x <= 20; x++) {
            if (containsCell(wallTops, x, 23) || containsCell(wallBodies, x, 23)
                    || containsCell(wallTops, x, 24) || containsCell(wallBodies, x, 24)) {
                throw new IllegalStateException("bottom opening blocked at x=" + x);
            }
        }
        if (!containsCell(wallTops, 9, 24) || !containsCell(wallTops, 21, 24)) {
            throw new IllegalStateException("stem junction caps missing at (9,24)/(21,24)");
        }
        for (int x = 0; x <= 8; x++) {
            if (isSewer(x, 24)) {
                if (!containsCell(wallTops, x, 23)) {
                    throw new IllegalStateException("bottom-band sewer cap gap at x=" + x);
                }
                continue;
            }
            if (!containsCell(wallBodies, x, 24)) {
                throw new IllegalStateException("bottom-band west brick gap at x=" + x);
            }
        }
        for (int x = 22; x < MAP_W; x++) {
            if (isSewer(x, 24)) {
                if (!containsCell(wallTops, x, 23)) {
                    throw new IllegalStateException("bottom-band sewer cap gap at x=" + x);
                }
                continue;
            }
            if (!containsCell(wallBodies, x, 24)) {
                throw new IllegalStateException("bottom-band east brick gap at x=" + x);
            }
        }
        // Stem sides col 9/21 rows 25..29 caps.
        for (int y = 25; y <= 29; y++) {
            if (!containsCell(wallTops, 9, y) || !containsCell(wallTops, 21, y)) {
                throw new IllegalStateException("stem cap gap at y=" + y);
            }
        }
        // Front 3D wrap across the stem: row 30 caps, row 31 bricks, 1-wide gate.
        // Sewers (if any) replace a brick with cap above; validated separately.
        for (int x = 9; x <= 21; x++) {
            if (x == 15) continue;
            if (isSewer(x, 31)) {
                if (!containsCell(wallTops, x, 30)) {
                    throw new IllegalStateException("front sewer cap gap at x=" + x);
                }
                continue;
            }
            if (!containsCell(wallTops, x, 30) || !containsCell(wallBodies, x, 31)) {
                throw new IllegalStateException("front wall gap at x=" + x);
            }
        }
        // Brick faces only where a T sits above: south edges of the mask.
        for (int[] w : wallBodies) {
            boolean topRow = w[1] == 4 && w[0] >= 1 && w[0] <= MAP_W - 2;
            boolean bottomBand = w[1] == 24 && ((w[0] <= 8) || (w[0] >= 22));
            boolean frontRow = w[1] == 31 && w[0] >= 9 && w[0] <= 21;
            if (!topRow && !bottomBand && !frontRow) {
                throw new IllegalStateException("brick face violated at " + w[0] + "," + w[1]);
            }
        }
        // T-over-W structure: every face must have a cap directly above it.
        for (int[] w : wallBodies) {
            if (w[1] == 0 || !containsCell(wallTops, w[0], w[1] - 1)) {
                throw new IllegalStateException("face without cap above at " + w[0] + "," + w[1]);
            }
        }
    }

    private static boolean containsCell(java.util.List<int[]> list, int x, int y) {
        for (int[] a : list) if (a[0] == x && a[1] == y) return true;
        return false;
    }

    private void assertNotOnDoor(int x, int y, String what) {
        for (Building b : buildings) {
            if ((x == b.doorX && y == b.doorY) || (x == b.enterX && y == b.enterY)) {
                throw new IllegalStateException(what + " on door/entrance at " + x + "," + y);
            }
        }
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

    /**
     * Fountain plaza: 3x3 cobble at cols 15-17 / rows 20-22, all PATH ground.
     * Base (16,21) solid with fountain_base art; upper (16,20) walkable with
     * fountain_upper art drawn overhead. Plaza stays inside walls, off
     * buildings/doors/walls, with the walkable ring keeping flood-fill intact.
     */
    private void assertFountainPlaza() {
        for (int y = 20; y <= 22; y++) {
            for (int x = 15; x <= 17; x++) {
                if (!isInsideMap(x, y)) {
                    throw new IllegalStateException("fountain plaza outside map at " + x + "," + y);
                }
                if (ground[x][y] != Ground.PATH) {
                    throw new IllegalStateException("fountain plaza must be PATH at " + x + "," + y);
                }
                assertNotOnWall(x, y, "fountain plaza");
                assertNotOnDoor(x, y, "fountain plaza");
                for (Building b : buildings) {
                    if (x >= b.tx && x < b.tx + b.tw && y >= b.ty && y < b.ty + b.th) {
                        throw new IllegalStateException("fountain plaza on building at " + x + "," + y);
                    }
                }
            }
        }
        if (fountains.size() != 1 || fountainUppers.size() != 1) {
            throw new IllegalStateException("fountain needs 1 base + 1 upper, have "
                    + fountains.size() + "+" + fountainUppers.size());
        }
        int[] base = fountains.get(0);
        int[] upper = fountainUppers.get(0);
        if (base[0] != 16 || base[1] != 21) {
            throw new IllegalStateException("fountain base off center at " + base[0] + "," + base[1]);
        }
        if (upper[0] != 16 || upper[1] != 20) {
            throw new IllegalStateException("fountain upper off center at " + upper[0] + "," + upper[1]);
        }
        if (!solid[base[0]][base[1]]) {
            throw new IllegalStateException("fountain base must be solid");
        }
        if (solid[upper[0]][upper[1]]) {
            throw new IllegalStateException("fountain upper must stay walkable");
        }
    }

    /**
     * Forest placement (locked schematic dark-green cells): every tree sits
     * inside the map on GRASS ground (stamped TREE), solid, off walls /
     * buildings / doors / entrances / NPC spawns / player spawn, with 1 free
     * cell around every entrance, 1 cell off the fountain plaza margin
     * (cols 14-18, rows 19-23), and never touching a building front (south) row.
     */
    private void assertTreePlacement() {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int[] t : TREES) {
            int x = t[0], y = t[1];
            String key = x + "," + y;
            if (!isInsideMap(x, y)) {
                throw new IllegalStateException("tree outside map at " + key);
            }
            if (!seen.add(key)) {
                throw new IllegalStateException("tree duplicate at " + key);
            }
            if (ground[x][y] != Ground.TREE) {
                throw new IllegalStateException("tree must stamp TREE ground at " + key);
            }
            if (!solid[x][y]) {
                throw new IllegalStateException("tree must be solid at " + key);
            }
            assertNotOnWall(x, y, "tree");
            assertNotOnDoor(x, y, "tree");
            for (Building b : buildings) {
                if (x >= b.tx && x < b.tx + b.tw && y >= b.ty && y < b.ty + b.th) {
                    throw new IllegalStateException("tree on building at " + key);
                }
                if (y == b.ty + b.th && x >= b.tx && x < b.tx + b.tw) {
                    throw new IllegalStateException("tree touches building front at " + key);
                }
                if (Math.abs(x - b.enterX) <= 1 && Math.abs(y - b.enterY) <= 1) {
                    throw new IllegalStateException("tree inside entrance buffer at " + key);
                }
            }
            for (Npc n : npcs) {
                if (n.col == x && n.row == y) {
                    throw new IllegalStateException("tree on npc spawn at " + key);
                }
            }
            if (x == SPAWN_C && y == SPAWN_R) {
                throw new IllegalStateException("tree on player spawn at " + key);
            }
            if (x >= 14 && x <= 18 && y >= 19 && y <= 23) {
                throw new IllegalStateException("tree inside plaza margin at " + key);
            }
        }
        if (trees.size() != TREES.length) {
            throw new IllegalStateException("tree count drift: have " + trees.size() + ", want " + TREES.length);
        }
    }

    /**
     * Forest connectivity: flood fill from spawn over walkable cells must reach
     * every PATH/GATE cell (the fountain base island excluded by design) and
     * every entrance — trees must not cut off any area or route to a door.
     */
    private void assertTreeConnectivity() {
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
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                if (x == 16 && y == 21) continue; // fountain base: solid island by design
                Ground g = ground[x][y];
                if ((g == Ground.PATH || g == Ground.GATE) && !seen[x][y]) {
                    throw new IllegalStateException("path cut off by trees at " + x + "," + y);
                }
            }
        }
        for (Building b : buildings) {
            if (!seen[b.enterX][b.enterY] || !seen[b.doorX][b.doorY]) {
                throw new IllegalStateException("door cut off by trees: " + b.name);
            }
        }
    }

    // ---- 2. Grid movement + key-state input ----
    public void update(InputManager in) {
        if (fadePhase != 0) { // scene swap in progress: freeze everything
            updateTransition();
            return;
        }
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

        // The opening press never walks through: latch clears once every
        // movement key is released, so entry always needs a fresh click.
        if (doorLatch && !up && !down && !left && !right) doorLatch = false;

        // Face-to-face freeze: while the dialogue panel is open both sides stay
        // idle (NPC already skips its routine in TALKING); E still advances it.
        if (dialogue.isOpen()) want = null;

        if (!stepping) {
            if (want == null) {
                walkFrame = 0;
            } else if (want != facing) {
                facing = want; // turn first, move next tick
                // Turning to face a shut door counts as the opening tap, so
                // entry is always 2 taps — never turn + bump + step.
                if (!inInnInterior) bumpDoor(col + want.dx, row + want.dy);
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
                // Stepping off shuts it behind you.
                if (openDoor != null && (col != openDoor.doorX || row != openDoor.doorY)) {
                    shutDoor(openDoor);
                }
                // Cross the threshold: fade into (or out of) the Inn interior.
                if (inInnInterior) {
                    if (col == INN_DOOR_X && row == INN_DOOR_Y) {
                        int ex = SPAWN_C;
                        int ey = SPAWN_R;
                        for (Building b : buildings) {
                            if ("INN".equals(b.name)) {
                                ex = b.enterX;
                                ey = b.enterY;
                            }
                        }
                        beginTransition(false, ex, ey, Dir.UP);
                    }
                } else if (openDoor != null && "INN".equals(openDoor.name)
                        && col == openDoor.doorX && row == openDoor.doorY) {
                    beginTransition(true, INN_SPAWN_X, INN_SPAWN_Y, Dir.DOWN);
                }
            }
        }

        if (in.drainTalkPressed()) interact();
        if (inInnInterior) {
            // The innkeeper just holds position; villagers stay outside.
            updateInnkeeperFace();
        } else {
            trackStuck(want);
            updateNpcs();
        }
        // Walked away mid-chat: shut the panel so no frozen box lingers.
        if (dialogue.isOpen()) {
            int dc = Math.abs(col - dialogue.speakerCol()) + Math.abs(row - dialogue.speakerRow());
            if (dc > 2) {
                dialogue.close();
                talkingNpc = null;
                message = "";
                msgTimer = 0;
            }
        }
        boolean f3 = in.isHeld(KeyEvent.VK_F3);
        if (f3 && !prevF3) debug = !debug;
        prevF3 = f3;

        if (msgTimer > 0 && !dialogue.isOpen() && --msgTimer == 0) message = "";
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
        if (inInnInterior) {
            if (tc < 0 || tr < 0 || tc >= INN_INT_W || tr >= INN_INT_H) return;
            if (innSolid[tc][tr]) return;
        } else {
            if (!isInsideMap(tc, tr)) return;
            if (bumpDoor(tc, tr)) return; // tap 1 opens; tap 2 steps through
            // The opening press never walks through: release + click again to enter.
            if (doorLatch && openDoor != null && tc == openDoor.doorX && tr == openDoor.doorY) return;
            if (solid[tc][tr]) return; // blocked
        }

        stepping = true;
        fromC = col; fromR = row;
        toC = tc; toR = tr;
        stepT = 0;
        animT = ANIM_TICKS;
        walkFrame = 1;
    }

    /** E works only on the entrance cell, facing up at the door. */
    private void interact() {
        // An open panel eats the press: advance, or close when done talking.
        if (dialogue.isOpen()) {
            String t = dialogue.advance();
            if (t == null) {
                if (talkingNpc != null) talkingNpc.state = NpcState.IDLE;
                talkingNpc = null;
                message = "";
                msgTimer = 0;
            } else {
                message = t;
                msgTimer = MSG_TICKS;
            }
            return;
        }
        if (inInnInterior) {
            int fx = col + facing.dx;
            int fy = row + facing.dy;
            // Talk straight to the innkeeper, or across the counter to them.
            int tx = fx;
            int ty = fy;
            if (fx >= 0 && fy >= 0 && fx < INN_INT_W && fy < INN_INT_H
                    && innWalls[fx][fy] == 2) {
                tx = fx + facing.dx;
                ty = fy + facing.dy;
            }
            if (innkeeper != null && tx == innkeeper.col && ty == innkeeper.row) {
                openTalk(innkeeper);
                return;
            }
            talkingNpc = null;
            message = "The inn is quiet. The innkeeper waits behind the counter.";
            msgTimer = MSG_TICKS / 2;
            return;
        }
        for (Building b : buildings) {
            if (col == b.enterX && row == b.enterY && facing == Dir.UP
                    && col + facing.dx == b.doorX && row + facing.dy == b.doorY) {
                talkingNpc = null;
                message = b.blurb;
                msgTimer = MSG_TICKS;
                return;
            }
        }
        int fx = col + facing.dx;
        int fy = row + facing.dy;
        for (Npc n : npcs) {
            if (fx == n.col && fy == n.row) {
                openTalk(n);
                return;
            }
        }
        talkingNpc = null;
        message = "Stand on a doorstep facing the door, then press E.";
        msgTimer = MSG_TICKS / 2;
    }

    /** Face each other, freeze the routine, open the dialogue panel. */
    private void openTalk(Npc n) {
        n.state = NpcState.TALKING;
        n.breaking = false;
        n.stepping = false;
        n.snap(); // logical cell already moved at step begin; re-sync render
        n.facing = dirTo(n.col, n.row, col, row);
        talkingNpc = n;
        dialogue.open(n);
        message = dialogue.advance();
        msgTimer = MSG_TICKS;
    }

    /** Innkeeper tracks the player while the panel is up (no wander logic). */
    private void updateInnkeeperFace() {
        if (innkeeper != null && dialogue.isOpen() && talkingNpc == innkeeper) {
            innkeeper.facing = dirTo(innkeeper.col, innkeeper.row, col, row);
        }
    }

    private static Dir dirTo(int fromC, int fromR, int toC, int toR) {
        int dx = toC - fromC;
        int dy = toR - fromR;
        if (Math.abs(dx) >= Math.abs(dy)) return dx >= 0 ? Dir.RIGHT : Dir.LEFT;
        return dy >= 0 ? Dir.DOWN : Dir.UP;
    }

    /** Stuck watch: pushing a held direction with zero cell progress. */
    private void trackStuck(Dir want) {
        if (col != lastPc || row != lastPr) {
            stuckT = 0;
            lastPc = col;
            lastPr = row;
            return;
        }
        if (want == null) {
            stuckT = 0;
            return;
        }
        if (++stuckT >= 30) {
            stuckT = 0;
            forceYield();
        }
    }

    /**
     * Anti-wall override: player fully stuck nose-to-block. Every villager in
     * the facing path yields once, nearest first, minute gate bypassed (it
     * re-arms after, so no spam). Talkers are never grabbed mid-dialogue.
     */
    private void forceYield() {
        for (int step = 1; step <= 2; step++) {
            int tc = col + facing.dx * step;
            int ty = row + facing.dy * step;
            for (Npc n : npcs) {
                if (!n.mind || n.state == NpcState.TALKING || n.stepping) continue;
                if (n.col == tc && n.row == ty) {
                    n.asideCd = 0;
                    n.avoidCd = 0;
                    yieldStep(n, false);
                }
            }
        }
    }

    /** Another villager within Manhattan range (for spacing rules). */
    private boolean npcNear(Npc self, int c, int r, int range) {
        for (Npc o : npcs) {
            if (o != self && Math.abs(o.col - c) + Math.abs(o.row - r) <= range) return true;
        }
        return false;
    }

    private void updateNpcs() {
        for (Npc n : npcs) {
            if (!n.mind) continue;
            if (n.stepping) {
                stepNpc(n);
                continue;
            }
            if (n.state == NpcState.TALKING) {
                n.facing = dirTo(n.col, n.row, col, row);
                if (talkingNpc != n || msgTimer == 0) {
                    n.state = NpcState.IDLE;
                    n.idleT = 60 + rng.nextInt(120);
                }
                continue;
            }
            if (n.asideCd > 0) n.asideCd--;
            if (n.avoidCd > 0) n.avoidCd--;
            boolean pressing = playerPressing(n);
            // Herding accumulator: sustained pressing breaks into committed escape.
            if (pressing) n.pushT++;
            else if (n.pushT > 0) n.pushT -= 2;
            if (playerDist(n) > NPC_RESUME_DIST) n.pushT = 0;
            // Cornered player: a villager on an exit cell clears 2-3 chained
            // steps, gates bypassed (they re-arm, so no spam). Never replan a
            // queue already being walked (pass-through or clearing in flight).
            boolean midQueue = (n.state == NpcState.MOVE_ASIDE || n.state == NpcState.BACK_UP
                    || n.state == NpcState.PASS_THROUGH) && n.path != null && !n.path.isEmpty();
            if (!midQueue && playerPocketed() && playerDist(n) == 1) {
                n.asideCd = 0;
                n.avoidCd = 0;
                if (cornerClear(n)) continue;
            }
            // Herded over and over: break away once toward open ground.
            if (!n.breaking && n.pushT >= 90) {
                if (startBreakaway(n)) continue;
            }
            // Yield pressure, at most one dodge per minute per villager.
            if (!n.breaking && n.asideCd == 0 && n.avoidCd == 0 && pressing) {
                if (yieldStep(n, followedByPlayer(n))) continue;
            }
            switch (n.state) {
                case IDLE -> {
                    if (isWide(n.col, n.row, n)) {
                        n.safeC = n.col;
                        n.safeR = n.row;
                    }
                    if (playerDist(n) > NPC_RESUME_DIST && n.idleT > 0 && --n.idleT > 0) break;
                    if (playerDist(n) <= NPC_RESUME_DIST) {
                        n.idleT = Math.max(n.idleT, 30);
                        break;
                    }
                    int[] target = pickHaunt(n);
                    if (target == null) {
                        n.idleT = 120 + rng.nextInt(180);
                        n.facing = Dir.values()[rng.nextInt(4)];
                        break;
                    }
                    n.path = findPath(n, target[0], target[1]);
                    if (n.path == null || n.path.isEmpty()) {
                        n.idleT = 120 + rng.nextInt(120);
                        break;
                    }
                    n.state = NpcState.WANDER;
                }
                case WANDER -> {
                    if (n.path == null || n.path.isEmpty()) {
                        n.state = NpcState.IDLE;
                        n.breaking = false;
                        n.idleT = 120 + rng.nextInt(240); // rest 2-6s, then look around
                        n.facing = Dir.values()[rng.nextInt(4)];
                        break;
                    }
                    int[] next = n.path.peek();
                    if (npcStepBlocked(n, next[0], next[1])) {
                        n.path = findPath(n, n.path.getLast()[0], n.path.getLast()[1]);
                        if (n.path == null) {
                            n.state = NpcState.IDLE;
                            n.idleT = 90 + rng.nextInt(90);
                        }
                        break;
                    }
                    n.path.poll();
                    beginNpcStep(n, next[0], next[1]);
                }
                case MOVE_ASIDE, BACK_UP -> {
                    // Chained clearing (corner exits): consume the queue first.
                    if (n.path != null && !n.path.isEmpty()) {
                        int[] next = n.path.peek();
                        boolean isPlayerCell = (next[0] == col && next[1] == row);
                        if (!isPlayerCell && npcStepBlocked(n, next[0], next[1])) {
                            n.path = null;
                        } else {
                            n.path.poll();
                            beginNpcStep(n, next[0], next[1]);
                            break;
                        }
                    }
                    // Arrived: wait for the player to pass at resume distance.
                    if (playerDist(n) > NPC_RESUME_DIST) {
                        n.state = NpcState.IDLE;
                        n.idleT = 60 + rng.nextInt(120);
                    } else {
                        n.idleT = 30;
                        n.state = NpcState.IDLE;
                    }
                }
                case PASS_THROUGH -> {
                    // Squeezing past: step queue is player cell, then beyond.
                    // Only the player's live cell is bypassed; walls/NPCs still abort.
                    if (n.path == null || n.path.isEmpty()) {
                        n.state = NpcState.IDLE;
                        n.idleT = 60 + rng.nextInt(120);
                        break;
                    }
                    int[] next = n.path.peek();
                    boolean isPlayerCell = (next[0] == col && next[1] == row);
                    if (!isPlayerCell && npcStepBlocked(n, next[0], next[1])) {
                        n.path = null;
                        n.state = NpcState.IDLE;
                        n.idleT = 60 + rng.nextInt(60);
                        break;
                    }
                    n.path.poll();
                    beginNpcStep(n, next[0], next[1]);
                }
                case TALKING -> { // handled above; unreachable here
                }
            }
        }
    }

    private int playerDist(Npc n) {
        return Math.abs(col - n.col) + Math.abs(row - n.row);
    }

    /** True when the player is close and heading into the villager's cell. */
    private boolean playerBlockedBy(Npc n) {
        if (playerDist(n) > NPC_BLOCK_DIST) return false;
        // Facing cell match (standing or turning toward the villager).
        if (col + facing.dx == n.col && row + facing.dy == n.row) return true;
        // Mid-step into the villager.
        if (stepping && toC == n.col && toR == n.row) return true;
        return false;
    }

    /** Tailed in a straight line: stepping behind within range, villager ahead. */
    private boolean followedByPlayer(Npc n) {
        int dist = playerDist(n);
        if (dist < 2 || dist > NPC_FOLLOW_DIST || !stepping) return false;
        int ahead = (n.col - col) * facing.dx + (n.row - row) * facing.dy;
        if (ahead <= 0) return false;
        int side = Math.abs((n.col - col) * facing.dy - (n.row - row) * facing.dx);
        return side <= 1;
    }

    /** Closing in from any side: this step shortens the distance to the villager. */
    private boolean playerClosing(Npc n) {
        if (playerDist(n) > NPC_BLOCK_DIST || !stepping) return false;
        int next = Math.abs(toC - n.col) + Math.abs(toR - n.row);
        return next < playerDist(n);
    }

    private boolean playerPressing(Npc n) {
        return playerBlockedBy(n) || playerClosing(n) || followedByPlayer(n);
    }

    /**
     * One polite step: free side cells first (MOVE_ASIDE), else the cell behind
     * the approach (BACK_UP toward the last wide spot). Never onto doors,
     * entrances, the player, walls, or other villagers. A tailed dodge chains:
     * short cooldown so it keeps sidestepping while followed, and the stale
     * wander path is dropped so it never walks straight back into the tail.
     */
    private boolean yieldStep(Npc n, boolean chased) {
        int px = col + facing.dx; // where the player is pressing toward
        int py = row + facing.dy;
        boolean comingNorth = py < n.row;
        int[][] sides = comingNorth || py != n.row
                ? new int[][]{{n.col - 1, n.row}, {n.col + 1, n.row}, {n.col, n.row + 1}}
                : new int[][]{{n.col, n.row - 1}, {n.col, n.row + 1}, {n.col + (n.col >= col ? 1 : -1), n.row}};
        for (int[] s : sides) {
            if (npcRestFree(n, s[0], s[1]) && !(s[0] == px && s[1] == py)
                    && !npcNear(n, s[0], s[1], 1)) {
                beginNpcStep(n, s[0], s[1]);
                n.state = NpcState.MOVE_ASIDE;
                n.breaking = false;
                n.path = null;
                n.asideCd = chased ? 10 : NPC_STEP_TICKS + 30;
                n.avoidCd = 3600; // one dodge per minute
                return true;
            }
        }
        // Second pass: any free side cell, even beside another villager.
        for (int[] s : sides) {
            if (npcRestFree(n, s[0], s[1]) && !(s[0] == px && s[1] == py)) {
                beginNpcStep(n, s[0], s[1]);
                n.state = NpcState.MOVE_ASIDE;
                n.breaking = false;
                n.path = null;
                n.asideCd = chased ? 10 : NPC_STEP_TICKS + 30;
                n.avoidCd = 3600; // one dodge per minute
                return true;
            }
        }
        // Nose-to-nose deadlock: pass THROUGH — player cell, then the cell
        // beyond along the villager's heading (2 steps forward). Beyond must be
        // free terrain with no other villager; the player's live cell is the
        // only bypass, consumed by the PASS_THROUGH state.
        boolean faceToFace = (col + facing.dx == n.col && row + facing.dy == n.row)
                && (n.col + n.facing.dx == col && n.row + n.facing.dy == row);
        if (faceToFace) {
            int midC = n.col + n.facing.dx; // == player cell when nose-to-nose
            int midR = n.row + n.facing.dy;
            int bx = n.col + 2 * n.facing.dx; // cell beyond the player
            int by = n.row + 2 * n.facing.dy;
            if (isInsideMap(bx, by) && !solid[bx][by] && !npcAt(bx, by, n)
                    && !isDoorOrEntrance(bx, by)) {
                java.util.ArrayDeque<int[]> pass = new java.util.ArrayDeque<>();
                pass.add(new int[]{midC, midR});
                pass.add(new int[]{bx, by});
                n.path = pass;
                n.state = NpcState.PASS_THROUGH;
                n.breaking = false;
                n.asideCd = NPC_STEP_TICKS + 30;
                n.avoidCd = 3600; // one dodge per minute
                return true;
            }
        }
        // Player directly behind in a squeeze: one step forward if free.
        if (col == n.col - n.facing.dx && row == n.row - n.facing.dy) {
            int fx = n.col + n.facing.dx;
            int fy = n.row + n.facing.dy;
            if (npcRestFree(n, fx, fy)) {
                beginNpcStep(n, fx, fy);
                n.state = NpcState.MOVE_ASIDE;
                n.path = null;
                n.asideCd = NPC_STEP_TICKS + 30;
                n.avoidCd = 3600; // one dodge per minute
                return true;
            }
        }
        // 1-wide: back away from the player toward the last wide spot.
        int bx = n.col - facing.dx;
        int by = n.row - facing.dy;
        if (npcRestFree(n, bx, by)) {
            beginNpcStep(n, bx, by);
            n.state = NpcState.BACK_UP;
            n.breaking = false;
            n.path = null;
            n.asideCd = chased ? 10 : NPC_STEP_TICKS + 30;
            n.avoidCd = 3600; // one dodge per minute
            return true;
        }
        return false;
    }

    /** Pocketed player: one or zero walkable neighbors (villagers count as walls). */
    private boolean playerPocketed() {
        int free = 0;
        int[][] adj = {{col + 1, row}, {col - 1, row}, {col, row + 1}, {col, row - 1}};
        for (int[] a : adj) {
            if (isInsideMap(a[0], a[1]) && !solid[a[0]][a[1]]) free++;
        }
        return free <= 1;
    }

    /**
     * Corner exit: chained 2-3 steps to the nearest standing cell at least 3
     * from the player, leaving the exit line. BFS shortest-first over steppable
     * cells (never through the player); gates already bypassed by the caller.
     */
    private boolean cornerClear(Npc n) {
        boolean[][] seen = new boolean[MAP_W][MAP_H];
        int[][] px = new int[MAP_W][MAP_H];
        int[][] py = new int[MAP_W][MAP_H];
        int[][] depth = new int[MAP_W][MAP_H];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{n.col, n.row});
        seen[n.col][n.row] = true;
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int d = depth[cur[0]][cur[1]];
            if (d > 0 && d <= 6 && npcRestFree(n, cur[0], cur[1])
                    && Math.abs(cur[0] - col) + Math.abs(cur[1] - row) >= 3) {
                java.util.ArrayDeque<int[]> steps = new java.util.ArrayDeque<>();
                int[] c = cur;
                while (!(c[0] == n.col && c[1] == n.row)) {
                    steps.addFirst(c);
                    c = new int[]{px[c[0]][c[1]], py[c[0]][c[1]]};
                }
                n.path = steps;
                n.state = NpcState.MOVE_ASIDE;
                n.breaking = false;
                n.asideCd = 10;
                n.avoidCd = 3600; // counts as the minute's dodge
                return true;
            }
            if (d == 6) continue;
            for (int i = 0; i < 4; i++) {
                int nx = cur[0] + dx[i];
                int ny = cur[1] + dy[i];
                if (!isInsideMap(nx, ny) || seen[nx][ny]) continue;
                if (nx == col && ny == row) continue; // never route through the player
                if (solid[nx][ny] && !(nx == n.col && ny == n.row)) continue;
                boolean occ = false;
                for (Npc o : npcs) {
                    if (o != n && o.col == nx && o.row == ny) {
                        occ = true;
                        break;
                    }
                }
                if (occ) continue;
                seen[nx][ny] = true;
                px[nx][ny] = cur[0];
                py[nx][ny] = cur[1];
                depth[nx][ny] = d + 1;
                q.add(new int[]{nx, ny});
            }
        }
        return false;
    }

    /**
     * Herded over and over: commit to the reachable zone cell with the most
     * separation plus open neighbors, face it at once, stroll there. Counts as
     * the minute's dodge.
     */
    private boolean startBreakaway(Npc n) {
        int bestScore = -1;
        int[] best = null;
        java.util.ArrayDeque<int[]> bestPath = null;
        int start = rng.nextInt(n.zone.length);
        for (int i = 0; i < n.zone.length; i++) {
            int[] h = n.zone[(start + i) % n.zone.length];
            int d = Math.abs(h[0] - col) + Math.abs(h[1] - row);
            if (d <= playerDist(n)) continue;
            if (!npcRestFree(n, h[0], h[1])) continue;
            java.util.ArrayDeque<int[]> p = findPath(n, h[0], h[1]);
            if (p == null || p.isEmpty()) continue;
            int open = 0;
            int[][] adj = {{h[0] + 1, h[1]}, {h[0] - 1, h[1]}, {h[0], h[1] + 1}, {h[0], h[1] - 1}};
            for (int[] a : adj) {
                if (npcRestFree(n, a[0], a[1])) open++;
            }
            int score = d + 2 * open;
            if (score > bestScore) {
                bestScore = score;
                best = h;
                bestPath = p;
            }
        }
        if (bestPath == null) return false;
        n.path = bestPath;
        n.state = NpcState.WANDER;
        n.breaking = true;
        n.facing = dirTo(n.col, n.row, best[0], best[1]);
        n.pushT = 0;
        n.asideCd = 10;
        n.avoidCd = 3600; // counts as the minute's dodge
        return true;
    }

    /** Another villager's logical cell (mid-tween positions already moved). */
    private boolean npcAt(int c, int r, Npc self) {
        for (Npc o : npcs) {
            if (o != self && o.col == c && o.row == r) return true;
        }
        return false;
    }

    /** A cell the villager may stand on: walkable, unoccupied, never a doorway. */
    private boolean npcRestFree(Npc self, int c, int r) {
        if (!isInsideMap(c, r) || solid[c][r]) return false;
        if (c == col && r == row) return false; // the player
        for (Npc o : npcs) {
            if (o != self && o.col == c && o.row == r) return false;
        }
        return !isDoorOrEntrance(c, r);
    }

    private boolean npcStepBlocked(Npc self, int c, int r) {
        if (!isInsideMap(c, r)) return true;
        if (solid[c][r] && !(c == self.col && r == self.row)) return true;
        if (c == col && r == row) return true;
        for (Npc o : npcs) {
            if (o != self && o.col == c && o.row == r) return true;
        }
        return false;
    }

    private boolean isDoorOrEntrance(int c, int r) {
        for (Building b : buildings) {
            if ((c == b.doorX && r == b.doorY) || (c == b.enterX && r == b.enterY)) return true;
        }
        return false;
    }

    /** Wide cell: at least one free standing neighbor (room to let someone pass). */
    private boolean isWide(int c, int r, Npc self) {
        int[][] adj = {{c + 1, r}, {c - 1, r}, {c, r + 1}, {c, r - 1}};
        for (int[] a : adj) {
            if (npcRestFree(self, a[0], a[1])) return true;
        }
        return false;
    }

    private int[] pickHaunt(Npc n) {
        // Two passes over the home zone: first haunts no closer to the player
        // than now (never walk back into a tail), then anything reachable.
        // Zones keep the five villagers scattered over every part of the village.
        for (int pass = 0; pass < 2; pass++) {
            int start = rng.nextInt(n.zone.length);
            for (int i = 0; i < n.zone.length; i++) {
                int[] h = n.zone[(start + i) % n.zone.length];
                if ((h[0] == n.col && h[1] == n.row) || Math.abs(h[0] - n.col) + Math.abs(h[1] - n.row) > 12) continue;
                // Never rest shoulder-to-shoulder on walked ground: on path-kind
                // tiles keep one cell clear of other villagers (pass 1 may relax).
                if (pass == 0 && isPathKind(h[0], h[1]) && npcNear(n, h[0], h[1], 1)) continue;
                if ((h[0] == n.col && h[1] == n.row) || Math.abs(h[0] - n.col) + Math.abs(h[1] - n.row) > 12) continue;
                if (pass == 0 && playerDist(n) <= NPC_FOLLOW_DIST + 2
                        && Math.abs(h[0] - col) + Math.abs(h[1] - row) < playerDist(n)) continue;
                if (!npcRestFree(n, h[0], h[1])) continue;
                if (findPath(n, h[0], h[1]) == null) continue;
                return h;
            }
        }
        return null;
    }

    /** BFS over walkable cells; returns steps from (not incl.) start to target. */
    private java.util.ArrayDeque<int[]> findPath(Npc self, int tc, int tr) {
        if (!isInsideMap(tc, tr)) return null;
        int[][] prev = new int[MAP_W * MAP_H][2];
        boolean[][] seen = new boolean[MAP_W][MAP_H];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{self.col, self.row});
        seen[self.col][self.row] = true;
        prev[self.col * MAP_H + self.row] = new int[]{-1, -1};
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            if (cur[0] == tc && cur[1] == tr) break;
            for (int i = 0; i < 4; i++) {
                int nx = cur[0] + dx[i];
                int ny = cur[1] + dy[i];
                if (!isInsideMap(nx, ny) || seen[nx][ny]) continue;
                if (nx == col && ny == row) continue; // never route through the player
                if (solid[nx][ny] && !(nx == self.col && ny == self.row)) continue;
                boolean occ = false;
                for (Npc o : npcs) {
                    if (o != self && o.col == nx && o.row == ny) {
                        occ = true;
                        break;
                    }
                }
                if (occ && !(nx == tc && ny == tr)) continue;
                seen[nx][ny] = true;
                prev[nx * MAP_H + ny] = cur;
                q.add(new int[]{nx, ny});
            }
        }
        if (!seen[tc][tr]) return null;
        java.util.ArrayDeque<int[]> steps = new java.util.ArrayDeque<>();
        int[] cur = new int[]{tc, tr};
        while (!(cur[0] == self.col && cur[1] == self.row)) {
            steps.addFirst(cur);
            cur = prev[cur[0] * MAP_H + cur[1]];
        }
        return steps;
    }

    private void beginNpcStep(Npc n, int tc, int tr) {
        // Logical cell moves now (solid follows immediately) so a second walker
        // can never route into the vacating cell mid-tween; render catches up.
        solid[n.col][n.row] = false;
        n.fromC = n.col;
        n.fromR = n.row;
        n.col = tc;
        n.row = tr;
        n.toC = tc;
        n.toR = tr;
        solid[tc][tr] = true;
        n.stepping = true;
        n.stepT = 0;
        n.facing = dirTo(n.fromC, n.fromR, tc, tr);
    }

    private void stepNpc(Npc n) {
        n.stepT++;
        double t = Math.min(1.0, n.stepT / (double) NPC_STEP_TICKS);
        n.renderCx = (n.fromC * TILE + TILE / 2.0) + (n.toC - n.fromC) * TILE * t;
        n.renderCy = (n.fromR * TILE + TILE / 2.0) + (n.toR - n.fromR) * TILE * t;
        if (n.stepT >= NPC_STEP_TICKS) {
            n.snap();
            n.stepping = false;
            n.trail.addLast(new int[]{n.col, n.row});
            while (n.trail.size() > 6) n.trail.pollFirst();
            if (isWide(n.col, n.row, n)) {
                n.safeC = n.col;
                n.safeR = n.row;
            }
        }
    }

    // ---- 3. Camera ----
    private void updateCamera(int viewW, int viewH) {
        int viewTilesW = (viewW + TILE * SCALE - 1) / (TILE * SCALE);
        int viewTilesH = (viewH + TILE * SCALE - 1) / (TILE * SCALE);
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
            if (inInnInterior) {
                drawInterior(g2, viewW, viewH);
                drawFade(g2, viewW, viewH);
                return;
            }
            int viewTilesW = Math.max(1, (viewW + TILE * SCALE - 1) / (TILE * SCALE));
            int viewTilesH = Math.max(1, (viewH + TILE * SCALE - 1) / (TILE * SCALE));
            updateCamera(viewW, viewH);

            g2.setColor(GamePalette.ROYAL_DEEP);
            g2.fillRect(0, 0, viewW, viewH);
            java.awt.Shape oldClip = g2.getClip();
            g2.setClip(0, 0, viewW, viewH);

            int offX = 0;
            int offY = 0;

            int c0 = 0;
            int r0 = 0;
            int c1 = MAP_W - 1;
            int r1 = MAP_H - 1;

            // Ground sprites only (cobble carries its own edging; no edge lines).
            for (int r = r0; r <= r1; r++) {
                for (int c = c0; c <= c1; c++) {
                    paintGround(g2, offX, offY, c, r);
                }
            }

            // Fountain plaza ground (3x3 cobble) already painted by paintGround().
            // No separate ground stamp needed here.

            // Y-sorted Layer 2: building bases, wall caps/faces, tree trunks, fountain base, NPCs, player.
            // Roofs/canopies/wall faces/fountain upper re-appear in Layer 3 below (overhead).
            java.util.TreeMap<Integer, java.util.List<Runnable>> order = new java.util.TreeMap<>();
            for (Building b : buildings) {
                int baseY = (b.ty + b.th) * TILE;
                order.computeIfAbsent(baseY * 4 + 1, k -> new java.util.ArrayList<>())
                        .add(() -> drawBuilding(g2, offX, offY, b));
            }
            for (int[] w : wallBodies) {
                if (w[0] < c0 - 1 || w[0] > c1 + 1 || w[1] < r0 - 1 || w[1] > r1 + 1) continue;
                int baseY = (w[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawWallBrick(g2, offX, offY, w[0], w[1]));
            }
            for (int[] sw : sewers) {
                if (sw[0] < c0 - 1 || sw[0] > c1 + 1 || sw[1] < r0 - 1 || sw[1] > r1 + 1) continue;
                int baseY = (sw[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawWallBrick(g2, offX, offY, sw[0], sw[1]));
            }
            for (int[] w : wallTops) {
                if (w[0] < c0 - 1 || w[0] > c1 + 1 || w[1] < r0 - 1 || w[1] > r1 + 1) continue;
                int baseY = (w[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawWallTop(g2, offX, offY, w[0], w[1]));
            }
            for (int[] t : trees) {
                if (t[0] < c0 - 1 || t[0] > c1 + 1 || t[1] < r0 - 1 || t[1] > r1 + 1) continue;
                int baseY = (t[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawTreeTrunk(g2, offX, offY, t[0], t[1]));
            }
            for (int[] f : fences) {
                if (f[0] < c0 - 1 || f[0] > c1 + 1 || f[1] < r0 - 1 || f[1] > r1 + 1) continue;
                int baseY = (f[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawFence(g2, offX, offY, f[0], f[1]));
            }
            for (int[] w : wells) {
                if (w[0] < c0 - 1 || w[0] > c1 + 1 || w[1] < r0 - 1 || w[1] > r1 + 1) continue;
                int baseY = (w[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawWell(g2, offX, offY, w[0], w[1]));
            }
            for (int[] b : bushes) {
                if (b[0] < c0 - 1 || b[0] > c1 + 1 || b[1] < r0 - 1 || b[1] > r1 + 1) continue;
                int baseY = (b[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawBush(g2, offX, offY, b[0], b[1]));
            }
            for (int[] f : fountains) {
                if (f[0] < c0 - 1 || f[0] > c1 + 1 || f[1] < r0 - 1 || f[1] > r1 + 1) continue;
                int baseY = (f[1] + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawFountainBase(g2, offX, offY, f[0], f[1]));
            }
            for (Npc n : npcs) {
                int baseY = (int) (n.renderCy + 13);
                order.computeIfAbsent(baseY * 4 + 3, k -> new java.util.ArrayList<>())
                        .add(() -> drawNpc(g2, offX, offY, n));
            }
            int playerBase = (int) (renderCy + 13);
            order.computeIfAbsent(playerBase * 4 + 3, k -> new java.util.ArrayList<>())
                    .add(() -> drawPlayer(g2, offX, offY));
            for (java.util.List<Runnable> batch : order.values()) {
                for (Runnable d : batch) d.run();
            }

            // Layer 3 overhead (Dawn of Souls): wall face strip, tree canopies, roofs, fountain upper.
            // Position-independent: redraws only overhead bands after the player.
            drawWallForeground(g2, offX, offY, c0, r0, c1, r1);
            drawTreeCanopyPass(g2, offX, offY, c0, r0, c1, r1);
            drawRoofForeground(g2, offX, offY, c0, r0, c1, r1);
            drawFountainUpperPass(g2, offX, offY, c0, r0, c1, r1);

            if (debug) drawDebug(g2, offX, offY, viewW, viewH, c0, r0, c1, r1);
            drawHud(g2, viewW, viewH);
            drawFade(g2, viewW, viewH);
        } finally {
            if (oldInterp != null) g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
            if (oldAA != null) g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }

    /**
     * Inn interior render: Layer 1 blue floor, Layer 2 Y-sorted walls/counter/
     * beds/innkeeper/player (gray walls with white caps, white counter top —
     * the Y-sort alone puts the counter in front of whoever stands behind it).
     */
    private void drawInterior(Graphics2D g2, int viewW, int viewH) {
        updateCameraInn(viewW, viewH);
        g2.setColor(GamePalette.ROYAL_DEEP);
        g2.fillRect(0, 0, viewW, viewH);
        g2.setClip(0, 0, viewW, viewH);
        int offX = 0;
        int offY = 0;
        int s = TILE * SCALE;

        // Layer 1: floor.
        for (int r = 0; r < INN_INT_H; r++) {
            for (int c = 0; c < INN_INT_W; c++) {
                int x = offX + (c * TILE - camX) * SCALE;
                int y = offY + (r * TILE - camY) * SCALE;
                g2.setColor(GamePalette.BLUE_FLOOR);
                g2.fillRect(x, y, s, s);
            }
        }

        // Layer 2: Y-sorted tiles + figures.
        java.util.TreeMap<Integer, java.util.List<Runnable>> order = new java.util.TreeMap<>();
        for (int r = 0; r < INN_INT_H; r++) {
            for (int c = 0; c < INN_INT_W; c++) {
                int kind = innWalls[c][r];
                if (kind == 0) continue;
                final int cc = c;
                final int rr = r;
                int baseY = (r + 1) * TILE;
                order.computeIfAbsent(baseY * 4 + 2, k -> new java.util.ArrayList<>())
                        .add(() -> drawInnCell(g2, offX, offY, cc, rr, kind));
            }
        }
        if (innkeeper != null) {
            int baseY = (int) (innkeeper.renderCy + 13);
            order.computeIfAbsent(baseY * 4 + 3, k -> new java.util.ArrayList<>())
                    .add(() -> drawNpc(g2, offX, offY, innkeeper));
        }
        int playerBase = (int) (renderCy + 13);
        order.computeIfAbsent(playerBase * 4 + 3, k -> new java.util.ArrayList<>())
                .add(() -> drawPlayer(g2, offX, offY));
        for (java.util.List<Runnable> batch : order.values()) {
            for (Runnable d : batch) d.run();
        }

        drawHud(g2, viewW, viewH);
    }

    /** One interior cell: 1=wall, 2=counter, 3=bed, 4=doorway. */
    private void drawInnCell(Graphics2D g2, int offX, int offY, int c, int r, int kind) {
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        switch (kind) {
            case 1 -> { // wall: gray body, white cap band
                g2.setColor(GamePalette.GRAY_WALL);
                g2.fillRect(x, y, s, s);
                g2.setColor(GamePalette.WHITE_TOP);
                g2.fillRect(x, y, s, Math.max(4, s / 3));
            }
            case 2 -> { // counter: dark face, white top band
                g2.setColor(GamePalette.DARK_GRAY);
                g2.fillRect(x, y, s, s);
                g2.setColor(GamePalette.WHITE_TOP);
                g2.fillRect(x, y, s, Math.max(4, s * 2 / 5));
            }
            case 3 -> { // bed: red mattress, pale pillow at the head
                g2.setColor(GamePalette.RED_BED);
                g2.fillRect(x + 1, y, s - 2, s);
                g2.setColor(GamePalette.PARCHMENT);
                g2.fillRect(x + 3, y + 2, s - 6, Math.max(4, s / 4));
            }
            default -> { // doorway: dark opening in the south wall band
                g2.setColor(GamePalette.GRAY_WALL);
                g2.fillRect(x, y, s, s);
                g2.setColor(GamePalette.ROYAL_DEEP);
                g2.fillRect(x + s / 4, y + s / 3, s / 2, s * 2 / 3);
            }
        }
    }

    /** The 12x8 room fits the viewport: center it on royal deep letterboxing. */
    private void updateCameraInn(int viewW, int viewH) {
        int viewTilesW = (viewW + TILE * SCALE - 1) / (TILE * SCALE);
        int viewTilesH = (viewH + TILE * SCALE - 1) / (TILE * SCALE);
        camX = (INN_INT_W - viewTilesW) * TILE / 2;
        camY = (INN_INT_H - viewTilesH) * TILE / 2;
    }

    /** Full-screen fade veil for scene changes. */
    private void drawFade(Graphics2D g2, int viewW, int viewH) {
        if (fadeAlpha <= 0f) return;
        java.awt.Composite old = g2.getComposite();
        g2.setComposite(java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, Math.min(1f, fadeAlpha)));
        g2.setColor(GamePalette.ROYAL_DEEP);
        g2.fillRect(0, 0, viewW, viewH);
        g2.setComposite(old);
    }

    private void paintGround(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        // Bridges render the same cobble as paths (walkable river crossings).
        if (isBridge(c, r)) {
            g2.drawImage(pathTile, x, y, s, s, null);
            return;
        }
        switch (ground[c][r]) {
            case GRASS, TREE -> g2.drawImage(grassTile, x, y, s, s, null);
            case PATH, ROAD, GATE, SQUARE -> g2.drawImage(pathTile, x, y, s, s, null);
            case WATER -> g2.drawImage(waterTile, x, y, s, s, null);
        }
    }

    private boolean isBridge(int c, int r) {
        for (int[] b : bridges) {
            if (b[0] == c && b[1] == r) return true;
        }
        return false;
    }

    private boolean isPathKind(int c, int r) {
        if (!isInsideMap(c, r)) return false;
        Ground g = ground[c][r];
        return g == Ground.PATH || g == Ground.ROAD || g == Ground.SQUARE || g == Ground.GATE;
    }

    /** Layer 2 fountain base: solid, Y-sorted with player/NPCs. */
    private void drawFountainBase(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(fountainBase, x, y, s, s, null);
    }

    /** Layer 3 fountain upper: PASSABLE transparent overlay, drawn after the player. */
    private void drawFountainUpper(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(fountainUpper, x, y, s, s, null);
    }

    /** Layer 3 fountain upper pass: player walks under (16,20) and appears behind it. */    private void drawFountainUpperPass(Graphics2D g2, int offX, int offY,
                                       int c0, int r0, int c1, int r1) {
        for (int[] f : fountainUppers) {
            if (f[0] < c0 - 1 || f[0] > c1 + 1 || f[1] < r0 - 1 || f[1] > r1 + 1) continue;
            drawFountainUpper(g2, offX, offY, f[0], f[1]);
        }
    }

    /** Which building owns the door at (c, r), or null. */
    private Building buildingDoorAt(int c, int r) {
        for (Building b : buildings) {
            if (c == b.doorX && r == b.doorY) return b;
        }
        return null;
    }

    /**
     * Bump a shut door: locked shows its blurb, else it swings open (any other
     * shuts) and the tap is consumed. Returns true when a shut door ate the tap.
     */
    private boolean bumpDoor(int tc, int tr) {
        Building shut = buildingDoorAt(tc, tr);
        if (shut == null || shut == openDoor) return false;
        if (shut.locked) {
            message = shut.blurb;
            msgTimer = MSG_TICKS;
            return true;
        }
        openDoorAt(shut, tc, tr);
        return true;
    }

    /** Swing a door open: any other shuts, solidity clears, latch arms. */
    private void openDoorAt(Building b, int tc, int tr) {
        if (openDoor != null) shutDoor(openDoor);
        openDoor = b;
        solid[tc][tr] = false;
        doorLatch = true; // this press only opens — release + click again to enter
        message = "The door creaks open.";
        msgTimer = MSG_TICKS;
        AudioManager.playEffect("door");
    }

    /** Shut a door: collision back on, open reference cleared if it was open. */
    private void shutDoor(Building b) {
        solid[b.doorX][b.doorY] = true;
        if (openDoor == b) openDoor = null;
    }

    private void drawBuilding(Graphics2D g2, int offX, int offY, Building b) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (b.tx * TILE - camX) * SCALE;
        int y = offY + (b.ty * TILE - camY) * SCALE;
        int w = b.tw * s;
        // Open sprite follows the door state: visible from the opening click
        // (tap 1, player still outside) until it shuts behind you.
        boolean doorway = openDoor == b;

        // Per-cell sprite facade: single roof row, upper window/plain rows,
        // ground base row (lower-level variants) with the door cell.
        for (int ly = 0; ly < b.th; ly++) {
            for (int lx = 0; lx < b.tw; lx++) {
                int cx = offX + ((b.tx + lx) * TILE - camX) * SCALE;
                int cy = offY + ((b.ty + ly) * TILE - camY) * SCALE;
                BufferedImage img;
                if (ly == 0) {
                    img = houseRoof;
                } else if (ly < b.th - 1) {
                    int upperRow = ly - 1; // 0-based among upper rows
                    img = upperHasWindow(b, upperRow, lx) ? houseWallWindow : houseWallPlain;
                } else {
                    if (b.tx + lx == b.doorX) {
                        img = doorway ? houseDoorOpen : houseDoorClosed;
                    } else {
                        img = baseHasWindow(b, lx) ? houseBaseWindow : houseBasePlain;
                    }
                }
                g2.drawImage(img, cx, cy, s, s, null);
            }
        }
        // Name label.
        g2.setColor(VillagePalette.WALL);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        int labelW = g2.getFontMetrics().stringWidth(b.name);
        g2.drawString(b.name, x + (w - labelW) / 2, y - 4);
    }

    /** Layer 2 trunk: solid base. */
    private void drawTreeTrunk(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(treeBaseTile, x, y, s, s, null);
    }

    private void drawFence(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(fenceTile, x, y, s, s, null);
    }

    private void drawWell(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(wellTile, x, y, s, s, null);
    }

    private void drawBush(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(bushTile, x, y, s, s, null);
    }

    /** Layer 3 canopy: PASSABLE visual overlay, drawn after the player. */
    private void drawTreeCanopy(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(treeUpperTile, x, y, s, s, null);
    }

    /** Top-wall face: front of the FF1 horizontal wall, solid — blocks movement. */
    private void drawWallBrick(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        if (isSewer(c, r)) {
            g2.drawImage(sewerTile, x, y, s, s, null);
            return;
        }
        g2.drawImage(wallBrickTile, x, y, s, s, null);
    }

    /** Wall cap: drawn from art, solid — blocks movement. Caps run continuous (ttttt). */
    private void drawWallTop(Graphics2D g2, int offX, int offY, int c, int r) {
        ensureSprites();
        int s = TILE * SCALE;
        int x = offX + (c * TILE - camX) * SCALE;
        int y = offY + (r * TILE - camY) * SCALE;
        g2.drawImage(wallTopTile, x, y, s, s, null);
    }

    private boolean isSewer(int c, int r) {
        for (int[] sw : sewers) {
            if (sw[0] == c && sw[1] == r) return true;
        }
        return false;
    }

    /**
     * FF1 foreground occlusion: after the player is drawn, cover the player's head
     * with the wall's lower face when standing directly south of the top wall.
     * Base tiles were already drawn before the player; this strip creates the
     * behind-the-wall effect without changing collision, camera, or Y-sort.
     * Front-only: only top row-1 bricks participate, never side caps.
     */
    private void drawWallForeground(Graphics2D g2, int offX, int offY,
                                    int c0, int r0, int c1, int r1) {
        ensureSprites();
        int s = TILE * SCALE;
        int strip = Math.max(8, s / 3); // ~10px at s=32: overlaps head, not whole body
        java.awt.Shape oldClip = g2.getClip();
        try {
            for (int[] w : wallBodies) {
                int c = w[0], r = w[1];
                if (c < c0 - 1 || c > c1 + 1 || r < r0 - 1 || r > r1 + 1) continue;
                int x = offX + (c * TILE - camX) * SCALE;
                int y = offY + (r * TILE - camY) * SCALE;
                // Only the lower face overlaps a south-adjacent player; clip to it.
                g2.setClip(x, y + s - strip, s, strip);
                g2.drawImage(wallBrickTile, x, y, s, s, null);
            }
            for (int[] w : sewers) {
                int c = w[0], r = w[1];
                if (c < c0 - 1 || c > c1 + 1 || r < r0 - 1 || r > r1 + 1) continue;
                int x = offX + (c * TILE - camX) * SCALE;
                int y = offY + (r * TILE - camY) * SCALE;
                g2.setClip(x, y + s - strip, s, strip);
                g2.drawImage(sewerTile, x, y, s, s, null);
            }
        } finally {
            g2.setClip(oldClip);
        }
    }

    /** Layer 3 tree canopy pass: draws only canopy tops after the player. */
    private void drawTreeCanopyPass(Graphics2D g2, int offX, int offY,
                                    int c0, int r0, int c1, int r1) {
        for (int[] t : treeUppers) {
            if (t[0] < c0 - 1 || t[0] > c1 + 1 || t[1] < r0 - 1 || t[1] > r1 + 1) continue;
            drawTreeCanopy(g2, offX, offY, t[0], t[1]);
        }
    }

    /**
     * Layer 3 roofs: Y-sort already draws a building after a player standing north
     * (Sprite_Y &lt; Roof_Y), but re-stamp the roof band here so the overhead edge
     * always covers a head walking behind it, per Dawn of Souls Layer 3.
     */
    private void drawRoofForeground(Graphics2D g2, int offX, int offY,
                                    int c0, int r0, int c1, int r1) {
        int s = TILE * SCALE;
        java.awt.Shape oldClip = g2.getClip();
        try {
            for (Building b : buildings) {
                if (b.tx + b.tw < c0 - 1 || b.tx > c1 + 1 || b.ty > r1 + 1 || b.ty + b.th < r0 - 1) continue;
                int roofRows = 1; // single straight roof line for every building
                int x = offX + (b.tx * TILE - camX) * SCALE;
                int y = offY + (b.ty * TILE - camY) * SCALE;
                g2.setClip(x, y, b.tw * s, roofRows * s);
                drawBuilding(g2, offX, offY, b);
            }
        } finally {
            g2.setClip(oldClip);
        }
    }

    private void drawNpc(Graphics2D g2, int offX, int offY, Npc n) {
        int x = (int) (offX + (n.renderCx - camX) * SCALE);
        int y = (int) (offY + (n.renderCy - camY) * SCALE);
        int swing = (n.stepping && (n.stepT / 3) % 2 == 0) ? 1 : 0;
        g2.setColor(VillagePalette.OUTLINE_DARK);
        g2.fillRect(x - 6, y + 6, 5, 7 - swing);
        g2.fillRect(x + 1, y + 6, 5, 7 + swing);
        g2.setColor(VillagePalette.SKIN);
        g2.fillOval(x - 6, y - 19, 12, 12);
        g2.setColor(n.clothes);
        g2.fillRect(x - 9, y - 7, 18, 13);
        g2.setColor(VillagePalette.OUTLINE_DARK);
        g2.drawRect(x - 9, y - 7, 17, 12);
        // Facing tick, same language as the player's nose dot.
        int nx = x, ny = y - 13;
        switch (n.facing) {
            case UP -> ny -= 8;
            case DOWN -> ny += 8;
            case LEFT -> nx -= 12;
            case RIGHT -> nx += 12;
        }
        g2.fillRect(nx - 1, ny - 1, 2, 2);
        // White name tag with a dark shadow so it reads on any ground.
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g2.setColor(VillagePalette.OUTLINE_DARK);
        g2.drawString(n.name, x - 9, y - 21);
        g2.setColor(VillagePalette.OUTLINE_OFFWHITE);
        g2.drawString(n.name, x - 10, y - 22);
    }

    /** 11. Four directions, walk frames. Figure fills one cell (~32px tall). */
    private void drawPlayer(Graphics2D g2, int offX, int offY) {
        int x = (int) (offX + (renderCx - camX) * SCALE);
        int y = (int) (offY + (renderCy - camY) * SCALE);
        int swing = 0;
        if (stepping) {
            swing = (walkFrame == 0) ? -3 : 3;
            g2.setColor(VillagePalette.OUTLINE_DARK);
            g2.fillRect(x - 6, y + 6, 5, 7 + swing);
            g2.fillRect(x + 1, y + 6, 5, 7 - swing);
        } else {
            g2.setColor(VillagePalette.OUTLINE_DARK);
            g2.fillRect(x - 6, y + 6, 5, 7);
            g2.fillRect(x + 1, y + 6, 5, 7);
        }
        g2.setColor(VillagePalette.SKIN);
        g2.fillOval(x - 6, y - 19, 12, 12);
        g2.setColor(VillagePalette.PLAYER_TUNIC);
        g2.fillRect(x - 9, y - 7, 18, 13);
        g2.setColor(VillagePalette.OUTLINE_OFFWHITE);
        g2.drawRect(x - 9, y - 7, 17, 12);
        int nx = x, ny = y - 13;
        switch (facing) {
            case UP -> ny -= 8;
            case DOWN -> ny += 8;
            case LEFT -> nx -= 12;
            case RIGHT -> nx += 12;
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
        if (dialogue.isOpen() && !message.isEmpty()) {
            int panelW = Math.min(viewW - 40, 760);
            int panelH = 96;
            int px = (viewW - panelW) / 2;
            int py = viewH - panelH - 16;
            int arc = 18;
            // Body (parchment) with royal blue border.
            g2.setColor(GamePalette.PARCHMENT);
            g2.fillRoundRect(px, py, panelW, panelH, arc, arc);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRoundRect(px, py, panelW, panelH, arc, arc);
            // Portrait placeholder box on the left.
            int portSize = panelH - 16;
            int portX = px + 12;
            int portY = py + 8;
            g2.setColor(GamePalette.ARCANE_BLUE);
            g2.fillRoundRect(portX, portY, portSize, portSize, 10, 10);
            g2.setColor(GamePalette.ROYAL_BLUE);
            g2.drawRoundRect(portX, portY, portSize, portSize, 10, 10);
            // Text area.
            int textX = portX + portSize + 12;
            int textW = panelW - (textX - px) - 12;
            g2.setColor(GamePalette.ROYAL_DEEP);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 15));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int lineH = fm.getHeight();
            int ty = py + fm.getAscent() + 6;
            // Wrap message into lines.
            java.util.List<String> lines = new java.util.ArrayList<>();
            StringBuilder cur = new StringBuilder();
            for (String word : message.split(" ")) {
                String test = cur.length() == 0 ? word : cur + " " + word;
                if (fm.stringWidth(test) <= textW) {
                    cur = new StringBuilder(test);
                } else {
                    if (cur.length() > 0) lines.add(cur.toString());
                    cur = new StringBuilder(word);
                }
            }
            if (cur.length() > 0) lines.add(cur.toString());
            int maxLines = (panelH - 12) / lineH;
            for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
                g2.drawString(lines.get(i), textX, ty + i * lineH);
            }
            String hint = dialogue.hint();
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            g2.setColor(GamePalette.ROYAL_BLUE);
            int hw = g2.getFontMetrics().stringWidth(hint);
            g2.drawString(hint, px + panelW - hw - 12, py + panelH - 8);
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
                    case WATER -> '~';
                };
            }
        }
        for (Building b : buildings) {
            char mark = switch (b.name) {
                case "INN" -> 'I';
                case "TAVERN" -> 'V';
                case "MENTOR" -> 'E';
                case "BLACKSMITH" -> 'W';
                case "MARKET" -> 'i';
                case "WIZARD" -> 'M';
                case "SANCTUARY" -> 'U';
                default -> 'H';
            };
            for (int y = b.ty; y < b.ty + b.th; y++) {
                for (int x = b.tx; x < b.tx + b.tw; x++) {
                    pic[y][x] = mark;
                }
            }
            pic[b.doorY][b.doorX] = 'D';
            pic[b.enterY][b.enterX] = 'd';
        }
        for (int[] f : fountains) pic[f[1]][f[0]] = 'F';
        for (int[] f : fountainUppers) pic[f[1]][f[0]] = 'U';
        for (int[] w : wallBodies) pic[w[1]][w[0]] = 'w';
        for (int[] w : wallTops) pic[w[1]][w[0]] = 'W';
        for (int[] sw : sewers) pic[sw[1]][sw[0]] = 'S';
        for (int[] b : bridges) pic[b[1]][b[0]] = '=';
        for (int[] w : wells) pic[w[1]][w[0]] = 'O';
        for (int[] f : fences) if (pic[f[1]][f[0]] == '.' || pic[f[1]][f[0]] == 'p') pic[f[1]][f[0]] = 'f';
        for (int[] b : bushes) if (pic[b[1]][b[0]] == '.' || pic[b[1]][b[0]] == 'p') pic[b[1]][b[0]] = 'b';
        for (int[] w : invisibleBounds) {
            if (pic[w[1]][w[0]] == '.' || pic[w[1]][w[0]] == 'p'
                    || pic[w[1]][w[0]] == 'r' || pic[w[1]][w[0]] == 's') {
                pic[w[1]][w[0]] = 'x';
            }
        }
        for (Npc n : npcs) pic[n.row][n.col] = 'N';
        pic[SPAWN_R][SPAWN_C] = 'P';
        System.out.println("MAP " + MAP_W + "x" + MAP_H + " (col 0.." + (MAP_W - 1) + "):");
        for (int y = 0; y < MAP_H; y++) {
            System.out.println(String.valueOf(pic[y]));
        }
        System.out.println("Legend: .=grass p=path G=gate ~=water T=tree F=fountain-base U=fountain-upper W=wallTop w=wallBrick S=sewer f=fence b=bush O=well ==bridge x=invisibleBound");
        System.out.println("I=inn V=tavern E=mentor W=blacksmith i=market H=house M=wizard U=sanctuary D=door d=entrance N=npc P=spawn");
        int treeCount = trees.size();
        System.out.println("Trees: " + treeCount + " WallTops: " + wallTops.size()
                + " WallBricks: " + wallBodies.size() + " Bounds: " + invisibleBounds.size()
                + " Buildings: " + buildings.size() + " NPCs: " + npcs.size()
                + " Bridges: " + bridges.size() + " Fountains: " + fountains.size()
                + "+" + fountainUppers.size()
                + " Sewers: " + sewers.size());
    }
}
