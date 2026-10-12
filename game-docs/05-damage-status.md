# Chronoseria: Damage and Status Effect Reference

Game design document · Damage types, status effects, and their links to skills and items. Currency in TIC (`03-currency-tic.md`), recovery items in `04-items-supplies.md`, resistance skills in `02-skill-system.md`.

## 1. Damage types

These types describe how an attack damages an enemy or player. Both enemies and playable characters can use or receive them, depending on their resistances and immunities.

One important distinction: damage reduces a character's HP or other resources, while a status effect changes how a character performs or behaves. Some effects can do both.

### 1. Physical Damage

HP damage. Damage caused by swords, axes, spears, bows, claws, punches, and other physical attacks.

- **Effect:** Reduces HP based on attack power, defense, and other modifiers.
- **Counter:** Armor, shields, physical defense buffs, evasion, and guarding.

### 2. Magic Damage

HP damage. Damage caused by spells, magical projectiles, curses, and arcane attacks.

- **Effect:** Reduces HP based on magical power, magic defense, and resistances.
- **Counter:** Magic defense, elemental resistance, barriers, spell reflection, or interruption.

### 3. Elemental Damage

HP damage. Damage associated with elements such as fire, ice, lightning, water, wind, earth, and light.

- **Effect:** Deals damage according to the element's power and the target's resistance or weakness.
- **Counter:** Elemental armor, resistance skills, protective spells, or equipment.
- **Special rule:** Elements can interact with status effects. For example, lightning might have a chance to inflict paralysis.

### 4. Dark or Curse Damage

HP or resource damage. Damage caused by dark magic, curses, or corrupted energy.

- **Effect:** Damages HP and may weaken the target or drain another resource.
- **Counter:** Curse resistance, purification, protective magic, or specialized equipment.

### 5. Soul Damage

Special damage. Damage that attacks a character's spiritual essence rather than only their physical body.

- **Effect:** Reduces HP, weakens soul-related attributes, or makes the target more vulnerable to spiritual attacks.
- **Counter:** Soul protection, spiritual healing, purification, or special restorative items.

### 6. Temporal Damage

Special damage. Damage caused by manipulating time or accelerating the target's physical deterioration.

- **Effect:** Deals damage directly or temporarily reduces speed, action frequency, or the duration of beneficial effects.
- **Counter:** Temporal resistance, time-protection spells, or rare restorative items.

### 7. Piercing or True Damage

Special damage. Damage that bypasses some or all of the target's defense.

- **Effect:** Ignores the specified portion of physical or magical defense.
- **Counter:** Evasion, damage reduction, barriers, or effects that specifically reduce true damage.
- **Balance rule:** True damage should be rare or limited to prevent defense from becoming useless.

**System split:** Physical, magic, and elemental damage are the main system. Dark, soul, temporal, and true damage are advanced mechanics for particular skills, bosses, rare weapons, or late-game content.

## 2. Status effects

Status effects are divided into five groups: physical impairment, mental impairment, bodily conditions, spiritual deterioration, and temporal effects. Seventeen effects total.

### A. Physical impairment

**1. Sleep** — The target falls asleep and cannot perform normal actions until awakened. *Solution:* an awakening item or spell, a valid damaging hit, or wait for duration to expire.

**2. Petrification** — The target turns to stone and cannot act. Depending on the game's rules, they may also become immune to ordinary damage or take increased damage from specific attacks. *Solution:* a Stone Cure item, a restoration spell, or a special sanctuary service. Immunity to damage while petrified should be a deliberate balance choice.

**3. Paralysis** — The target temporarily loses the ability to move or perform actions. It may occasionally recover and act, depending on severity. *Solution:* a Paralysis Cure item, a restorative spell, or wait for the effect to end.

**4. Stun** — The target loses their next action or a short period of action time after a powerful blow or disruptive attack. *Solution:* usually expires quickly. Resistance to stun or specific recovery skills can reduce its chance or duration.

**5. Exhaustion** — The target's endurance is severely reduced, limiting endurance-consuming actions such as physical skills, dodges, or special attacks. *Solution:* a Tonic, rest at an Inn, or an endurance-restoring skill.

**6. Blindness** — The target's vision is impaired, reducing accuracy and hiding traps or hidden details. *Solution:* Cleansing Water, a cleansing spell, or wait for the effect to end.

### B. Mental impairment

**7. Confusion** — The target may attack the wrong enemy, target an ally, or perform an unintended action. *Solution:* a Confusion Cure item, a clarity spell, or wait for the effect to expire.

