# Chronoseria: Asset Arrangements — Block Placement

Game design document · How village tiles and structures are placed on the grid.

Source of truth for tile placement. Building purposes live in `01-village-buildings.md`. Systems live in `00-gameplay-improvements.md`.

## 1. Sources and sizes

| Art | Source upload | Runtime copy | Project copy |
| --- | --- | --- | --- |
| Grass | `Pictures/Chronoseria Assets/grass.png` | `src/main/resources/assets/tiles/grass.png` | `assets/grass.png` |
| Tree (forest, schematic dark-green) | `Pictures/Chronoseria Assets/Tree base.png`, `tree upper.png` | `src/main/resources/assets/tiles/tree_base.png`, `tree_upper.png` | `assets/tree_base.png`, `assets/tree_upper.png` |
| Wall brick (front face) | `Pictures/Chronoseria Assets/Brick wall.png` | `src/main/resources/assets/tiles/wall_brick.png` | `assets/wall_brick.png` |
| Wall Top (cap) | `Pictures/Chronoseria Assets/brick Wall Top.png` | `src/main/resources/assets/tiles/wall_top.png` | `assets/wall_top.png` |
| Water | `Pictures/Chronoseria Assets/Water.png` | `src/main/resources/assets/tiles/water.png` | `assets/water.png` |
| Cobble path (all path kinds) | `Pictures/Chronoseria Assets/Cobblestone path.png` | `src/main/resources/assets/tiles/path.png` | `assets/path.png` |
| House roof (single row) | `Pictures/Chronoseria Assets/house roof.png` | `src/main/resources/assets/tiles/house_roof.png` | `assets/house_roof.png` |
| House wall, window | `Pictures/Chronoseria Assets/House wall window only.png` | `src/main/resources/assets/tiles/house_wall_window.png` | `assets/house_wall_window.png` |
| House wall, plain | `Pictures/Chronoseria Assets/house wall no window.png` | `src/main/resources/assets/tiles/house_wall_plain.png` | `assets/house_wall_plain.png` |
| House base, window (ground floor) | `Pictures/Chronoseria Assets/House wall lower level.png` | `src/main/resources/assets/tiles/house_wall_base_window.png` | `assets/house_wall_base_window.png` |
| House base, plain (ground floor) | `Pictures/Chronoseria Assets/house wall lower level no window.png` | `src/main/resources/assets/tiles/house_wall_base_plain.png` | `assets/house_wall_base_plain.png` |
| House door, closed | `Pictures/Chronoseria Assets/House door closed.png` | `src/main/resources/assets/tiles/house_door_closed.png` | `assets/house_door_closed.png` |
| House door, open (on doorway) | `Pictures/Chronoseria Assets/House door open.png` | `src/main/resources/assets/tiles/house_door_open.png` | `assets/house_door_open.png` |
| Sewer grate (north + south wall crossings) | `Pictures/Chronoseria Assets/Sewer.png` | `src/main/resources/assets/tiles/sewer.png` | `assets/sewer.png` |
| Fountain base (solid, Layer 2) | `Pictures/Chronoseria Assets/fountain base layer.png` | `src/main/resources/assets/tiles/fountain_base.png` | `assets/fountain_base.png` |
| Fountain upper (walkable overhead, Layer 3) | `Pictures/Chronoseria Assets/fountain upper layer.png` | `src/main/resources/assets/tiles/fountain_upper.png` | `assets/fountain_upper.png` |

Rules:

- All are 32x32 (verified). One image = one cell, drawn 1:1 to `s = TILE * SCALE`.
- Grid: `TILE = 16`, `MAP_W = MAP_H = 32`, `SCALE = 2` (`VillageScene.java`).
- Never stretch a tile across the map. Never place off-grid.
- Keep uploads in Pictures untouched; runtime loads from `src/main/resources` via `AssetLoader`.

## 2. Layer model (Dawn-of-Souls style)

| Layer | Contents | Code |
| --- | --- | --- |
| Layer 1 ground | Grass, path, water, gate (bridges render as path; 3x3 fountain plaza renders as path) | `paintGround()` |
| Layer 2 fringe | Wall caps/faces, sewer grates, tree trunks, fences, fountain base, buildings base, doors, NPC blockers | Y-sorted `draw()` pass (`baseY * 4 + order`) |
| Layer 3 overhead | Wall face lower strip (`wallBrickTile`, `sewerTile` for sewers), tree canopies, building roofs, fountain upper | `drawWallForeground()`, `drawTreeCanopyPass()`, `drawRoofForeground()`, `drawFountainUpperPass()` after player |

