# 00 — Chronoseria: The Ancient Runes — Overview & Rebrand Rules

> Source: `C:/Users/Francis Adrian Idul/Documents/Final Fantasy` (original J2ME game, local only, untouched)
> Docs home: `C:/Users/Francis Adrian Idul/Documents/Ancient Runes/` (this folder)
> Goal: copy ONLY codes + inside logic (battle, encounters, story arcs) into 10–20 markdowns, then rebuild as ORIGINAL, not FF2/3.
> No auto-build of a Java game yet. Docs first.

## 1. What the original is

- J2ME / Java ME MIDP-2.0 + CLDC-1.0 MIDlet.
- `MANIFEST.MF`:
  ```
  Manifest-Version: 1.0
  MIDlet-Name: Final Fantasy
  MIDlet-Version: 1.0.0
  MIDlet-Vendor: Electronic Arts
  MIDlet-1: Final Fantasy,/i.png,FinalFantasy
  MicroEdition-Profile: MIDP-2.0
  MicroEdition-Configuration: CLDC-1.0
  MIDlet-Icon: /i.png
  MIDlet-Data-Size: 5000
  Created-By: Metaflow
  ```
- Bytecode `major version: 45` = Java 1.1 era. That is why JDK 21/26 cannot run it directly.
- 12 classes, 284,205 bytes total:
  ```
  g.class 87632 | c.class 64838 | d.class 33364 | a.class 29839
  h.class 26302 | e.class 18249 | k.class 9529 | j.class 6715
  f.class 5713 | FinalFantasy.class 886 | i.class 595 | b.class 543
  ```
- No `.java` source. Only `.class` + data files. All game classes except `FinalFantasy,b,i` are obfuscated (`a,c,d,e,f,g,h,j,k`).
- Toolchain observed: `javac 21.0.12.1` available, but original needs Java 8 + emulator to run as-is. New build will target JDK 26 with clean code.

## 2. High-level architecture (from signatures + bytecode)

```
FinalFantasy (MIDlet)
  startApp():
    if (a == null) {
      Display d = Display.getDisplay(this);
      a = new a(this, d);
      d.setCurrent(a);
      new Thread(a).start();
    } else {
      Display.getDisplay(this).setCurrent(a);
    }
  pauseApp(): if (a.a > 0 && a != null) a.h();
  destroyApp(b): if (a != null) a.i();

a extends Canvas implements Runnable
  - main screen + game loop + input + save + audio refs
  - paint(Graphics), run(), keyPressed(int), keyReleased(int), hideNotify()
  - static globals: Random, Display, Image, Graphics, Player[], RecordStore helpers

h (abstract base)
  - shared tables, RNG, entity lists, draw helpers
  - abstract int a(); abstract void a(Graphics);
  - 100+ static helpers for draw, text, save, math

c,d,e,g extends h
  - game states / screens. Each has a()/b()/d() lifecycle + a(Graphics) render.
  - g is biggest (87KB, 150+ fields, 100+ methods) -> main world/battle engine
  - c second (64KB) -> second major state
  - d,e smaller states/menus

f = sprite/image wrapper (Image[], byte[][], int[][] frames)
j = entity/state machine (pos, flags, anim, k ref)
k = tile/anim unit (short[]/byte[] frames, draw with Graphics)
b = tiny data record (byte[] b[8], c[8], byte[8][3], short a())
i = container (b[4], short[86][2], short[32], int[3]x4, short[36], bool[50], bool[255])
```

## 3. Folders / assets in original

- `PACK0_0..PACK4_14` = 75 files, 15 per PACK. 5 groups = 5 chapters/arcs. See `13-story-arcs-PACK0-4.md`.
- `a0..a71` (72 blobs) + `bg0 119041`, `mg0 348985`, `ms0 171351`, `m0 87524`, `c0 76932`, `ch0 88321`, `ts0 81020`, `ss0 45276`, `w0 46846`, `cp0 18114`, `np0 44450`, `fn0 8383`.
- `I 56b` = language table: `ENGLISH, FRANCAIS, ITALIANO, DEUTSCH, ESPANOL` + header bytes.
- `c.p 5b` = zeros (save placeholder / padding).
- `i.png 446b` = MIDlet icon.
- See `14-maps-assets.md` for full table.

## 4. Rebrand rules — Ancient Runes (must follow)

1. No `Final Fantasy`, no `Square`, no `Chocobo/Moogle/Crystal` names, no EA assets verbatim.
2. Copy LOGIC, not text/graphics. Rename everything: `FinalFantasy->RuneOrigin`, `a->RuneCanvas`, etc. Full map in `15-rebrand-to-Ancient-Runes.md`.
3. Clean-room: describe behavior in own words. Do not paste decompiled method bodies verbatim beyond short `javap` signatures for reference.
4. Original folder NEVER modified. Only read via `javap`. All new writing goes here.
5. Future JDK 26 build will be Gradle + `JFrame/JPanel/Graphics2D`, `javax.sound.sampled`, `java.nio.file` saves — no `javax.microedition.*`.
6. Personal study only. Do not redistribute original PACKs or EA IP.

## 5. Doc map in this folder

```
00-overview.md (this file)
01-entry-lifecycle-FinalFantasy.md
02-class-catalog.md
03-main-canvas-a.md
04-engine-g.md
05-state-c.md
06-base-h-and-states-d-e.md
07-sprite-f.md
08-entity-j.md
09-tile-anim-k.md
10-data-b-i.md
11-battle-system.md
12-encounters-spawns.md
13-story-arcs-PACK0-4.md
14-maps-assets.md
15-rebrand-to-Ancient-Runes.md
```

## 6. Next step

Read 01→10 for raw evidence, then 11→15 for synthesized systems + rebrand plan. No code scaffold until docs approved.
