# Chronoseria: NPC Villagers — Mind and Behavior

Game design + implementation document · Ordinary village routine, polite yielding,
dialogue. Source of truth for villager behavior. Map placement source of truth
stays in `07-asset-arrangements.md`. Code lives in
`src/main/java/runes/VillageScene.java` (Java Swing, grid-step, 60 UPS).

## 1. Cast

All five are ordinary villagers (no schedules, no quests yet). Spawn, home zone,
and dialogue below. Zones keep every part of the village covered at once.

| Villager | Spawn | Home zone haunts | Dialogue stages 0 → 2 |
|---|---|---|---|
| MIRA | `(14,19)` stem-mid | `(14,19)`, `(14,22)`, `(18,18)`, `(20,19)` | “Good day! The village is peaceful today.” / “The inn is north up the road if you need a rest.” / “It is nice to see new faces around here.” |
| Bram | `(16,12)` junction | `(20,8)`, `(14,12)`, `(9,11)`, `(6,10)` | “Fountain's east. Shops are warm.” / “Shops line the north ridge. Mind the walls.” / “Odd says stay on the road. He's right.” |
| ODD | `(16,13)` corridor | `(18,18)`, `(20,19)`, `(14,22)`, `(16,13)` | “Stay on the road.” / “The gate is one tile wide. Single file.” / “Walls are solid. Don't bother pushing.” |
| Pip | `(14,28)` gate | `(14,28)`, `(14,27)`, `(18,27)`, `(14,22)` | “South road goes everywhere.” / “I saw the fountain glittering yesterday.” / “Plaza's west. Can't miss it.” |
| Sol | `(9,7)` bar-north | `(5,8)`, `(9,8)`, `(4,10)`, `(6,10)`, `(9,7)`, `(9,11)` | “Knock twice at the Inn.” / “Blue roof, north side. That's the one.” / “Quiet today. Good.” |

Roster code: `VillageScene.java:417-441`. Spawn cells are `solid` at build and
covered by the static asserts (inside map, off trees/walls/paths/doors).

## 2. Mind model (FSM)

`enum NpcState` (`VillageScene.java:78`): `IDLE`, `WANDER`, `TALKING`,
`MOVE_ASIDE`, `BACK_UP`, `PASS_THROUGH`. Mutually exclusive — a villager never
wanders and yields at once. Per-NPC record (`VillageScene.java:80`): grid
`col/row`, render tween `renderCx/renderCy`, `facing`, `state`, stepping triple
`from/to/stepT`, `idleT`, `asideCd`, `avoidCd`, `pushT`, `breaking`, dialogue
`stage`, BFS `path` queue, 6-cell `trail`, `safeC/safeR` last wide cell.

| State | Does | Exits when |
|---|---|---|
| `IDLE` | Stands, records wide cells, waits 2–5s (`120–300` ticks) | Timer done → pick zone haunt → `WANDER` |
| `WANDER` | Strolls BFS path at 18 ticks/cell (player: 12) | Arrives → rest 2–6s, random facing → `IDLE` |
| `TALKING` | Frozen, faces player, stage line in message box | Box expires → `IDLE` |
| `MOVE_ASIDE` | Steps (or chained queue) to a free side cell | Queue done → hold until player past resume range → `IDLE` |
| `BACK_UP` | Retreats from a pinch toward the last wide spot | Same resume rule as above |
| `PASS_THROUGH` | Scripted 2-step squeeze: your cell, then beyond | Queue done → `IDLE` |

Tick entry `updateNpcs()` (`VillageScene.java:918`): mid-tween advances; talkers
face you; then pressure checks; then the state switch. Calm near the player on
purpose — villagers hold instead of bustling around feet.

## 3. Wandering

Loop: rest → nearby zone haunt (≤12 tiles) → BFS there (never through you) →
rest. Haunt picker (`VillageScene.java:1326`) runs two passes: first haunts no
closer to you than now (never walks back into a tail), then anything reachable;
targets must be walkable, unoccupied, never doors/entrances, and on path-kind
ground they must not sit shoulder-to-shoulder with another villager. Blocked
next step → replan once → else rest. `findPath()` (`VillageScene.java:1350`) is
plain grid BFS over `!solid` minus villagers minus you.

## 4. Yield pressure (any villager)