Paint order is intentional: all-grass base → paths → water (`solid`) → bridges
(ground stays `WATER` but renders as `path.png` and clears `solid`) → walls via
`wallMask` overlay (walls set `solid` but never change `ground`, so seams sit on
grass and water gaps at walls are deliberate sewer points) → buildings/NPCs/fountain.
Collision is a separate bitmask: `solid[x][y]`. Visuals never decide movement; `startStep()` blocks on `solid`.

## 3. Walls — outer-rectangle perimeter, caps wrap, faces front-only

Implementer: `VillageScene.java` §7 — `buildTopWall()`, `buildSideCaps()`,
`buildBottomBand()`, `buildStemWalls()`, `buildFrontWall()` write a `wallMask`,
then `stampWallsDynamic()` resolves it: mask cell with wall south of it becomes
cap `T`, otherwise south-facing face `W`.

- Top band: `rows 3-4, x0..31`; row 3 all `wallTop`, solid; `row 4, x1..30 =
  wallBrick` with corners `(0,4)/(31,4) = wallTop`, solid.
- Sides: `col 0 / col 31, y5..24`, solid. `rows 5-23 = wallTop`; bottom cells
  `(0,24)/(31,24) = wallBrick` (3D thickness, like the old bar-bottom corners).
- Bottom band: `rows 23-24, x0..9 + x21..31`; row 23 all `wallTop`; row 24 =
  `wallBrick` except junction caps `(9,24)/(21,24)`. Opening `x10..20` on both
  rows (B8 roof + lanes pass through, no wall cells).
- Stem: `col 9 / col 21, y25..29 = wallTop`, solid.
- Front wall, top-to-bottom = cap then face: `rows 30-31, x9..21 except gate =
  wallTop` first layer, solid; `row 31, same span = wallBrick` last layer,
  solid. Gate `x15` stays `GATE` walkable on both rows.
- Ground under every wall cell stays `GRASS` so seams sit on grass, not gaps.
  The base fill is all-grass first, so there is grass inside + outside the walls.
  Walls never set `ground[][]` — they only overlay `wallTops`/`wallBodies` and set
  `solid`. Water is therefore never placed under a wall; the river stops at the
  wall edge and the sewer grate carries it visually through.
  The north river passes under the top band via sewer `(15,4)`, and the south run
  passes under the bottom band via sewer `(23,24)` (walls stay solid over grass ground).

Corner rule for future stamps:

```text
IF cell == BRICK_FRONT at (x, y):
  Place WALL_TOP at (x, y-1)
  IF (x-1, y) is not BRICK_FRONT: (x, y) is the left end; (x-1, y) stays background
  IF (x+1, y) is not BRICK_FRONT: (x, y) is the right end; (x+1, y) stays background
  Never stamp BRICK on a side column
```

Counts: `wallTops = 115`, `wallBodies = 59`, `sewers = 2`, `bounds = 0`.

## 4. Ground, paths, gate, spawn

Layout source: `assets/new-village-map.png` (32x32 schematic, decoded by cell-center
sampling in build tooling — never hand-transcribed).

- Base: entire map starts `GRASS` (grass inside + outside the walls).
- Path loops (all cobble `path.png`): north lane `row 10, cols 4-17`; west loop
  `col 4, rows 11-20`; middle loop `col 17, rows 11-22`; east lane `row 12, cols 18-30`;
  east loop `col 30, rows 13-20`; south lane `row 20, cols 5-29` (fence gap at `x17`
  stays walkable grass); corridor west `col 10, rows 21-27`; gate column
  `col 15, rows 21-29`; inn frontage `row 27, cols 11-14`; nub `(16,22)`.
- Bridges (cobble over water, walkable): `(15,10)`, `(4,12)`, `(17,16)`, `(23,20)`.
  A bridge cell keeps `WATER` ground but `paintGround()` draws `path.png` first via
  `isBridge()` and `solid` is cleared, so the crossing reads as cobble and walks.
- River: north edge entry `col 15, rows 0-2` (passes under the top wall via sewer
  `(15,4)`); mid run `col 15` down to row 11, row-12 run `cols 1-15`, east run
  `row 16, cols 15-23`, south run `col 23` (passes under the bottom band via sewer
  `(23,24)`) to the moat `row 29, cols 23-29`, exiting off-map at `col 29`.
