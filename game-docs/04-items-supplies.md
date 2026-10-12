# Chronoseria: Items and Supplies

Game design document · Recovery inventory, remedies, and field supplies. All prices in TIC (see `03-currency-tic.md`). Source of truth for item prices and availability.

## 1. Proposed Recovery Items

Recovery items help players manage three primary character resources: HP, MP, and endurance.

### 1.1 HP Recovery: Potions

| Item          | HP Restored | Price   |
| ------------- | ----------- | ------- |
| Small Potion  | 50 HP       | 10 TIC  |
| Medium Potion | 500 HP      | 65 TIC  |
| Large Potion  | 1,500 HP    | 160 TIC |

### 1.2 MP Recovery: Ethers

| Item         | MP Restored | Price   |
| ------------ | ----------- | ------- |
| Small Ether  | 50 MP       | 10 TIC  |
| Medium Ether | 500 MP      | 65 TIC  |
| Large Ether  | 1,500 MP    | 160 TIC |

### 1.3 Endurance Recovery: Tonics

| Item         | Endurance Restored | Price   |
| ------------ | ------------------ | ------- |
| Small Tonic  | 50 Endurance       | 10 TIC  |
| Medium Tonic | 500 Endurance      | 65 TIC  |
| Large Tonic  | 1,500 Endurance    | 160 TIC |

### 1.4 Combined Recovery: Elixirs

Elixirs restore HP, MP, and endurance simultaneously. Their prices are higher because they restore all three resources in one use.

| Item          | HP    | MP    | Endurance | Price   |
| ------------- | ----- | ----- | --------- | ------- |
| Small Elixir  | 50    | 50    | 50        | 35 TIC  |
| Medium Elixir | 500   | 500   | 500       | 190 TIC |
| Large Elixir  | 1,500 | 1,500 | 1,500     | 450 TIC |

### 1.5 Price Categories

- **Small:** Affordable for routine recovery.
- **Medium:** Moderately priced for substantial recovery.
- **Large:** Expensive, intended for situations requiring significant recovery.

### 1.6 Availability

| Item Tier                          | Proposed Availability                                                |
| ---------------------------------- | -------------------------------------------------------------------- |
| Small Potions, Ethers, and Tonics  | Most villages and towns                                              |
| Medium Potions, Ethers, and Tonics | Established villages and larger towns                                |
| Large Potions, Ethers, and Tonics  | Larger towns, kingdoms, and advanced markets                         |
| Small Elixirs                      | Rare merchants, special rewards, and treasure chests                 |
| Medium Elixirs                     | Rare treasure chests, difficult quests, and selected merchants       |
| Large Elixirs                      | Powerful enemies, major quest rewards, and exceptionally rare chests |

The Conventional Market is the primary shop for recovery items. Its available stock depends on the size and development of the settlement.

### 1.7 General Item Rules

1. Items cannot restore a character beyond their maximum stat.
2. Prices and stock may vary between merchants.
3. Rare items should remain limited to preserve their value.
4. Item drops and prices should be balanced to prevent excessive farming.
5. Recovery values and prices may be adjusted after playtesting.

## 2. Status Remedies (generic names)

Single-status cures tied to `00-gameplay-improvements.md` §4 and resistances in `02-skill-system.md` §§4.3–4.6. One remedy cures one condition; no early panaceas. All prices in TIC.

| Item | Cures | Price | Availability |
| ---- | ----- | ----- | ------------ |
| Antidote | Poison | 25 TIC | Most villages and towns |
| Gold Needle | Petrification | 80 TIC | Larger towns and kingdoms (rare) |
| Dawn Bell | Sleep | 30 TIC | Most villages and towns |
| Clarity Dust | Confusion | 30 TIC | Established villages and larger towns |
| Fever Herb | Fever (illness) | 40 TIC | Established villages and larger towns |
| Shock Seed | Paralysis | 45 TIC | Established villages and larger towns |
| Sonic Beans | Silence | 35 TIC | Established villages and larger towns |
| Holy Cross | Fear | 35 TIC | Established villages and larger towns |
| Reality Petals | Charm | 40 TIC | Rare merchants and treasure chests |
| Holy Water | Ordinary curses | 120 TIC | Advanced markets, quests, and special enemies (rare) |
| Magic Sand | Slow, doom | 180 TIC | Advanced markets, major quests, and rare chests (rare) |
| Cleansing Water | Blindness | 35 TIC | Established villages and larger towns |
| Seraphim Dial | Unconsciousness, Dead (field, restores at 25% HP) | 150 TIC | Rare merchants and treasure chests only |

Full status-to-remedy mapping (including advanced effects) lives in `05-damage-status.md` §4.

Notes:
- Remedies cure but do not grant immunity; resistances remain skill-based (`02` §§4.3–4.6).
- Unconsciousness and Dead revive via Seraphim Dial in the field (25% HP) or fully at the Sacred Sanctuary (`01-village-buildings.md` §3).
- Serious illness, poisoning, or other harmful conditions still require treatment before recovery completes (`00` §4.1).

## 3. Field Supplies (travel and exploration)

Cheap stackable goods for preparation, from `00` §§5–6 and hub roles in `01-village-buildings.md`.

| Item | Effect | Price | Availability |
| ---- | ------ | ----- | ------------ |
| Trail Rations | Small endurance top-up while traveling | 5 TIC | Most villages and towns |
| Lantern Oil | Lights dark dungeon tiles for a short time | 8 TIC | Most villages and towns |
| Rune Chalk | Marks visited tiles/doors on the local map | 6 TIC | Established villages and larger towns |

> Future proposals moved to `06-recommendations-future-work.md`.

## 4. Current Design Status

**Established**

- Twelve recovery items with proposed restoration values and prices.
- Thirteen status remedies with generic names, prices, and availability.
- Three field supplies for travel and exploration.
- Initial availability and item-balancing rules.

> Open items moved to `06-recommendations-future-work.md`.

## Design links

- Currency, format, and series rules: `03-currency-tic.md`.
- Shops that stock these: `01-village-buildings.md` (Conventional Market primary, Blacksmith for gear later).
- Inventory categories and equip preview: `00-gameplay-improvements.md` §5.1.
- Recovery skills that interact with items (Lifesteal, Magic Power Steal caps): `02-skill-system.md` §4.1–4.2.
