# 05 — State — class c (64KB)

`c.class` 64838 bytes. `public final class c extends h`.

## Fields (excerpt)
`static k i,j,k; static int x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T; static int[] p,q,r,s,t,u,v,w; static int[][] b,c,d,e,f; static byte[][] b,c,d,e,f,g,h; static byte[] f,g,h,i,j; static short[] a; static short[][] g; static Graphics a; static j a,e,f,g,h,i;`

## Methods (~70)
- Lifecycle: `c(), b(), a(), a():int, b():boolean, a(int,int), F(),G(),H(),I(),J(),K(),L(),M(),N(),O(),P()`
- Render: `a(Graphics)`, `a(Graphics,int,int,int,int,boolean)`, `a(int,int,int,int,boolean)`, `f(int,int)`, `a(int,int,int,int)`
- Helpers: `a(k):int`, `h(int),i(int),j(int),k(int),l(int),m(int),n(int),o(int),p(int)`, `a(int):void`, `b(boolean)`, `c(boolean,boolean)`

## Interpretation
- Second-largest state = likely battle screen OR major town/dungeon hub (holds many `j` entities + `k` anims).
- `k i,j,k` statics = shared anim templates (player, enemy, effect).
- `int[][]/byte[][]` tables = damage formulas, AI scripts, dialogue indices.
- Overloaded `a(int...)` = state transitions: `a(stage,room)`, `a(stage,room,flag)`.

## Copy notes
- Candidate for `BattleScreen` or `DungeonHub` in Ancient Runes.
- Extract tables to `data/battle_tables.json` equivalent, not hard-coded arrays.
- Keep `a(Graphics)` split: background, sprites, floating numbers, menu.
