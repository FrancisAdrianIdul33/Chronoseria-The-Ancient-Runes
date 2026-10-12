# Chronoseria: Village and Kingdom Buildings

Game design document · Exploration hubs and player services.

Villages and kingdoms act as safe hubs where players recover, gather rumors, revive allies, shop for gear, learn magic, stock supplies, and train field skills. Each building has a single clear purpose so new players can navigate without a tutorial wall.

## Building roster

| No. | Building Name       | Purpose                                                                      |
| --- | ------------------- | ---------------------------------------------------------------------------- |
| 1   | Inn                 | Restores HP, MP, and endurance through sleeping.                             |
| 2   | Tavern              | Allows players to interact with villagers, hear rumors, and purchase drinks. |
| 3   | Sacred Sanctuary    | Revives unconscious or dead party members.                                   |
| 4   | Blacksmith          | Sells weapons, armor, and protective equipment.                              |
| 5   | Wizard's Domain     | Teaches magic skills and abilities.                                          |
| 6   | Conventional Market | Sells potions, antidotes, and other supplies.                                |
| 7   | Mentor's House      | Allows players to learn and train specialized skills for combat, travel, and exploration. |

## Building details

### 1. Inn — rest and recovery
- Full restore: HP, MP, and endurance after sleeping.
- Cost scales lightly by arc so early game stays friendly and late game keeps TIC meaningful.
- Sleeping advances time to morning and triggers any overnight NPC dialogue refresh.

### 2. Tavern — rumors and villagers
- Talk to villagers, hear rumors, and purchase drinks.
- Rumors feed the keyword archive (see `00-gameplay-improvements.md` §2.1) and hint at hidden chests, optional bosses, and next destinations.
- Drinks give small temporary buffs; prices stay low so the tavern remains a social stop, not a power shop.

### 3. Sacred Sanctuary — revival (Seraphim Dial)
- Revives unconscious or dead party members: Seraphim Dial rite at the Sanctuary (full HP, fee-based); a carried Seraphim Dial also revives in the field at 25% HP.
- Ties into the unconsciousness and recovery system (`00-gameplay-improvements.md` §4.1): field treatment stabilizes, sanctuary fully revives.
- Fee or offering required; cost grows with party level to keep death meaningful.

### 4. Blacksmith — gear
- Sells weapons, armor, and protective equipment.
- Shows stat preview before purchase/equip (`00-gameplay-improvements.md` §5.1).
- Stock rotates per village/kingdom tier; best gear stays in hidden chests or late kingdoms.

### 5. Wizard's Domain — magic
- Teaches magic skills and abilities.
- Unlocks by progression flags; advanced spells gated behind arcs or mentor hints.
- Explains MP cost, element, and status effects before learning.

### 6. Conventional Market — supplies
- Sells potions, antidotes, and other supplies.
- Categories: healing, combat, quest-safe; quest items can never be sold (`00-gameplay-improvements.md` §5.1).
- Stocks cure items matching local dungeon status effects so players can prepare.

### 7. Mentor's House — skill system
- Learn and train specialized skills for combat, travel, and exploration.
- Rules and definitions live in `02-skill-system.md` (L1-50, 2-slot equip/train, one mentor per skill).
- Roster: Lockpicking, Telepathy, Extended Instinct, Treasure Sense, Danger Sense, Encounter Veil, Pathfinder, Lifesteal, Magic Power Steal, resistances, Weapon Master, Berserk Mode, Fury Mode, Mana Barrier, Rune Focus, Swift Recovery — see `02` §§3-4.

## Design links
- Recovery and status treatment: `00-gameplay-improvements.md` §4.
- Inventory categories and equip preview: `00-gameplay-improvements.md` §5.1.
- Rumors and keywords: `00-gameplay-improvements.md` §2.1–2.3.
- Port source for shop/menu states: `../docs/06-base-h-and-states-d-e.md`, `../docs/14-maps-assets.md`.
