# 00 — Recommendations & Future Work (extracted from docs/ reference notes)

> This file holds all Ancient Runes plans, copy notes, and next steps moved out of `docs/00-15`
> so those files stay reference-only (other game's evidence).
> Source files are unchanged except for a pointer line.

## 00 — Overview (from 00-overview.md:88,112-114)
- Future JDK 26 build will be Gradle + `JFrame/JPanel/Graphics2D`, `javax.sound.sampled`, `java.nio.file` saves — no `javax.microedition.*`.
- Next step: Read 01→10 for raw evidence, then 11→15 for synthesized systems + rebrand plan. No code scaffold until docs approved.

## 01 — Entry Lifecycle (from 01-entry-lifecycle-FinalFantasy.md:26-37)
- FinalFantasy -> RuneOriginApp (main class, JDK26: `public class Main { public static void main }`)
- startApp first-branch -> `init() + showWindow() + startLoopThread()`
- startApp second-branch -> `resumeWindow()`
- pauseApp -> `onPause()`, destroyApp -> `onExitSave()`
- Display.setCurrent(a) -> JFrame.setContentPane(GamePanel)
- new Thread(a).start() -> game loop executor / Thread(GameLoop)
- What to copy:
  - Lazy singleton screen creation, re-set on resume, thread start once.
  - Pause guard on short state flag before calling suspend.
  - Destroy always calls save/teardown if screen exists.

## 02 — Class Catalog (from 02-class-catalog.md:28)
- Copy for Ancient Runes: keep same state split, rename to meaningful names.

## 03 — Main Canvas (from 03-main-canvas-a.md:31-34)
- RuneCanvas: `JPanel + Runnable`, `paintComponent(Graphics2D)`, `KeyListener`, `GameLoop thread`.
- Keep static globals but refactor to `GameContext` singleton (no statics sprawl).
- Save: RMS -> `SaveManager` with `java.nio.file`.

## 04 — Engine g (from 04-engine-g.md:23-26)
- Split g into: `WorldEngine`, `BattleEngine`, `MapRenderer`, `UIRenderer`, `FlagStore`.
- Keep method-per-layer idea, but as separate classes for JDK26.
- Encounter logic lives here — see 12-encounters-spawns.md.

## 05 — State c (from 05-state-c.md:19-22)
- Candidate for `BattleScreen` or `DungeonHub` in Ancient Runes.
- Extract tables to `data/battle_tables.json` equivalent, not hard-coded arrays.
- Keep `a(Graphics)` split: background, sprites, floating numbers, menu.

## 06 — Base h + States d,e (from 06-base-h-and-states-d-e.md:26)
- Copy: keep h pattern as `GameState` interface + `BaseState` helper in JDK26. d->MenuState, e->DialogueState.

## 07 — Sprite f (from 07-sprite-f.md:17-20)
- Ancient Runes `Sprite` class: `BufferedImage[] frames; int w,h; void load(byte[] packed); void draw(Graphics2D g,int x,int y); void drawClipped(...)`.
- Replace `Image[]` with `BufferedImage[]`. Keep packed format loader — document RLE/palette after deeper decompile.
- `b` flag = visible/dirty. `d,e` = anchor offset.

## 08 — Entity j (from 08-entity-j.md:19-21)
- Ancient Runes `Entity`: `x,y,hp,maxHp,mp,atk,def,spd; boolean alive,active,visible; Anim anim; Script script; void update(); void draw(Graphics2D);`
- Keep flag count but name them. AI tables in `byte[][]` -> `List<AICommand>`.

## 09 — Tile/Anim k (from 09-tile-anim-k.md:19-21)
- Split into `Tileset` (static map) + `Anim` (timed frames) in JDK26.
- Keep `short` ids for save compat, but expose enums: `Tile.SOLID, Tile.ENCOUNTER, Tile.WARP`.

## 10 — Data b+i (from 10-data-b-i.md:19,42)
- Copy: `GrowthCurve { int tier; int scale(); }` + `Stats { byte[8][3] }`.
- Copy: `Party { Battler[4] }`, `QuestFlags[86][2]`, `StoryFlags[50]`, `Bestiary[255]`.

## 11 — Battle System (from 11-battle-system.md:33-36)
- Keep 4-slot party, 255 bestiary, tier-scale curve.
- Rebuild as: `BattleEngine { List<Battler> allies,enemies; Queue<Turn> orderBySpd; }`, `DamageCalc { int calc(Atk,Def,Tier,Element) }`, `BattleScreen`, `MessageBox`.
- Do NOT copy spell names/text — write original rune magic.

## 12 — Encounters & Spawns (from 12-encounters-spawns.md:31-34)
- Ancient Runes `EncounterSystem { float rateFor(Biome,Steps); List<Enemy> roll(Zone) }`.
- Keep step-counter + 0-255 roll (classic feel), but expose JSON tables.
- Bosses = `short[86][2]` one-shots, not random.

## 13 — Story Arcs (from 13-story-arcs-PACK0-4.md:23-26)
- Ancient Runes: `arcs/arc1..arc5/` each with `header.json, maps/, dialogues/, encounters.json, boss.json`.
- Keep 5-arc pacing, 12-15 maps per arc. Write ORIGINAL story — rune shards, not crystals.
- Loader: `AssetLoader.loadArc(n)` mirrors `a.a(PACKn_m)`.

## 14 — Maps & Assets (from 14-maps-assets.md:31-34)
- Copy STRUCTURE + sizes for reference, not binary content verbatim in new game.
- Ancient Runes `assets/` mirrors: `tiles/, sprites/, portraits/, maps/, audio/, fonts/, text/{en,fr,it,de,es}/`.
- Keep 5-lang support — cheap win from `I` design.

## 15 — Rebrand + JDK26 Plan (from 15-rebrand-to-Ancient-Runes.md:39,41-43)
- No build yet — docs approved first. Next: CFR decompile of a,g,c for bodies, then Gradle scaffold `ancient-runes-jdk26/`.
- Immediate next steps when ready:
  1. Run CFR/Procyon on a,g,c,h -> save excerpts to `16-decompile-notes.md` (new file if needed).
  2. Scaffold Gradle Java 26 app in separate folder (NOT here).
  3. Implement GamePanel + AssetLoader loading copied PACK structure as JSON.
