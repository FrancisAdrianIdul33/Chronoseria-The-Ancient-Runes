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

## 3. Walls — T-shaped perimeter, caps wrap, faces front-only

Implementer: `VillageScene.java` §7 — `buildTopWall()`, `buildSideCaps()`,
`buildBarBottom()`, `buildStemWalls()`, `buildFrontWall()` write a `wallMask`,
then `stampWallsDynamic()` resolves it: mask cell with wall south of it becomes
cap `T`, otherwise south-facing face `W`. T-junctions resolve cap-to-cap
automatically.

- Top bar: `row 0, x0..31 = wallTop`, solid; `row 1, x1..30 = wallBrick` with
  corners `(0,1)/(31,1) = wallTop`, solid.
- Bar sides: `col 0 / col 31, y2..12 = wallTop`, solid. No brick on sides.
- Bar-bottom band: `row 12, x0..10 + x21..31 = wallTop` (inner corners
  `(10,12)/(21,12)` close the junction mouth); `row 13` same outer spans =
  `wallBrick` plus `x10/x21 = wallTop` junction caps. Opening `x11..20` on both
  rows connects bar to stem.
- Stem: `col 10 / col 21, y14..29 = wallTop`, solid. Interior `x11..20`.
- Front wall, top-to-bottom = cap then face: `row 30, x10..21 except gate =
  wallTop` first layer, solid; `row 31, same span = wallBrick` last layer,
  solid, corners wrapped in brick for 3D thickness. Gate `x15` stays `GATE`
  walkable on both rows.
- Ground under every wall cell stays `GRASS` so seams sit on grass, not gaps.
  The base fill is all-grass first, so there is grass inside + outside the T.

Corner rule for future stamps:

```text
IF cell == BRICK_FRONT at (x, y):
  Place WALL_TOP at (x, y-1)
  IF (x-1, y) is not BRICK_FRONT: (x, y) is the left end; (x-1, y) stays background
  IF (x+1, y) is not BRICK_FRONT: (x, y) is the right end; (x+1, y) stays background
  Never stamp BRICK on a side column
```

Counts: `wallTops = 118`, `wallBodies = 60`, `bounds = 0`.

## 4. Ground, paths, gate, spawn

- Base: entire map starts `GRASS` (grass inside + outside the T).
- Spine `col 15, rows 8-30` (1-line, unbroken through the junction);
  north lane `row 8, cols 3-28`; west plaza court `cols 3-7, rows 9-11`.
- Gate floor: `(15,30),(15,31) = GATE` (1-wide), always walkable.
- Spawn: `(15,29)`, facing up.

## 5. Trees, grass, fountain, fences

- Grass: `grass.png` tiled on `GRASS` and under `TREE` cells; procedural checker is fallback only.
- Trees: bar edges `col 2 / col 29 rows 3-11`, `row 2 x5-6/x19-20`, bar-south
  `(27-28,9)`; stem flanks `(11,18),(11,23),(20,16),(20,23),(13,27),(18,27)`;
  outside backdrop `(3,16),(4,16),(3,20),(5,24),(27,16),(28,20),(26,24),(4,27),(28,27)`.
  `solid = true`, `ground = TREE`. `tree.png` split: bottom half trunk in Layer 2,
  top half canopy in Layer 3 overlay.
- Fountain: `(19,17)` 2x2, solid, east court off the spine.
- Fences: `(14,23),(14,24),(16,23),(16,24)`, solid Layer 2.

## 6. Buildings and doors

- 11 buildings, bottom-center door on facade row, entrance cell directly south. `E` works only on entrance facing up.
- Roster: INN `(3,3)` 5x4; HOUSE `(10,4)`; WEAPON `(17,4)`; ARMOR `(24,4)`;
  HOUSE `(11,10)`; ITEM `(17,10)`; MAGIC `(11,14)`; HOUSE `(11,19),(11,24)`;
  HOUSE `(17,14),(17,19)` — all 3x3 except INN. Junction houses sit at `x11-13`
  / `x17-19` leaving grass buffers `x14/x16` so the spine never touches a building.
