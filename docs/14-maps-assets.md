# 14 — Maps & Assets Catalog

## Big blobs (sorted)
| File | Bytes | Guess |
|------|-------|-------|
| mg0 | 348985 | magic/effect graphics pack |
| ms0 | 171351 | monster sprites |
| bg0 | 119041 | backgrounds-tiles |
| a39 | 115516 | chapter splash / large map |
| ch0 | 88321 | character portraits |
| m0 | 87524 | main map data |
| ts0 | 81020 | tileset |
| c0 | 76932 | characters / classes |
| a68 | 54228 | boss sprite |
| a62 | 51418 | dungeon map |
| w0 | 46846 | world map |
| a49 | 45786 | town map |
| ss0 | 45276 | sound / song data? |
| np0 | 44450 | NPC data |
| a57 | 43802 | field map |

## a0..a71 (72 files)
Small 400-40KB each. Pattern: `a19 441` tiny (icon?), `a22-29 ~1KB` (tiles), `a0,a20-27` 1-3KB (UI), `a30-37` 2-8KB (NPC), `a40-50` 4-45KB (maps), `a51-69` 7-54KB (dungeons/bosses). `a39` outlier 115KB = title/end screen.

## System files
- `I 56b` = langs: header `05 04 00 00 07 ENGLISH 01 00 09 FRANCAIS 02 00 08 ITALIANO 03 00 07 DEUTSCH 04 00 08 ESPANOL` = 5 langs, id-prefixed.
- `c.p 5b` = 5 zero bytes (save slot placeholder).
- `i.png 446b` = MIDlet icon.
- `fn0 8383` = font bitmap. `cp0 18114` = codepage/text. `ss0/ts0` = sound/tiles.

> Recommendations moved to `00-recommendations-future-work.md`.
