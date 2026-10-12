# Recommendations & Future Work (extracted from game-docs/00-05)

> This file holds all recommendations, future proposals, and open/TBD items moved out of
> `game-docs/00-05` so those files stay spec-only.
> Source files are unchanged except for a pointer line.

## From 00-gameplay-improvements.md (§8, §9)
### Recommended development priorities (moved from §8)
Phase 1 — Core gameplay clarity. Build first:
- Quest journal and post-boss objectives
- World map and location guidance
- NPC keyword archive and dialogue history
- Basic combat feedback and status descriptions
- Inventory organization and saving

Phase 2 — Strategic gameplay systems. Add after core loop works:
- Unconsciousness and recovery mechanics
- Distinct party roles and enemy bestiary
- Meaningful exploration rewards
- Varied dungeons and strategic boss battles
- NOTE staleness fixed: `Rare encounter-prevention consumable` line removed — avoidance is skill-only via Encounter Veil (`02-skill-system.md` §3.6), per §3.2.

Phase 3 — Polish and balancing:
- Encounter frequency and difficulty balancing
- Reactive NPC dialogue and lore codex
- Mobile accessibility and interface settings
- Tutorial improvements and optional hints
- Autosave testing and recovery edge cases

### Final design principles (moved from §9)
1. Preserve mystery, reduce confusion.
2. Make difficulty fair.
3. Make information accessible.
4. Reward exploration.
5. Make survival strategic.
6. Respect mobile players.

## From 02-skill-system.md (§5 tuning, §6, §7)
- Balancing TBD: powerful effects such as complete status immunity or unlimited endurance must be evaluated during testing.
- Lifesteal and Magic Power Steal must have defined recovery limits.
- Encounter Veil step counts, Mana Barrier absorb values, Rune Focus bonuses, Swift Recovery tick rates require tuning against encounter pacing (`../docs/12-encounters-spawns.md`) and fair difficulty (`00-gameplay-improvements.md` §6).
- Future Skill Expansion principles:
  1. Every skill should have a distinct gameplay purpose.
  2. Skills should reward exploration, preparation, and strategic decisions.
  3. L1-50 progression should provide meaningful improvements.
  4. Two-skill limit should encourage choice without making other skills irrelevant.
  5. Skills should remain useful without making combat/exploration trivial.
- Design status open: exact skill costs, activation, cooldowns, durations, numerical bonuses, level-by-level progression remain TBD during implementation/balancing. System will continue to expand.

## From 04-items-supplies.md (§4, §5 open)
- Future categories reserved (items/prices not finalized): Status Recovery, Combat Enhancements, Defensive Items, Travel/Exploration Supplies, Rare Quest Items, Crafting Materials.
- Open: additional item types/prices, combat enhancement items, equipment prices/upgrade costs, selling prices and enemy/quest rewards, adjustments after playtesting.
- General rule kept in spec but flagged for tuning: recovery values and prices may be adjusted after playtesting.

## From 05-damage-status.md (§1 recommendation, §6, §8 open)
- Design recommendation: Physical/Magic/Elemental as main system; Dark/Soul/Temporal/True as advanced (boss/rare/late-game).
- Recommended core list for first game:
  - Core damage: Physical, Magic, Fire, Ice, Lightning, Dark, True.
  - Core statuses: Poison, Sleep, Blindness, Paralysis, Stun, Confusion, Silence, Petrification, Slow, Curse, Instant Death.
  - Advanced: Fever, Body Withering, Temporal Aging, Soul Severance, Doom.
- Open: status-effect resistance chart (chance/duration reduction per level); resistance skills for paralysis, silence, fear, charm, blindness, and death; Lifesteal/Magic Power Steal caps; exact durations, cooldowns, cure values after playtesting.
