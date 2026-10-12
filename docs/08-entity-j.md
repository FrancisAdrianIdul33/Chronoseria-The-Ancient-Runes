# 08 — Entity — class j

`j.class` 6715 bytes. `final class j` — entity / battler / NPC state machine.

## Fields
`int i..u, a..h; boolean a..j; byte[][] a,b,c; int[] a,b,c; int p..w; k a; long a;`

Key: `boolean a..j` (10 flags: alive, active, visible, poison?, etc.), `byte[][] a,b,c` (AI script / anim script), `k a` (anim driver), `int a..h` (x,y,hp,mp,atk,def,spd,state).

## Methods
`j(); a(int,int,int,int); b(int,int,int,int); a(int,int,int,int,boolean,byte[][]); a(int); a(); a(boolean); b(); b(boolean); c(); a(Graphics); a():int; a(int[]); b():int; c():int; d():int; b(int); d();`

Interpretation:
- `a(x,y,hp,mp)` spawn/init. `b(...)` reposition. 6-arg `a` = spawn with script.
- `a(boolean)/b(boolean)` = set active/visible. `b()/c()/d()` = update/physics/anim tick.
- `a(Graphics)` draws self via `k`.
- `a():int/b():int/c():int/d():int` = getState/getHP/getMP/getStatus.

> Recommendations moved to `00-recommendations-future-work.md`.