**8. Silence** — The target cannot cast spells or use abilities that require speech or chanting. *Solution:* a Silence Cure item, a cleansing spell, or wait for the effect to end.

**9. Fear** — The target may suffer reduced accuracy, weaker attacks, or a chance to lose their turn or flee. *Solution:* a Courage item or morale-restoring spell, or wait for the effect to expire.

**10. Charm** — The target temporarily fights for the caster or refuses to attack the caster, depending on the rules. *Solution:* a Charm Cure item, a purification spell, or wait for the effect to end.

### C. Bodily conditions and deterioration

**11. Poison** — Deals damage over time, usually at the end of each turn or at fixed intervals. *Solution:* an Antidote, a cleansing spell, or a poison-resistant skill.

**12. Fever** — A bodily condition that gradually reduces HP or endurance and may lower physical performance. *Solution:* a Fever Remedy, a healing spell designed for bodily ailments, or rest under appropriate conditions.

**13. Body Withering** — A severe curse that gradually weakens the body, reducing maximum HP, physical attack, or defense. *Solution:* a Withering Cure, powerful restoration magic, or a Sacred Sanctuary service designed to treat severe curses.

### D. Spiritual and resource impairment

**14. MP Drain** — Removes MP immediately or over time, reducing the target's ability to cast spells. *Solution:* an Ether, a mana-restoring skill, or a recovery service.

**15. Curse** — Applies a harmful magical condition. It might reduce healing received, weaken attributes, or cause periodic damage. *Solution:* a Curse Cure item, purification magic, or a designated sanctuary service.

### E. Temporal effects

**16. Slow** — Reduces action speed, delays turns, or lowers the frequency of actions. *Solution:* a Haste spell, a Time Restoration item, or wait for the effect to expire.

**17. Temporal Aging** — A rare curse that causes the target's body to deteriorate through accelerated time, reducing attributes or maximum HP temporarily. *Solution:* a Temporal Restoration item or advanced time magic. Permanent aging, if ever used, should be reserved for major story consequences.

## 3. Severe and special status effects

These effects are more dangerous than ordinary status conditions and should be rare or require specific enemy abilities.

| Effect | What It Does | Solution |
| --- | --- | --- |
| Instant Death | Immediately defeats the target, regardless of remaining HP, unless protected. | Revive with a revival item or Sacred Sanctuary; Death Resistance can reduce the chance of the effect. |
| Doom | Marks a target with a countdown. If the countdown expires, the target dies. | Use a Doom Cure, defeat the caster if the effect permits it, or revive the target after death. |
| Petrification | Prevents normal actions by turning the target to stone. | Stone Cure or restoration magic. |
| Soul Severance | Temporarily disconnects the target from their spiritual power, disabling certain abilities or reducing maximum MP. | Soul restoration or advanced purification. |
| Body Withering | Reduces maximum HP or physical attributes over time. | Withering Cure or powerful restoration. |
| Skill Seal | Prevents the use of selected skills or all special abilities. | Skill Seal Cure or purification magic. |
| Life Leech | Drains HP over time and transfers some of it to the attacker. | Remove the effect with a cleansing item or spell; defeat the caster if the effect ends on their defeat. |
| Dead | Terminal state when the recovery bar reaches zero. Seraphim Dial revives in the field (25% HP); the Sanctuary fully revives. | Seraphim Dial (field, 25%) or Sanctuary (full, fee-based). |

Important distinction: Instant Death is a defeat effect, not ordinary HP damage. Doom is a delayed defeat effect. Petrification and Soul Severance are status effects that may lead to defeat without directly reducing HP.

## 4. Cure mapping (items owned by 04)

Item prices, availability, and full definitions live in `04-items-supplies.md` §2 (source of truth, no duplication here).

Status → cure → service:
- Poison → Antidote → Market; Petrification → Gold Needle → Sanctuary (severe); Sleep → Dawn Bell → Market; Confusion → Clarity Dust → Market; Paralysis → Shock Seed → Market; Silence → Sonic Beans → Market; Fear → Holy Cross → Market; Charm → Reality Petals → Sanctuary; Fever (illness) → Winter Pearl → Inn/Market; Curse / Body Withering → Holy Water → Sanctuary; Slow / Doom → Magic Sand → Sanctuary; Unconsciousness → Seraphim Dial (field, 25%) or Sanctuary (full); Dead → Seraphim Dial (field, 25%) or Sanctuary (full); Blindness → Cleansing Water → Market.
- Sanctuary Revival (service, fee-based, full HP) stays at the Sacred Sanctuary (`01-village-buildings.md` §3) — it is a service, not a stocked item.

