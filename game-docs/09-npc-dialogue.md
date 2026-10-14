# Chronoseria: NPC Dialogue — Panel Module

Game design + implementation document · How villager conversations open,
advance, and close. Roster lines are quoted verbatim. Mind FSM lives in
`08-npc-villagers.md`; placement in `07-asset-arrangements.md`. Code:
`src/main/java/runes/Dialogue.java` (panel),
`src/main/java/runes/VillageScene.java` (`interact()`, HUD).

## 1. Roster lines (all stages, in order)

| Villager | 1 | 2 | 3 |
|---|---|---|---|
| MIRA | “Good day! The village is peaceful today.” | “The inn is north up the road if you need a rest.” | “It is nice to see new faces around here.” |
| Bram | “Fountain's east. Shops are warm.” | “Shops line the north ridge. Mind the walls.” | “Odd says stay on the road. He's right.” |
| ODD | “Stay on the road.” | “The gate is one tile wide. Single file.” | “Walls are solid. Don't bother pushing.” |
| Pip | “South road goes everywhere.” | “I saw the fountain glittering yesterday.” | “Plaza's west. Can't miss it.” |
| Sol | “Knock twice at the Inn.” | “Blue roof, north side. That's the one.” | “Quiet today. Good.” |

Lines live in each `Npc.lines` (`VillageScene.java:417-441`); `Npc.stage`
cycles forever, so repeat chats rotate through all three.

## 2. Panel flow

```text
Face villager + E → open (line = current stage) → E → next line → E → next
→ E past last line → CLOSE (box clears, villager resumes IDLE next tick)
```

- One conversation shows every line from the current stage, then shuts.
- `E` with an open panel always advances/closes first — it never starts a
  second chat or reads a door behind the box.
- Prompt hint bottom-right of the box: `E: next 1/3` … `E: done`.
- Buildings use the same box (their blurb) but never open the panel: no hint,
  300-tick expiry as before.

## 3. Close rules

| Closes when | Effect |
|---|---|
| `E` past the final line (“done”) | Box clears, `talkingNpc` released, villager `IDLE` |
| Walking >2 tiles from the speaker | Auto-close, box clears, villager `IDLE` (no frozen boxes mid-map) |
| Timer expiry | Only for building blurbs/hints — an open villager panel never expires |

Talking freezes the routine (`stepping` cut, render re-synced, faces you) and
`TALKING` exits only through a close path, never the timer.

## 4. Quest hooks (later)

- `Npc.stage` counts shown lines — gate quest flags on thresholds per speaker.
- Add per-NPC `flags` beside `stage` when the first quest lands; the open →
  advance → close skeleton stays untouched.

## 5. Code map

`Dialogue.java`: `open()`, `advance()` (text or null = just closed),
`close()`, `isOpen()`, `isLast()`, `hint()`, `speakerCol/Row()`.
`VillageScene.interact()`: panel-first E routing, door blurbs, talk-to-face
match. `VillageScene.update()`: walk-away check, timer freeze while open.
`VillageScene.drawHud()`: box + hint.

## 6. Proven headless

Open line 1 → survives 400 ticks unexpired → lines 2, 3 → hint `E: done` →
4th E closes with empty box and resumed villager; walk-away shuts and clears.