- Paths never touch buildings except via entrance cells.
- NPCs: MIRA `(14,19)`, Bram `(16,12)`, ODD `(16,13)`, Pip `(14,28)`, Sol `(9,7)` — solid, never on doors/trees/paths.

## 7. Collision quick matrix

| Block | Layer | Solid | Notes |
| --- | --- | --- | --- |
| Grass/path/road/plaza/gate | 1 | Passable | Walking layer |
| Wall Top cap | 2 | Impassable | Top row + corners + bar sides + bar-bottom `row 12` + stem sides + front `row 30` |
| Wall brick face | 2 (+3 strip) | Impassable | Top `row 1 x1..30` + bar-bottom `row 13` outer spans + front `row 31` stem span minus gate |
| Invisible bound | — | Impassable | Reserved; none currently placed (south is now visible wall) |
| Tree trunk | 2 | Impassable | Base cell blocks |
| Tree canopy | 3 | Passable overlay | Visual only, covers head behind tree |
| Building body/door frame | 2 | Impassable | Door cell itself walkable |
| Roof | 3 | Overlay | Covers player walking behind |
| Fence/fountain/NPC | 2 | Impassable | Grouped blockers |

## 8. Validation (never remove)

`VillageScene` asserts at build time: doors reachable by flood fill from spawn; path gap; door-on-facade; inside map; no overlap; `wallBrick` only on top `row 1 x1..30`, bar-bottom `row 13` outer spans, and front `row 31` stem span minus gate; caps continuous along the T polygon; stem/junction openings clear; every structure inside the T and off walls; gate walkable; exactly 11 doors.

## 9. Adding a new block

1. Add 32x32 PNG to both `assets/` and `src/main/resources/assets/tiles/`.
2. Load in `ensureSprites()` via `loadTile("/assets/tiles/<name>.png")` with null fallback.
3. Stamp with `wallTop / wallBrick / tree / invisibleBound` or `fillGround`; set `solid` to match §7.
4. Draw in the correct pass: ground → Y-sorted Layer 2 → Layer 3 overhead after player.
5. Extend validation counts if the block is structural.

## 10. Standards & principles — follow these every time a map is generated

1. Tiles are 32x32, one tile per cell, never stretched and never placed off-grid.
   Paint the ground base first (grass / floor), then stamp walls and structures over it.
2. Render in three passes: Layer 1 ground, Layer 2 Y-sorted fringe, Layer 3
   overhead after the player (face strips, canopies, roofs). Collision lives only
   in `solid[][]`; visuals never block movement.
3. Every brick face `W` has a cap `T` directly above it (`T`-over-`W`). Faces sit
   on south-facing edges only; sides and corners are caps. Junctions join
   cap-to-cap. Never hand-place `T`/`W` — add footprint cells to `wallMask` and
   let `stampWallsDynamic()` resolve them by south-neighbor check.
4. Every opening (gate, door gap, corridor mouth) is flanked by caps, and inner
   T corners get explicit corner caps so no grass notch shows at the turn.
   Gates and doors are 1-wide unless the design doc says otherwise.
5. Paths are 1-line spines and lanes with a single-tile gate. Where a road passes
   a building, keep one cell of plain ground as a buffer on each side so the road
   never touches a footprint except on a registered entrance cell.
6. Buildings sit on walkable ground clear of walls, with bottom-center doors and
   a walkable entrance cell directly south; `E` works only on the entrance facing
   up. Trees split trunk (Layer 2, solid) / canopy (Layer 3, overlay). Props and
   NPCs sit on walkable ground, never on paths, doors, entrances, or walls.
7. A map ships only when every build-time assert passes: flood-fill reachability
   from spawn/entry, path gap, door-on-facade, inside-map, no-overlap,
   face-placement, `T`-over-`W`, containment off walls, walkable gate, exact
   interactable count. Do not weaken an assert to make a layout fit — fix the layout.
8. Rooms and dungeons reuse this stamper and these validators; only the ground
   and wall art swap. Do not duplicate wall logic per map — extend the shared
   `wallTop / wallBrick / wallMask / stampWallsDynamic` helpers instead.