- Gate floor: `(15,30),(15,31) = GATE` (1-wide), always walkable.
- Spawn: `(13,29)` on grass by the gate column, facing up.

## 5. Trees, grass, fountain, fences, wells

- Grass: `grass.png` tiled on `GRASS` (two-shade checker baked into the art). No
  procedural fallback — missing art fails fast at load.
- Trees: 159 forest cells from the locked schematic dark-green layer
  (`assets/new-village-map.png` `#116D18`, cell-center sampled), stamped via
  `tree()` (`TREES` fixed array, same map every run, no RNG): solid trunk,
  trunk Y-sorted Layer 2, canopy overhead Layer 3 (`tree_base.png`/`tree_upper.png`).
  The upper canopy is a separate cell directly above the base (no overlap of the
  two images in one cell); when that cell is itself a tree base (vertical
  adjacency), the canopy overlays that base. Clumps  of 2-5 with grass gaps: northfield thickets (rows 0-2), ridge bands behind
  the north buildings (rows 5-7), west ridge slivers, corner/dead-space fills,
  south exterior woods; singles frame lanes/plaza/gate. Never on paths,
  bridges, doors, entrances, water, or the plaza margin (cols 14-18, rows
  19-23); 1 free cell around every entrance; never touching a building front
  (south) row; NPC spawns clear (4 villager haunts overlap trunks and are
  skipped by `pickHaunt`). ~28% forested by schematic count; fences (17 orange
  cells) and the well (`(2,8)`) are now wired up (see sections 5/11); the
  fence-gap rule is active.
- Fountain plaza: `3x3` cobble `PATH` at cols `15-17`, rows `20-22` (all walkable
  except center). Base `(16,21)` = `fountain_base.png`, Layer 2 Y-sorted, `solid`.
  Upper `(16,20)` = `fountain_upper.png`, Layer 3 overhead after player, walkable
  (transparent, player steps under it and appears behind). Old single-cell
  `water.png` fountain replaced. Layout (`c` = cobble, `f` = fountain):
  ```text
  ccc
  cfc
  cfc
  ccc
  ```
  Center `5` of the `3x3` is the solid base; the cell above it is the walkable upper.
- Fences: 17 orange cells on the schematic, wired with `fence.png`
  (`assets/fence.png`, `src/main/resources/assets/tiles/fence.png`): cells
  (4,21),(5,21),(6,21),(7,11),(8,11),(12,11),(13,11),(14,19),(15,19),
  (16,19),(16,28),(17,25),(17,28),(22,11),(23,11),(26,21),(27,21); solid
  blockers on grass.
- Wells: `(2,8)` dark-grey single, wired with `well.png`
  (`assets/well.png`, `src/main/resources/assets/tiles/well.png`); solid.- Bushes: 18 in clustered clumps (outside + inside the village), varied shapes,
  on free grass cells away from doors/entrances/walls, wired with `bush.png`
  (`assets/bush.png`, `src/main/resources/assets/tiles/bush.png`); solid grass
  props.

## 6. Buildings and doors

- 8 buildings, all 3x4, door centered on facade bottom row (except legacy 4-wide
  INN rule, dormant), entrance cell directly south. `E` works only on entrance facing up.
- Doors start shut (solid). Entry is 2-tap: pushing into a shut door swings it
  open (tap 1, `The door creaks open.`), then release and push again to step
  through (tap 2) — holding the opening press never walks you in. Turning to
  face a shut door counts as tap 1, so entry is always 2 taps and never
  turn + bump + step. It shuts behind you when you step off, and bumping
  another door shuts the first (at most one open). The open sprite shows from
  the opening click (with the creak message) until the door shuts behind you.
  HOUSE stays locked — bumping it shows
  its blurb and never opens. `E` on the entrance facing up still reads the
  building blurb.
- Facades (sprite): single straight roof row (`house_roof.png`); max 2 floors — upper rows mix
  `house_wall_window.png` / `house_wall_plain.png` (1–4 windows per building, centered);
  ground row mixes `house_wall_base_window.png` / `house_wall_base_plain.png` (lower-level
  walls live only here, door cell excluded). Door shows `house_door_open.png` while the player
  stands on the door or entrance cell, else `house_door_closed.png`.
