# 12 — Encounters & Spawns (how enemies appear on area)

## Evidence
- `a.a = Random` global. `a.b,c` ints = step counters? `a.a:short` = zone id.
- `k.a(x,y):boolean` = collision/query. `k.b..g(n):int` tile props include encounter flag.
- `g.e(x,y):boolean`, `g.f(x,y):boolean`, `g.d(int):boolean` = encounter checks.
- `i.d` int + `i.a boolean[50]` story flags gate spawns.
- `PACK0_1..PACK4_8` mid-size blobs (2-12KB) = encounter tables per map.
- `a0..a71` + `m0,ms0` = map + monster sprites.

## Reconstructed loop
```
onStep(x,y):
  tile = k.a(x,y)
  if !tile.walkable: block
  if tile.encounter && storyFlagAllows(zone):
    steps++
    rate = table[zone][PACK_*(x)]  // from PACK blobs via a.a(String)->DataInputStream
    if Random.nextInt(256) < rate:
      party = i.a (b[4])
      enemies = spawn(table.pick(), count=1..3, scale=b.a())
      g.switchState(BATTLE)
      mark bestiary[i.b[255]] seen
```

## Spawn tables
- 5 PACK groups = 5 regions, each 15 maps. Encounter blob likely `PACKn_5, _8` (8-10KB range).
- `short[86][2]` in i = 86 triggers x (map,event) = one-time spawns/bosses.
- `Player[]` in a = battle swirl SFX.

## Copy notes
- Ancient Runes `EncounterSystem { float rateFor(Biome,Steps); List<Enemy> roll(Zone) }`.
- Keep step-counter + 0-255 roll (classic feel), but expose JSON tables.
- Bosses = `short[86][2]` one-shots, not random.
