# 17 — Branding — Chronoseria: The Ancient Runes

## Canonical name
- Full: `Chronoseria: The Ancient Runes` — always keep subtitle.
- Short: `Chronoseria` — only when space is tight (icon, save folder stays `.ancient-runes` for compat).
- No tagline.
- Test name `Emberfall Prologue` removed.

## Lockup
- Line 1: `CHRONOSERIA` — serif bold, parchment #E8D8B0, fade 0->1 over ~1s.
- Line 2: `THE ANCIENT RUNES` — sans, letterspaced, gold #C69A3A, fades after line 1.
- Line 3 (after 2s): `click any key` — lowercase sans, soft fade + glow pulse (gold under-glow alpha oscillates with tick). Not clickable before 2s.

## Opening flow
TITLE (fade title, then prompt at 2s) --any key/click--> MENU --select--> GAME.
ESC steps back GAME->MENU->TITLE.

## Menu (from docs 10/11/13/14)
- New Game — fresh Party b[4], flags bool[50] cleared (doc 10, 11)
- Load Game — via SaveManager, disabled if no save (doc 03)
- Continue — only if session save present
- Chronicles — story arcs PACK0-4 journal (doc 13)
- Bestiary — bool[255] seen list (doc 10)
- Settings — languages EN/FR/IT/DE/ES from I file + volume (doc 14)

## Palette lock
UI uses GamePalette only: parchment/royal/gold + feedback red/green/arcane/violet. No new brand colors.

## App naming
- Window: `Chronoseria: The Ancient Runes`
- Docs home: `docs/` in repo root (this folder).

## Game design docs
- Forward design spec lives in `game-docs/` (sibling to `docs/`): `00-gameplay-improvements.md` (quest journal, combat, recovery, inventory, difficulty, mobile, phases 1-3).
