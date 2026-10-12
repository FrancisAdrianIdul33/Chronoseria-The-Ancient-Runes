# Chronoseria: Items and Supplies

Game design document · Recovery inventory, availability, and future categories. All prices in TIC (see `03-currency-tic.md`).

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

## 2. Future Item Proposals

The following categories are reserved for future expansion. Their individual items and prices have not yet been finalized.

| Category                        | Possible Purpose                                                      |
| ------------------------------- | --------------------------------------------------------------------- |
| Status Recovery                 | Removes poison, petrification, sleep, or confusion.                   |
| Combat Enhancements             | Temporarily increases attack power, magic power, or other attributes. |
| Defensive Items                 | Temporarily improves defense or resistance to status effects.         |
| Travel and Exploration Supplies | Helps players navigate, explore, or prepare for dangerous areas.      |
| Rare Quest Items                | Used to complete special quests or unlock story events.               |
| Crafting Materials              | Used for equipment upgrades, item creation, or other crafting systems.|

## 3. Current Design Status

**Established**

- Twelve recovery items with proposed restoration values and prices.
- Initial availability and item-balancing rules.

**Open for future development**

- Additional item types and their prices.
- Status recovery and combat enhancement items.
- Equipment prices and upgrade costs.
- Selling prices and rewards from enemies or quests.
- Any adjustments required after gameplay testing.

## Design links

- Currency, format, and series rules: `03-currency-tic.md`.
- Shops that stock these: `01-village-buildings.md` (Conventional Market primary, Blacksmith for gear later).
- Inventory categories and equip preview: `00-gameplay-improvements.md` §5.1.
- Recovery skills that interact with items (Lifesteal, Magic Power Steal caps): `02-skill-system.md` §4.1–4.2.
