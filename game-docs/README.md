# game-docs/ — Chronoseria active spec (working folder)

> `docs/` is frozen reference (other game's logic). This folder is the game plan.

| File | Topic | Owns (source of truth) |
| ---- | ----- | ---------------------- |
| `00-gameplay-improvements.md` | Gameplay spec | Systems, survival, pacing |
| `01-village-buildings.md` | Hubs & services | Buildings, shops, services |
| `02-skill-system.md` | Skills | Skill rules + definitions (L1-50, 2-slot) |
| `03-currency-tic.md` | Currency | TIC identity, format, rules |
| `04-items-supplies.md` | Items | Item prices, availability (`§1-§3`) |
| `05-damage-status.md` | Damage & statuses | Status definitions, rules, cross-ref |
| `06-recommendations-future-work.md` | Backlog | Priorities, TBD, tuning |
| `07-asset-arrangements.md` | Tile placement | Block coords, layers, collision |

Rules:
- One source of truth per topic (see Owns column). Others link, don't duplicate.
- Prices live in `04`. Statuses live in `05`. Skills live in `02`.
- Currency is always TIC, format `N TIC`.
- Cross-refs: `00-gameplay-improvements.md §X` inside this folder, `../docs/XX-....md` for reference.
