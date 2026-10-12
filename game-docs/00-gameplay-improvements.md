# Chronoseria: Gameplay Spec

Game development design document · Classic fantasy turn-based RPG

Chronoseria is a classic fantasy RPG inspired by early Japanese role-playing games. Its goal is to preserve the nostalgic experience of pixel-art graphics, turn-based combat, exploration, warriors, monsters, magic, and ancient mysteries while improving quest guidance, gameplay clarity, strategic depth, and mobile usability.

The following spec consolidates the design into a structured plan for development.

## 1. Quest and progression systems

1.1 Quest Journal and Objective Tracking

Maintain a quest journal containing active quests, completed quests, side quests, and important quest updates.

- Display the current main objective.
- Record quest progress and completed objectives.
- Provide optional hints without revealing every solution.
- Update the journal automatically when important events occur.

1.2 Post-Boss Progression Guidance

After a major boss battle, inform players of the consequences and what they can investigate next.

- Display a story update or new objective.
- Introduce clues about the next destination.
- Update relevant NPC dialogue and quest records.
- Allow players to discover optional content without forcing a single route.

1.3 World Map and Location Guidance

Provide a world map and local maps to help players navigate towns, dungeons, forests, and other locations.

- Mark discovered locations and landmarks.
- Indicate relevant destinations when appropriate.
- Allow players to review previously visited places.
- Preserve undiscovered areas and optional secrets.

## 2. Dialogue, lore, and NPC interaction

2.1 NPC Keyword Archive

Add a dedicated Keywords tab inside the player's panel or menu. It automatically records important names, locations, clues, and terms mentioned by NPCs.

- Group keywords into quests, characters, locations, ancient runes, and history.
- Display the NPC who mentioned each keyword and its dialogue context.
- Highlight keywords related to current objectives.
- Avoid recording ordinary words that would clutter the archive.

2.2 Lore Codex and Dialogue History

Provide a reference library for characters, monsters, kingdoms, locations, ancient runes, and historical events.

- Save important conversations and clues.
- Summarize major story developments.
- Allow players to revisit discovered lore.
- Connect relevant information to quests and keywords.

2.3 Reactive NPC Dialogue

Make the world respond to the player's progress.

- Change dialogue after important boss battles.
- Let NPCs provide rumors, optional quests, and useful hints.
- Allow towns to react to major story events.
- Preserve important dialogue in the keyword archive.

## 3. Combat and enemy encounter systems

3.1 Better Combat Feedback and Enemy Weaknesses

Help players understand the results of their tactical decisions.

- Indicate effective, resisted, and ineffective attacks.
- Let players discover weaknesses through battles, NPC clues, and exploration.
- Record discovered enemy information in a bestiary.
- Keep some weaknesses hidden for experimentation and discovery.

3.2 Encounter Avoidance via Skill (supersedes former prevention item)

The former rare encounter-prevention consumable has been removed. Encounter avoidance now lives in exactly one place: the Encounter Veil skill — see `02-skill-system.md` §3.6.

Avoidance costs a skill slot (two-skill limit), keeping the choice strategic. Treasure chests return to gear, lore, and non-avoidance consumables.

3.3 Strategic Boss Encounters

Design bosses around tactical decisions rather than relying only on high HP or overwhelming damage.

- Introduce distinct attacks and battle phases.
- Provide clues before especially dangerous attacks.
- Reward defensive skills, buffs, healing, and status cures.
- Change boss behaviour as the battle progresses.

3.4 Distinct Party Roles and Character Progression

Give each character class a meaningful role in battle.

- Establish unique strengths, weaknesses, and abilities.
- Allow party formation adjustments, including front and back rows where appropriate.
- Encourage combinations of physical attacks, elemental magic, defence, and healing.
- Explain character attributes and ability effects.

## 4. Character survival, status effects, and recovery

4.1 Unconsciousness and Recovery System

When a character's HP reaches zero, the character becomes unconscious instead of immediately dying.

- Display a separate recovery bar beneath the HP bar.
- Make recovery progress slowly under normal conditions.
- Allow relevant attributes, including willpower and endurance, to influence recovery stability.
- Require treatment for serious illness, poisoning, or other harmful conditions.
- If recovery reaches zero before treatment, trigger Dead — revived by Seraphim Dial (field, 25% HP) or at the Sacred Sanctuary (full HP).
- Restore consciousness when recovery reaches its required threshold, with a small amount of HP.

The recovery bar represents a limited survival window, not a second HP bar.

4.2 Clearer Status Effects and Treatment

Explain the effects of poison, paralysis, illness, and other conditions.

- Display status descriptions in the character panel.
- Explain how each condition affects combat and recovery.
- Identify appropriate medicine, antidotes, and healing abilities.
- Make harmful recovery effects visible before they become fatal.

## 5. Inventory, exploration, and dungeon design

5.1 Improved Inventory Management

- Organize items into healing, combat, equipment, quest, and key-item categories.
- Display item effects and the conditions they cure.
- Add sorting, quantity indicators, and clear descriptions.
- Prevent accidental use or sale of important quest items.
- Preview equipment stat changes before equipping an item.

5.2 Meaningful Exploration Rewards

Reward curiosity with discoveries that provide more than ordinary currency or routine battles.

- Place rare equipment, consumables, lore, and unique abilities in hidden chests.
- Add optional dungeons, secret bosses, and shortcuts.
- Use environmental clues to reveal hidden rooms.
- Keep optional discoveries separate from mandatory progression.

5.3 Varied Dungeon Design

Make each major dungeon feel distinct.

- Introduce switches, environmental puzzles, traps, and shortcuts.
- Use enemy placement to teach new mechanics.
- Give dungeons distinctive visual themes and gameplay challenges.
- Provide understandable clues and avoid arbitrary puzzle solutions.

## 6. Difficulty, pacing, and encounter balance

6.1 Fair Difficulty and Resource Balance

- Test boss encounters against parties in the expected level range.
- Offer alternative strategies for under-levelled players.
- Make suitable healing supplies and equipment available before difficult battles.
- Reduce unnecessary grinding while preserving meaningful character progression.

6.2 Better Random Encounter Pacing

- Avoid excessive encounters during short trips.
- Consider visible enemies where practical.
- Vary enemy formations and battle conditions.
- Give players strategic options for managing encounters.
- Ensure travel remains dangerous enough to make preparation meaningful.

## 7. Tutorials, saving, and mobile usability

7.1 Better Tutorials and Discoverability

- Introduce mechanics gradually through gameplay.
- Allow players to review tutorials from the menu.
- Explain unfamiliar combat commands and status icons.
- Provide optional hints for difficult puzzles.

7.2 Saving and Progress Protection

- Add autosave at appropriate milestones.
- Provide manual save slots.
- Preserve quest progress, keywords, inventory, and character states.
- Define fair recovery rules if the entire party becomes unconscious.

7.3 Mobile-Friendly Interface

- Use large, touch-friendly controls.
- Support one-handed navigation where practical.
- Include confirmation prompts for consequential actions.
- Provide adjustable text speed, battle speed, and UI scaling.
- Ensure menus and dialogue remain readable on small screens.

> Recommendations moved to `06-recommendations-future-work.md`.
