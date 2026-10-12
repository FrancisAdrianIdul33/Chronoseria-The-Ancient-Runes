# 09 — Tile/Anim — class k

`k.class` 9529 bytes. `final class k` — tile + frame animation unit.

## Fields
`short a,b,c,d; short[] a..n; byte[] a..k; int a,b,c; byte a; boolean a,b; f a; int d..l;`

Pattern: many `short[]` (frame indices/durations) + `byte[]` (tile ids/flags) + `f a` (sprite ref).

## Methods (25)
`k(); a(int,int,int,int,int); a(); b(); a(int,int,int); a(int):int; a(Graphics,int,int,int,int); a(Graphics,int,int,int,int,boolean); b(int):int; ... g(int):int; a(int,int):int; b(int,int):int; a(int,int,int,int); a(int); c(); b(int); d(); a(int,int):boolean; a(Graphics,int,int,int); a(Graphics,int,int,int,int,int,int,int); a(Graphics,int,int,int,boolean); a(int,int); b(Graphics,...);`

Interpretation:
- `a(w,h,frames,speed,loop)` init. `a(n):int` = get frame n. `b..g(n)` = get tile property (solid? encounter?).
- `a(Graphics,...)` family = draw tile / draw anim frame with flip (`boolean` = mirror).
- `a(x,y):boolean` = collision query.
- `f a` = shared sprite sheet.

> Recommendations moved to `00-recommendations-future-work.md`.