Rules: remedies cure but grant no immunity (resistances are skill-based, `02` §§4.3–4.6); field revival never restores full HP; serious illness/poisoning still requires treatment before recovery completes (`00-gameplay-improvements.md` §4.1).

## 5. Rules for implementing status effects

1. **Duration:** Every temporary status effect should have a defined duration, such as a number of turns or seconds.
2. **Stacking:** Reapplying the same effect should refresh its duration or increase its severity only when explicitly allowed.
3. **Resistance:** Resistance skills and equipment should reduce the chance of being afflicted or reduce the effect's duration.
4. **Immunity:** High-level resistance may grant immunity to selected status effects, but not automatically to every effect.
5. **Boss protection:** Major bosses may resist or ignore certain effects, but should not be universally immune to every status.
6. **Cleansing:** Items, spells, skills, and services should have clearly defined roles in removing effects.
7. **Defeat and revival:** Revival should remove only the effects specified by the rules, rather than automatically clearing every condition.
8. **Player readability:** The interface should clearly display the effect's name, icon, remaining duration, and any important gameplay consequence.

> Core/advanced split moved to `06-recommendations-future-work.md`.

## 6. Skill, item, and service cross-reference

Every status effect should be addressable through at least one path: a resistance skill (prevents), a cure item (removes), or a service (severe cases). Resistances are skill-based (`02-skill-system.md` §§4.3–4.6); cures are item-based (`04-items-supplies.md` §2); full revival and severe-curse treatment stay at the Sacred Sanctuary (`01-village-buildings.md` §3).

> Legend: `—` = future resistance (see `06-recommendations-future-work.md`).

| Status effect | Resistance skill (`02`) | Cure item (`04` §2) | Service |
| --- | --- | --- | --- |
| Poison | Poison Resistance (4.3) | Antidote | Market |
| Petrification | Petrification Resistance (4.4) | Gold Needle | Sanctuary (severe) |
| Sleep | Sleep Resistance (4.5) | Dawn Bell | Market |
| Blindness | — | Cleansing Water | Market |
| Confusion | Confusion Resistance (4.6) | Clarity Dust | Market |
| Paralysis | — | Shock Seed | Market |
| Silence | — | Sonic Beans | Market |
| Fear | — | Holy Cross | Market |
| Charm | — | Reality Petals | Sanctuary |
| Fever / Illness | — | Winter Pearl | Inn / Market |
| Curse | — | Holy Water | Sanctuary |
| Body Withering | — | Holy Water (partial) | Sanctuary |
| Slow / Doom | — | Magic Sand | Sanctuary |
| Unconsciousness | Swift Recovery (4.12) | Seraphim Dial (field, 25%) | Sanctuary (full) |
| Dead | — | Seraphim Dial (field, 25%) | Sanctuary (full) |
| Instant Death | — Death Resistance | — (revive only) | Sanctuary |
| Stun / Exhaustion | Fury Mode (4.9, endurance) | Tonics / Inn rest | Inn |

### Damage ↔ skill ↔ item interplay

- **Physical** — Weapon Master and Berserk Mode (`02` §§4.7–4.8) boost output; armor, shields, and Guard defend against it. Potions restore HP lost.
- **Magic / Elemental** — Mana Barrier (`02` §4.10) softens incoming; Rune Focus (§4.11) surges elemental hits. Ethers restore MP spent.
- **Dark / Soul** — Purification services and Holy Water address curses; Mana Barrier and Soul Restoration counter drain. Rare, late-game focused.
- **Temporal** — Magic Sand and Sanctuary treat these; reserved for bosses and rare skills per §1.
- **True Damage** — Evasion and damage-reduction only; no skill or armor scales against it. Kept rare by rule.

### Recovery interplay

Lifesteal and Magic Power Steal (`02` §§4.1–4.2) recover HP/MP by dealing damage but must have defined caps (`02` §5) so Potions and Ethers (`04` §1) stay relevant. Swift Recovery (`02` §4.12) speeds the unconsciousness recovery bar (`00` §4.1); Seraphim Dial revives in the field (25% HP), the Sanctuary fully revives.

## 7. Design Status

**Established**

- Seven damage types with counters and a main/advanced split.
- Seventeen status effects plus eight severe/special effects.
- Fourteen cure/revival items with TIC prices and availability.
- Eight implementation rules for duration, stacking, resistance, immunity, and readability.
- Full skill ↔ item ↔ service cross-reference.

> Open items moved to `06-recommendations-future-work.md`.

