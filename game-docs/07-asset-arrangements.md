# Chronoseria: Asset Arrangements — Block Placement

Game design document · How village tiles and structures are placed on the grid.

Source of truth for tile placement. Building purposes live in `01-village-buildings.md`. Systems live in `00-gameplay-improvements.md`.

## 1. Sources and sizes

| Art | Source upload | Runtime copy | Project copy |
| --- | --- | --- | --- |
| Grass | `Pictures/Chronoseria Assets/Grass.png` | `src/main/resources/assets/tiles/grass.png` | `assets/grass.png` |
| Tree | `Pictures/Chronoseria Assets/Tree.png` | `src/main/resources/assets/tiles/tree.png` | `assets/tree.png` |
| Wall brick (front face) | `Pictures/Chronoseria Assets/Wall brick.png` | `src/main/resources/assets/tiles/wall_brick.png` | `assets/wall_brick.png` |
| Wall Top (cap) | `Pictures/Chronoseria Assets/Wall Top.png` | `src/main/resources/assets/tiles/wall_top.png` | `assets/wall_top.png` |

Rules:

- All four are 32x32. One image = one cell, drawn 1:1 to `s = TILE * SCALE`.
- Grid: `TILE = 16`, `MAP_W = MAP_H = 32`, `SCALE = 2` (`VillageScene.java`).
- Never stretch a tile across the map. Never place off-grid.
- Keep uploads in Pictures untouched; runtime loads from `src/main/resources` via `AssetLoader`.

## 2. Layer model (Dawn-of-Souls style)

| Layer | Contents | Code |
| --- | --- | --- |
| Layer 1 ground | Grass, path, road, plaza, gate | `paintGround()` |
| Layer 2 fringe | Wall caps/faces, tree trunks, fences, fountain, buildings base, doors, NPC blockers | Y-sorted `draw()` pass |
| Layer 3 overhead | Wall face lower strip, tree canopies, building roofs | `drawWallForeground()`, `drawTreeCanopyPass()`, `drawRoofForeground()` after player |

Collision is a separate bitmask: `solid[x][y]`. Visuals never decide movement; `startStep()` blocks on `solid`.

## 3. Walls — front-only brick, caps wrap

Implementer: `VillageScene.java` §7 — `buildTopWall()`, `buildSideCaps()`, `buildFrontWall()`.

- Top cap row: `row 0, x0..31 = wallTop`, solid.
- Top face row: `row 1, x1..30 = wallBrick`, solid. Top brick appears only here.
- Top corners: `(0,1)` and `(31,1) = wallTop`, solid. These terminate the top brick row so cap meets cap with no T-joint.
- Sides: `col 0 / col 31, y2..29 = wallTop`, solid. No brick on sides. Inner `col 1 / col 30` stay walkable grass/path.
- Front wall, top-to-bottom = cap then face: `row 30, x0..31 except gate = wallTop` first layer, solid; `row 31, x0..31 except gate = wallBrick` last layer, solid. Front corners `(0,31)/(31,31) = wallBrick` so the face wraps for 3D thickness. Gate `x15-16` stays `GATE` walkable on both rows.
- Ground under every wall cell stays `GRASS` so seams sit on grass, not gaps.

Corner rule for future stamps:

```text
IF cell == BRICK_FRONT at (x, y):
  Place WALL_TOP at (x, y-1)
  IF (x-1, y) is not BRICK_FRONT: (x, y) is the left end; (x-1, y) stays background
  IF (x+1, y) is not BRICK_FRONT: (x, y) is the right end; (x+1, y) stays background
  Never stamp BRICK on a side column
```

Counts: `wallTops = 120` (32 top + 2 top corners + 56 sides + 30 front caps), `wallBodies = 60` (30 top + 30 front), `bounds = 0`.

## 4. Ground, paths, gate, spawn

- Base: entire map starts `GRASS`.
- Main road `cols 15-16, rows 8-30`; north lane `rows 8-9, cols 3-28`; south lane `rows 26-27, cols 3-28`; plaza `cols 11-20, rows 13-18`; spurs `x18 rows 12-14`, `row 17 cols 4-10`, `row 17 cols 21-25`; east connector `x30 rows 20-26` (kept walkable — do not block `col 30`).
- Gate floor: `(15,30),(16,30),(15,31),(16,31) = GATE`, always walkable.
- Spawn: `(15,29)`, facing up.

## 5. Trees, grass, fountain, fences

- Grass: `grass.png` tiled on `GRASS` and under `TREE` cells; procedural checker is fallback only.
- Trees: fixed lists only — corner forests `x22-29/y3-7`, `x2-12/y3-7`, blocks `x2-6` and `x24-29`, groups `(12,10),(13,10),(12,11)`, `(24,19),(25,19),(25,20)`, `(5,20),(6,20),(6,21)`. `solid = true`, `ground = TREE`. `tree.png` split: bottom half trunk in Layer 2, top half canopy in Layer 3 overlay.
- Fountain: `(15,15)` 2x2, solid, inside plaza.
- Fences: `(10,20),(11,20),(29,22),(29,23)`, solid Layer 2.

## 6. Buildings and doors

- 11 buildings, bottom-center door on facade row, entrance cell directly south. `E` works only on entrance facing up.
- Roster: INN `(3,3)` 5x4; HOUSE `(10,4)`; WEAPON `(17,4)`; ARMOR `(24,4)`; ITEM `(3,13)`; HOUSE `(7,13)`; BIG `(23,13)`; MAGIC `(7,22)`; HOUSE `(11,22),(18,22),(22,22)` — all 3x3 except INN.
- Paths never touch buildings except via entrance cells.
- NPCs: MIRA `(14,19)`, Bram `(21,12)`, ODD `(17,12)`, Pip `(22,28)`, Sol `(9,7)` — solid, never on doors/trees/paths.

## 7. Collision quick matrix

| Block | Layer | Solid | Notes |
| --- | --- | --- | --- |
| Grass/path/road/plaza/gate | 1 | Passable | Walking layer |
| Wall Top cap | 2 | Impassable | Top row + corners + sides + front `row 30` |
| Wall brick face | 2 (+3 strip) | Impassable | Top `row 1 x1..30` + front `row 31` edge-to-edge minus gate |
| Invisible bound | — | Impassable | Reserved; none currently placed (south is now visible wall) |
| Tree trunk | 2 | Impassable | Base cell blocks |
| Tree canopy | 3 | Passable overlay | Visual only, covers head behind tree |
| Building body/door frame | 2 | Impassable | Door cell itself walkable |
| Roof | 3 | Overlay | Covers player walking behind |
| Fence/fountain/NPC | 2 | Impassable | Grouped blockers |

## 8. Validation (never remove)

`VillageScene` asserts at build time: doors reachable by flood fill from spawn; path gap; door-on-facade; inside map; no overlap; `wallBrick` only on top `row 1 x1..30` and front `row 31` minus gate; top/front caps continuous; side caps present; gate walkable; exactly 11 doors.

## 9. Adding a new block

1. Add 32x32 PNG to both `assets/` and `src/main/resources/assets/tiles/`.
2. Load in `ensureSprites()` via `loadTile("/assets/tiles/<name>.png")` with null fallback.
3. Stamp with `wallTop / wallBrick / tree / invisibleBound` or `fillGround`; set `solid` to match §7.
4. Draw in the correct pass: ground → Y-sorted Layer 2 → Layer 3 overhead after player.
5. Extend validation counts if the block is structural.
