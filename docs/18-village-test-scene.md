# 18 — Village Test Scene (temporary)

Temporary walkable village for Chronoseria: The Ancient Runes (JDK 26 Swing).
Replaces the old GAME placeholder grid. Landscape 640x480.

## Entry flow

1. **TITLE** — fades in; "click any key" after 2s; any key/click continues.
2. **MENU** — `New Game` or `Continue` enters the village. `Load Game`,
   `Chronicles`, `Bestiary`, `Settings` remain inert placeholders.
3. **VILLAGE** — walk, talk, `ESC` back to menu, `ESC` again to title.

## Controls

| Key | Action |
| --- | --- |
| Arrows / WASD | Walk (held-key state, smooth movement) |
| ENTER / Z | Talk to a nearby NPC; advance dialogue |
| ESC | Return to menu (clears held keys) |

## Map

Simple cell grid: **16 x 12 cells of 40px = exactly 640x480** (no scrolling).

- `.` grass, `,` path, `T` tree (border), `W` pond, `H` house, `D` door.
- Three houses (Inn, Market, Sanctuary door fronts) connect to a main road.
- Solid cells: tree, water, house, door. Actor boxes collide with each other.

## Actors (temporary emoji glyphs)

| Actor | Glyph | Notes |
| --- | --- | --- |
| Player | 🧙 | Walk bob animation while moving |
| Mira | 👩 | Inn area; 2 dialogue lines |
| Orin | 🧔 | Market area; 2 dialogue lines |
| Kael | 👴 | Village road; 2 dialogue lines |
| Scenery | 🌳 🌊 🏠 🚪 | Tree / water / house / door cells |

NPCs wander randomly within 3 cells of home, pausing between steps.

Dialogue box: parchment fill, royal-blue border, gold corner squares
(`GamePalette`), speaker glyph + name + line, bottom of screen. Player
movement freezes while a dialogue is open.

## Code

- `src/main/java/runes/VillageScene.java` — map, movement, collision,
  wandering NPCs, dialogue, rendering.
- `InputManager` — held-key set (`keyPressed`/`keyReleased`) + one-shot
  talk press filtered from auto-repeat.
- `GamePanel` — `GAME` screen delegates to `VillageScene`; `menuSelect`
  gates to New Game/Continue; `startGame()` resets the scene.
- `Main` — 640x480, `panel.attachInput(input)`.

## Replace later

Emoji glyphs and the cell layout are placeholders. Swap for `AssetLoader`
sprites and real map data from `14-maps-assets.md` when art exists.
