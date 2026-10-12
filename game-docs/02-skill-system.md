# Chronoseria: Skill System Design Document

## 1. Overview

Chronoseria features a specialized skill system that allows players to develop unique abilities for combat, travel, and exploration. These skills provide practical advantages, such as detecting hidden treasures, avoiding dangerous encounters, unlocking restricted areas, increasing combat damage, and resisting harmful status effects.

Players can discover specialized mentors in different villages, kingdoms, and areas throughout the world. Each mentor specializes in teaching a particular skill, encouraging players to explore different locations to discover new abilities and training opportunities.

Every skill can be trained from Level 1 to Level 50. Depending on the skill, higher levels can increase its duration, effectiveness, detection range, damage bonus, or resistance.

The skill system encourages strategic decision-making because players can equip and train only two skills at a time.

## 2. Core Skill System Rules

- **Skill Levels:** Each skill can be trained from Level 1 to Level 50, with Level 50 as the maximum.
- **Skill Limit:** Players can equip and train only two skills at a time.
- **Skill Duration:** Temporary skills have durations that depend on their training levels. Higher levels may extend their duration or improve their effects.
- **Skill Applications:** Skills can be used during combat, travel, or exploration, depending on their purpose.
- **Mentor Specialization:** Each mentor specializes in a particular skill. Players must explore different locations to discover mentors and learn their abilities.
- **Strategic Choices:** Players must choose two skills to develop and equip based on their preferred playstyle and current objectives.

## 3. Exploration and Travel Skills

### 3.1 Lockpicking

**Category:** Exploration

Allows players to unlock doors and other locked areas without requiring a conventional key. It provides access to restricted locations, optional routes, and potentially valuable rewards.

Higher levels may allow players to open more difficult locks or improve their success rate.

### 3.2 Telepathy

**Category:** Communication and Exploration

Allows players to perceive thoughts or communicate mentally with certain characters, depending on the situation. It may reveal hidden information, private thoughts, or clues that cannot be obtained through ordinary conversations.

Higher levels may expand its range, duration, or effectiveness.

### 3.3 Extended Instinct

**Category:** Awareness and Exploration

Enhances the player's awareness of the surrounding environment. It may help identify nearby dangers, suspicious locations, environmental clues, or unusual activity during exploration.

Higher levels may improve detection range or reveal more detailed information about nearby surroundings.

### 3.4 Treasure Sense

**Category:** Exploration

Allows players to detect hidden chests and nearby treasures that might otherwise remain unnoticed. It encourages exploration and rewards players who investigate their surroundings.

Higher levels may increase detection range or improve the ability to locate concealed treasures.

### 3.5 Danger Sense

**Category:** Travel and Exploration

Allows players to detect potential enemy encounter areas while traveling. When activated, dangerous areas appear as red circles on the travel map, allowing players to identify and avoid them when possible.

Higher levels may increase detection range or provide more information about nearby threats.

### 3.6 Encounter Veil [Added from port docs]

**Category:** Travel

Suppresses random encounters for a number of steps instead of seconds, matching the step-counter plus 0–255 roll loop (see `../docs/12-encounters-spawns.md`, `EncounterSystem.step()`/`roll()`).

- Higher levels extend safe steps and may widen the safe radius on the travel map.
- Never blocks scripted boss fights, story battles, or designated special encounters.
- Effect pauses when travel is paused, dialogue is open, or the game is otherwise not advancing.
- Replaces the former anti-encounter consumable (see `00-gameplay-improvements.md` §3.2): avoidance now costs a skill slot, keeping the two-skill choice meaningful.

### 3.7 Pathfinder [Added from port docs]

**Category:** Travel and Exploration

Reveals warp, shortcut, and stair tiles on the local map, drawn from tile-query and chapter-map structure (see `../docs/09-tile-anim-k.md`, `../docs/13-story-arcs-PACK0-4.md`).

Higher levels increase reveal radius and may mark one-way or hidden exits. Complements Treasure Sense and Danger Sense without replacing them.

## 4. Combat Skills

### 4.1 Lifesteal

**Category:** Combat and Recovery

Allows players to recover HP by dealing damage to enemies. A portion of the damage dealt is converted into HP, helping players survive longer without relying entirely on healing items or magic.

Higher levels may increase the percentage of damage converted into HP.

### 4.2 Magic Power Steal

**Category:** Combat and Resource Recovery

Allows players to recover MP by dealing damage to enemies. A portion of the damage dealt is converted into MP, helping players maintain their ability to use magic during prolonged battles.

Higher levels may increase the amount of MP recovered from attacks.

### 4.3 Poison Resistance

**Category:** Combat and Status Resistance

