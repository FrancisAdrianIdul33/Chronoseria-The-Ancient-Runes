# 03 — Main Canvas — class a

`a.class` 29839 bytes. `public final class a extends Canvas implements Runnable`.

## Why it matters
Holds globals, loop, input, save, audio. All other states accessed via static `a.*`.

## Fields (javap -p excerpt)
- `static Random a; public static int b,c; public static short a;`
- `static Display a; static FinalFantasy a; static a a; public static h a; static k a; static j a,b;`
- `static Image a; static Graphics a;`
- `static Player[] a; static InputStream[] a;`
- `static byte[] a..i; static short[] a,b,c; static short[][] a; static byte[][] a..e;`
- `static int a..z, long a..h, boolean a..p`
- Instance: `protected a(); public a(FinalFantasy,Display)`

## Methods (130+)
- Loop: `run()`, `paint(Graphics)`, `hideNotify()`, `keyPressed(int)`, `keyReleased(int)`
- Lifecycle: `h(), i(), b(), c(), d(), e(), f(), g(), e(int), f(int), g(int), h(int)`
- Draw: `a(Graphics,byte[],int,int,int,int,int)`, `a(Graphics,int,int,int)`, etc. 10 overloads
- Data: `a(String):DataInputStream`, `a(DataInputStream):String/byte[]`, `a(byte[],int):short[]`, `a(byte[]):int`, `c(byte[],int):int`
- Save/RMS: `a(String):RecordStore`, `a(int):ByteArrayInputStream`, `a(int,ByteArrayOutputStream):boolean`
- Audio/state: `b(int):boolean`, `a(long):boolean`, `b(long):boolean`

## Bytecode-verified behavior
- `run()` = game thread. `paint()` = full-screen redraw, delegates to current `h` state via `h.a(Graphics)`.
- `keyPressed/Released` map phone keys (-1..-4, 0..9, * #) to game flags in `h`.
- `a(String)` opens resource `DataInputStream` for PACK/a* files.
- `Player[]` + `InputStream[]` = background music + SFX slots.

> Recommendations moved to `00-recommendations-future-work.md`.
