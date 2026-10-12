# 06 — Base h + States d,e

## h — abstract base, 26302 bytes
`public abstract class h` — all game screens inherit.

Key members:
- Tables: `int[] b..x, byte[] a..g, short[][] a..d, short[][][] a, char[][] a,b, String[] a`
- Entities: `k[] a,b, k a..h, j a..d, i[] a,b, i a`
- State: `int c..w, boolean c..i, long a..c, Random a`
- Abstract: `abstract int a(); abstract void a(Graphics);`
- Concrete: `void a(); void b(); void d(); void e(); void f();`
- Static helpers (~80): draw rect/text/image, `a(Graphics,int,int,int,int)`, save `a(ByteArrayInputStream):int`, math `b(int):int`, `a(int,int,int):int`, flags `a(int):boolean`

Interpretation: h = engine toolkit. Subclasses only implement `a()` (tick id) + `a(Graphics)` (draw).

## d — 33364 bytes, extends h
Fields: `int b,x..M, int[] a,p, short[][] g, byte[] f, j a,e..n, String[] a,b, boolean a`
Methods: `c(), a(), d(), a():int, b(), a(Graphics)` + 30 private render splits `k(Graphics)..o(Graphics)`, `a(Graphics,int)`, `b(Graphics,int,int)`.
Guess: menu-heavy state (many Strings, many j slots = party list / shop).

## e — 18249 bytes, extends h
Fields: `k i, int a,b,x..I, short[][] g,h, short[] a, byte[][] b, byte[] f, char[] a, char a,b, j e, String[] a, boolean a,b,i, int[] p,a`
Methods: `a(), a():int, b(), a(Graphics)` + `f(), B()..G()`.
Guess: dialogue/text state (char[], char cursors, String[] lines).

> Recommendations moved to `00-recommendations-future-work.md`.