Reduces the chance or severity of poison inflicted by enemy attacks. Depending on the skill level, it may reduce poison damage over time, shorten its duration, or eventually provide complete immunity.

### 4.4 Petrification Resistance

**Category:** Combat and Status Resistance

Reduces the chance of becoming petrified by enemy attacks or abilities. Petrification may prevent a character from performing actions during battle.

Higher levels may increase resistance until the player becomes completely immune to petrification.

### 4.5 Sleep Resistance

**Category:** Combat and Status Resistance

Reduces the chance of falling asleep due to enemy attacks or abilities. Since sleeping characters cannot perform normal combat actions, this skill helps maintain combat readiness.

Higher levels may increase resistance until the player becomes completely immune to sleep.

### 4.6 Confusion Resistance

**Category:** Combat and Status Resistance

Reduces the chance of becoming confused by enemy attacks or abilities. Depending on the combat rules, confusion may cause a character to target the wrong enemy or perform unintended actions.

Higher levels may increase resistance until the player becomes completely immune to confusion.

### 4.7 Weapon Master

**Category:** Combat and Offensive Enhancement

Increases the damage dealt by the player's equipped weapon. It benefits characters who rely on physical attacks and allows them to become more effective against powerful enemies.

Higher levels may increase the weapon damage bonus.

### 4.8 Berserk Mode

**Category:** Combat and Offensive Enhancement

Allows players to enter a temporary state of heightened aggression that increases their offensive power during battle. It is intended for players who prefer an aggressive approach to combat and want to defeat enemies more quickly.

Higher levels may increase the damage bonus or extend the skill's duration. Any defensive penalties or other drawbacks can be determined during balancing.

**Primary focus:** Increased offensive power.

### 4.9 Fury Mode

**Category:** Combat and Endurance Enhancement

Allows players to enter an extreme state of energy in which their endurance becomes locked at an abnormally high level for a limited duration. During this state, endurance-consuming actions may become easier to perform, with reduced or temporarily eliminated endurance limitations depending on the skill's level.

Higher levels may extend the duration or improve the skill's benefits.

**Primary focus:** Extreme energy and sustained combat activity.

### Difference Between Berserk Mode and Fury Mode

| Feature | Berserk Mode | Fury Mode |
|---|---|---|
| Primary benefit | Increased offensive power | Enhanced endurance availability |
| Main purpose | Deal more damage | Sustain endurance-consuming actions |
| Combat approach | Aggressive offense | Prolonged, high-energy combat |
| Level progression | Improves damage bonus or duration | Improves endurance effect or duration |

These are separate skills with different strategic purposes. Players may choose between them depending on whether they prioritize offensive strength or sustained action.

### 4.10 Mana Barrier [Added from port docs]

**Category:** Combat and Shield

Creates a temporary damage shield that absorbs incoming damage before HP is reduced. Mentioned in the village mentor examples (see `01-village-buildings.md`).

Higher levels increase absorb amount and duration. Pairs as a defensive alternative to Berserk Mode and Fury Mode aggression.

### 4.11 Rune Focus [Added from port docs]

**Category:** Combat and Offensive Enhancement

Channels rune energy into a short attack surge, adding a small tier-scaled bonus derived from the growth-curve and damage formula (see `../docs/10-data-b-i.md`, `../docs/11-battle-system.md`).

Higher levels extend turns or raise the bonus. Gives Weapon Master a tactical, windowed alternative.

### 4.12 Swift Recovery [Added from port docs]

**Category:** Combat and Recovery

Speeds unconsciousness recovery ticks from the recovery-bar system (see `00-gameplay-improvements.md` §4.1, `../docs/11-battle-system.md`).

Higher levels shorten ticks to consciousness and may restore slightly more HP on wake. Does not replace treatment for poison, illness, or other harmful conditions.

## 5. Skill Progression and Balancing

All skills follow the Level 1 to Level 50 progression system. However, their improvements depend on their specific functions.

- **Offensive skills:** Improve damage or other offensive benefits.
- **Recovery skills:** Improve HP or MP recovery.
- **Resistance skills:** Reduce the chance, severity, or duration of status effects, potentially granting immunity at higher levels.
- **Detection skills:** Improve detection range or information revealed.
- **Temporary enhancement skills:** Improve their effects or duration.

Skills should be balanced to prevent a single ability from becoming the best choice for every situation. Powerful effects, such as complete status immunity or unlimited endurance, should be carefully evaluated during testing.

Lifesteal and Magic Power Steal should also have defined recovery limits to prevent excessive or unlimited resource regeneration.

Tuning values moved to `06-recommendations-future-work.md`.

## 6. Design Status

This document establishes the initial skill concepts, categories, and core rules for Chronoseria.

> Recommendations moved to `06-recommendations-future-work.md`.
