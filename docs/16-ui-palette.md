# 16 — UI Palette — Chronoseria: The Ancient Runes (60-30-10)

Source of truth: `src/main/java/runes/GamePalette.java` (JDK 26, Swing).

## Tokens
- PARCHMENT #E8D8B0 — 60%: menu bg, dialogue box, cards
- PARCHMENT_DARK #C9B88A — borders, unselected buttons
- ROYAL_BLUE #253653 — 30%: headers, status bar, dialogue text/border
- ROYAL_DEEP #1A2538 — title gradient top, game viewport bg
- LEGENDARY_GOLD #C69A3A — 10%: selection, rune ring, HP border, accents
- CRIMSON_RED #8F3434 — enemy dmg, danger, low HP
- FOREST_GREEN #477A52 — heals, HP fill, safe
- ARCANE_BLUE #638EB8 — ice/mana/water
- MYSTIC_VIOLET #A66AC2 — dark magic, curses, rare

## Screen map
- TITLE: gradient ROYAL_DEEP->ROYAL_BLUE, ring GOLD, title PARCHMENT, subtitle + PRESS ENTER GOLD
- MENU: PARCHMENT bg, ROYAL_BLUE header, cards PARCHMENT_DARK unselected / GOLD selected, text ROYAL_BLUE
- GAME: ROYAL_DEEP viewport, ROYAL_BLUE status bar (PARCHMENT text, HP bar FOREST_GREEN on PARCHMENT_DARK, GOLD border), dialogue PARCHMENT box + ROYAL_BLUE border + GOLD corners

## Rules
- Never hardcode hex in panels — import GamePalette.
- Sprites get separate pixel palette later; UI theme does not constrain them.
- Feedback colors only for combat numbers/effects, not layout.