- Roster (7 canonical + 1 house): TAVERN `(7,6)`; MENTOR `(12,6)`; BLACKSMITH `(19,8)`;
  MARKET `(25,8)` (corrected +1 east per schematic decode; door stays `(26,11)`);
  HOUSE `(1,13)` on the west wall line (wall column stays clear);
  WIZARD `(6,16)`; SANCTUARY `(26,16)`; INN `(12,23)` by the south corridor.
  Doors: `(8,9)`, `(13,9)`, `(20,11)`, `(26,11)`, `(2,16)`, `(7,19)`, `(27,19)`, `(13,26)`;
  all entrances walkable (paths, except B5 `(2,17)` grass).
- Street frontage (amends old path-gap rule): paths may run flush against facades;
  no path ground may cover a door cell; every entrance stays walkable; flood fill
  from spawn must reach all 8 entrances. The old one-cell buffer rule no longer
  applies — the map is drawn street-style like the reference village.
- Wall-river crossings on this map: north `(15,3-4)` and south `(23,23-24)`.
  Both get a sewer grate — always apply sewers on north and south walls where
  the river meets a wall (never leave a plain brick at the water column).
- Sewer placement pattern (5 wide, water column centered, grass buffers both sides).
  Legend: `t` = wall cap, `w` = wall brick, `s` = sewer grate, `z` = water/river, `g` = grass.

  ```text
  ggzgg
  ggzgg
  ttttt
  wwsww
  ggzgg
  ggzgg
  ```

  Placed: north `S = (15,4)` with cap `(15,3)`, water `(15,2)/(15,5)`; south
  `S = (23,24)` with cap `(23,23)`, water `(23,22)/(23,25)`. South 5-wide south
  row reads `Wgwgg` at cols 21-25 because the stem wall `W` at `(21,25)` sits
  2 west of the river — the immediate `gwg` buffer still holds.

  Rules: caps run continuous above (never a gap at the sewer column); the sewer replaces
  the brick in the brick row (never the cap row); water runs through both sides;
  sewers only on valid brick rows (top `row 4`, bottom-band `row 24`, front `row 31`),
  never side walls; the sewer is solid with a `wallTop` cap directly above it
  (asserted at build time).
- Paths never touch buildings except via entrance cells.
- NPCs: Elowen `(5,7)`, Darius `(22,7)`, Kaelira `(16,13)`, Amara `(13,18)`, Theron `(13,28)` —
  solid, on walkable grass, never on doors/paths/walls/fountain.
- Villager minds (all five, zoned): Elowen north-west block, Darius north-east block, Kaelira
  center loops, Amara mid-south + fountain court, Theron corridor-south + gate — each wanders
  only its home haunts so every part stays covered. `IDLE` 2–5s → `WANDER` BFS → rest
  2–6s. `TALKING` freezes facing the player with 3 cycling lines. `MOVE_ASIDE`
  steps to a free side cell (never doors/player/walls) only when blocking an
  approaching player; `BACK_UP` retreats when pinched. Resume past 4 tiles.
  Logical cell + `solid[][]` move at step begin (atomic, no mid-tween overlap),
  render catches up over 18 ticks; calm near the player instead of bustling feet.
- Yield pressure (any villager): contact block, closing within 2 from any side,
  or tailing in-line behind within 4 while stepping. Dodges chain on a short
  cooldown and drop the stale wander path; the next haunt pick biases away from
  the player so they never walk straight back into a tail. Nose-to-nose with no
  sides free resolves as `PASS_THROUGH`: 2 scripted steps (your cell, then the
  cell beyond along their heading, walls/NPCs still abort), so you squeeze past
  each other instead of deadlocking.
- Cornered exits: a villager standing on your only way out clears a chained
  2–3 step path off your exit line (gates bypassed once, then re-armed).
- Herding: sustained pressing accumulates; at threshold the villager commits to
  a `break-away` — the reachable zone cell with most separation plus open
  neighbors — facing it at once and strolling there, ignoring minor re-presses
  until arrival. Counts as the minute's dodge. At most one dodge per
  villager per minute (3600-tick gate).

## 7. Collision quick matrix