One check (`playerPressing()`, `VillageScene.java:1080`) with three triggers:

- Contact block (`≤2` tiles): your facing cell is them, or your step lands on them.
- Closing halo (`≤2`): your step shortens their distance, from any side.
- Tailing (`2–4` behind): you step with them ahead on your line (±1 lateral).

One step answers it (`yieldStep()`, `VillageScene.java:1091`): free side cells
first — preferring cells beside no other villager, never doors/entrances/you/
walls — else the cell behind the approach (`BACK_UP`). Chased dodges chain on a
10-tick cooldown and drop the stale path; the next haunt biases away from you.

## 5. Deadlocks and herding

- Nose-to-nose with no sides free → `PASS_THROUGH`: your cell, then the cell
  beyond along their heading (walls/NPCs/doors still abort it). You two squeeze
  past; solid accounting follows them so F3/map stay true.
- Cornered you (≤1 free neighbor, villager on an exit) → `cornerClear()`
  (`VillageScene.java:1189`): chained 2–3-step BFS to the nearest standing cell
  ≥3 away, off your exit line; gates bypassed once, then re-armed. In-flight
  queues are never overwritten by a replan.
- Herded over and over → `startBreakaway()` (`VillageScene.java:1247`):
  sustained pressing accumulates (`pushT`, threshold 90 ≈ 1.5s, decays off-press,
  resets past resume range); the villager faces its escape cell at once and
  commits to the reachable zone cell with most separation plus open neighbors,
  ignoring minor re-presses until arrival.
- Fully stuck (30 ticks pushing with zero cell progress) → `forceYield()`
  (`VillageScene.java:895` + `trackStuck()`, `VillageScene.java:873`): everyone
  in your facing path yields nearest-first, gates bypassed once. Talkers are
  never grabbed mid-dialogue.

## 6. Throttles

- Minute gate: every dodge sets `avoidCd = 3600` — at most one casual dodge per
  villager per minute. Emergencies (stuck, corner, break-away) bypass it once
  and re-arm it.
- Anti-jitter `asideCd`: `48` ticks casual, `10` chased.
- Atomic steps: logical cell + `solid[][]` move at step begin
  (`beginNpcStep()`, `VillageScene.java:1392`), render lerps over
  `NPC_STEP_TICKS = 18` (`stepNpc()`, `VillageScene.java:1408`) — two walkers
  can never route into each other mid-tween, and you can't step into an
  in-flight cell.

## 7. Collision, render, validation

- Collision: villagers ride `solid[][]` dynamically (set at build, moved with
  every step), so your `startStep()`, the F3 red overlay, BFS, and `printMap`
  all stay true with zero special cases. Exactly one solid per villager, never
  shared, never dropped (headless-verified).
- Render: `drawNpc()` (`VillageScene.java:1841`) draws at the tweened position
  with a facing tick in your nose-dot language; Y-sort uses `renderCy + 6`
  (`VillageScene.java:1507`), same as you.
- Talk: `interact()` (`VillageScene.java:833`) — facing cell match freezes them
  (`stepping` cut, render re-synced), stage line shows, stage advances, expiry
  returns them to `IDLE`.
- Static asserts still cover spawn cells only; runtime guards
  (`npcRestFree()`, `npcStepBlocked()`, door/entrance bans) cover walkers.

## 8. Tick constants (60 UPS)

`NPC_STEP_TICKS = 18`, `NPC_BLOCK_DIST = 2`, `NPC_FOLLOW_DIST = 4`,
`NPC_RESUME_DIST = 4`, idle `120–300`, rest `120–360`, talk box `300`,
stuck `30`, push threshold `90`, minute gate `3600`
(`VillageScene.java:31-34`).

## 9. Proven headless

Wander ranges legal (MIRA `c6-14/r9-19`), `MOVE_ASIDE@12` vacates `@30`, talk
shows stage 0 then `IDLE`, 5 cells solid with no dupes, tail dodge first tick,
pinch squeeze `PASS_THROUGH@0` settling past you, pair-wall push-through,
corner exit at tick 14, break-away commit mid-chase, 2500-tick hunts with zero
illegal cells. Scatter run: Sol `7-11`, Bram `8-12`, ODD `13-22`, MIRA
`18-22`, Pip holds gate while you stand at spawn (by design).
