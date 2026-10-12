# 13 — Story Arcs — PACK0..PACK4 (75 files)

## Layout (verified via dir)
- PACK0: 15 files, PACK1: 15, PACK2: 15, PACK3: 15, PACK4: 15 = 75 total.
- Per-PACK sizes follow same pattern: `_0 ~300b` (header), `_12 ~150b` (index), `_13 ~450b`, `_9 ~550b`, `_7 ~800-1300b`, `_14 ~1400b`, `_2 ~2500b`, `_4 ~3200b`, `_6 ~3000b`, `_11 ~7-8KB`, `_5 ~9-10KB`, `_8 ~10KB`, `_1 ~10-12KB`, `_3 ~5KB`, `_10 ~4-5KB`.

Example PACK0:
`_0 310, _1 10280, _2 2492, _3 5035, _4 2944, _5 8607, _6 2825, _7 855, _8 9235, _9 574, _10 4110, _11 7409, _12 155, _13 411, _14 1381`

## Interpretation
- `_0` = chapter header (id, map count, start flag).
- `_12,_13,_9,_7` tiny = dialogue index / warp list / shop list.
- `_2,_4,_6,_14` small = scripts/events (a(int,String) loader in `a` class).
- `_10,_3` mid = tilesets for chapter.
- `_11,_5,_8,_1` large = maps + encounters + sprites for chapter.
- 5 PACKs = 5 story arcs (e.g., Prologue, 3 rune quests, Finale — rename for Ancient Runes).

## Progression (from i flags + g states)
- `boolean[50]` = story flags. `short[86][2]` = (chapter,event) triggers.
- `c.a(stage,room)`, `g.a(stage,room,flag)` transitions load `PACKn_m` via `a.a(String)`.
- Boss at end of each PACK (one-shot from 86-table) unlocks next PACK.

> Recommendations moved to `00-recommendations-future-work.md`.