| Block | Layer | Solid | Notes |
| --- | --- | --- | --- |
| Grass/path/road/plaza/gate | 1 | Passable | Walking layer; bridges keep `WATER` ground but render path + clear solid |
| Wall Top cap | 2 | Impassable | Top band `row 3` + corners, sides `col 0/31 rows 5-23`, bottom band `row 23`, stem sides, front `row 30` |
| Wall brick face | 2 (+3 strip) | Impassable | Top `row 4 x1..30`, bottom-band `row 24` outer spans, front `row 31` stem span minus gate (minus sewer cells) |
| Sewer grate | 2 (+3 strip with `sewerTile`) | Impassable | Replaces brick at river crossings `(15,4)` + `(23,24)`, cap above |
| Invisible bound | — | Impassable | Reserved; none currently placed (south is now visible wall) |
| Tree trunk | 2 | Impassable | Base cell blocks (159 forest cells) |
| Tree canopy | 3 | Passable overlay | Visual only, separate cell above base; overlays base of tree above in vertical adjacency || Building body/door frame | 2 | Impassable | Door cell itself walkable |
| Roof | 3 | Overlay | Covers player walking behind |
| Fountain base | 2 | Impassable | `(16,21)` on `3x3` cobble plaza, `fountain_base.png`, Y-sorted |
| Fountain upper | 3 | Passable overlay | `(16,20)` transparent, `fountain_upper.png` via `drawFountainUpperPass()`, player goes behind |
| Fence/NPC | 2 | Impassable | Grouped blockers |

## 8. Validation (never remove)

`VillageScene` asserts at build time: doors reachable by flood fill from spawn; street frontage (no path on door cells, entrances walkable); door-on-facade (centered; legacy 4-wide INN exception dormant); inside map; no overlap; `wallBrick` only on top `row 4 x1..30`, bottom-band `row 24` outer spans, side bottoms `(0,24)/(31,24)`, and front `row 31` stem span minus gate (sewer cells replace bricks there and are tracked separately); caps continuous along the rectangle polygon; bottom/stem openings clear; every structure inside the walls and off walls; gate walkable; sewer rule per-unit (2 placed: north `(15,4)` + south `(23,24)`, each solid with cap above on a valid brick row); fountain plaza `3x3 PATH` cols `15-17` rows `20-22`, base `(16,21)` solid + upper `(16,20)` walkable; forest placement (159 schematic trees on `TREE` ground, solid, off walls/buildings/doors/entrances/NPCs/spawn, entrance buffers + plaza margin + building-front rows clear, count exact); forest connectivity (flood fill from spawn reaches every `PATH`/`GATE` cell modulo the fountain island, and every entrance); doors start shut+solid with walkable entrances and none open (`assertDoorsShut`, stamped after the reachability asserts); exactly 8 doors.

## 9. Adding a new block

1. Add 32x32 PNG to both `assets/` and `src/main/resources/assets/tiles/`.
2. Load in `ensureSprites()` via `loadTile("/assets/tiles/<name>.png")` with null fallback.
3. Stamp with `wallTop / wallBrick / sewer / tree / invisibleBound` or `fillGround`; set `solid` to match §7. `sewer(c,r)` removes the stamped brick from `wallBodies`, tracks it in `sewers`, and keeps `solid`.
4. Draw in the correct pass: ground → Y-sorted Layer 2 (`drawWallBrick()` draws `sewerTile` when `isSewer()`) → Layer 3 overhead after player (sewers use the same strip clip but with `sewerTile`).
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
5. Paths are loop/lane networks.
   Street frontage is allowed: a path may run flush against a facade, but no
   path ground may cover a door cell and every entrance stays registered walkable.
6. Buildings sit on walkable ground clear of walls, with centered doors and
   a walkable entrance cell directly south; `E` works only on the entrance facing
   up. Fountains are solid water-sprite islands. Props and
   NPCs sit on walkable ground, never on paths, doors, entrances, or walls.
7. A map ships only when every build-time assert passes: flood-fill reachability
   from spawn/entry, street frontage, door-on-facade, inside-map, no-overlap,
   face-placement, `T`-over-`W`, containment off walls, walkable gate, exact
   interactable count. (Street frontage above is a documented design change,
   not a weakening: door/entrance guarantees hold.) Do not weaken an assert to make a layout fit — fix the layout.
8. Rooms and dungeons reuse this stamper and these validators; only the ground
   and wall art swap. Do not duplicate wall logic per map — extend the shared
   `wallTop / wallBrick / wallMask / stampWallsDynamic` helpers instead.
9. Every river-wall crossing gets a sewer: always check north and south walls where
   water meets wall and replace that brick with `sewer.png` (solid, cap above).
   Current standard: north `(15,4)` + south `(23,24)`; never ship a plain brick
   at a water column.
10. Tree placement rules (classic walled-town forest):
    - Border: forest bands run just inside the walls plus the outside-walls
      exterior (northfield, south woods), skipping the gate corridor, stem
      opening, paths, river, and wall cells. The perimeter itself is wall, so
      trees frame it instead of replacing it.
    - Place trees in clusters of 2 to 5 and in forest zones (schematic
      dark-green cells, fixed hand-verified pattern — same map every run, no
      RNG). Uneven forest edges, with a few open cells inside.
    - Keep about 40 to 50 percent of free grass open (schematic count rules;
      current map: ~28% forested, rest open).
    - Never on a path, bridge, door, entrance, fence, water, or plaza edge.
    - Keep 1 free cell around every entrance. Keep 1 cell gap from building
      fronts (south side). Trees behind and beside buildings are allowed.
    - Fence-gap rule is active: 17 fence cells (orange) are now solid fences;
      schematic adjacencies stand.
    - Trees are solid single tiles (`tree()` trunk cell). Y-sort: trunk with
      entities Layer 2, canopy drawn in front Layer 3 in the cell directly
      above the base (separate cell; overlays the base of the tree above when
      vertically adjacent).
    - After placing, run a flood fill from spawn: every `PATH`/`GATE` cell and
      every entrance must stay reachable. If not, remove the blocking tree —
      never weaken the assert.

## 11. Current village asset placement (build snapshot)

Counts: 159 trees (+149 upper-canopy cells), 17 fences, 1 well at `(2,8)`,
18 bushes, 8 buildings, 5 NPCs, 4 bridges, 1 fountain (base+upper), 2 sewers,
115 wall tops, 59 wall bricks.

Fences (solid): (4,21),(5,21),(6,21),(7,11),(8,11),(12,11),(13,11),(14,19),
(15,19),(16,19),(16,28),(17,25),(17,28),(22,11),(23,11),(26,21),(27,21).

Well (solid): (2,8).

Bushes (solid clumps): (3,0),(4,0); (10,0),(10,1); (24,0),(25,0);
(6,6),(6,7); (18,13),(18,14); (24,15),(23,15),(24,16); (7,26),(8,26),(7,25);
(20,23),(19,23),(20,24).

Sewers: (15,4) north, (23,24) south. Bridges: (15,10),(4,12),(17,16),(23,20).
Fountain: base (16,21), upper (16,20). Spawn: (16,26). Gate: (15,30)-(15,31).

Tree upper canopy is drawn in the cell directly above each tree base; when that
cell is itself a tree base (vertical adjacency) the canopy overlays it.

Full map (one char per tile; see legend in `VillageScene.printMap()` output):

```text
TTTbb....Tb....~.....TTTbb.TTT..
TTTT...T..bTT..~.TTT...TT.....T.
...TTTT........~...T.......T..TT
WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
WwwwwwwwwwwwwwwSwwwwwwwwwwwwwwwW
WTTTTTT......TT~......TTTTTTTTTW
WTTTTTbVVV..EEE~...TTTTTTTTTTTTW
WTTT.NbVVV..EEE~......N...TTTTTW
WTO....VVVT.EEE~T..WWW...iii.TTW
WT.....VDVT.EDE~TT.WWW.TTiii..TW
W...ppppdppppdp=ppTWWW...iii..TW
W...p..ff...ff.~.pTWDWff.iDi...W
W~~~=~~~~~~~~~~~.pppdpppppdppppW
WHHHpTTTTT.....~Npb...........pW
WHHHp..TTTT....~.pb.TT....T...pW
WHHHp..........~.p.TTT.bb.....pW
WHDHp.MMM......~~=~~~~~~b.UUU.pW
W.d.p.MMMT.......p.....~..UUU.pW
W...p.MMM....N...p....T~..UUUTpW
W...p.MDM...T.fffp.....~.TUDUTpW
WT..pppdppppppppUpppppp=pppdpppW
WT..fff...pTT..pFp.....~..ff...W
WTT.......p....ppp.T...~.TTTTTTW
WWWWWWWWWWp.IIIp...bbWWWWWWWWWWW
wwwwwwwwwWp.IIIp....bWwSwwwwwwww
..TT...b.WpTIIIp.f...W.~..TT....
...TTTTbbWpTIDIpT...TW.~..T....T
.........Wpppdpp....TW.~....T...
TT......TWT..N.pff.TTW.~.......T
.TT..T...WTTTP.p....TW.~~~~~~~..
.....TT..WWWWWWGWWWWWW.......~.T
..T......wwwwwwGwwwwww.TT.T..~..
```

Legend: `.` grass, `p` path, `G` gate, `~` water, `T` tree, `F` fountain base,
`U` fountain upper, `W` wall top, `w` wall brick, `S` sewer, `f` fence, `b` bush,
`O` well, `=` bridge, letters for buildings/NPCs/spawn.