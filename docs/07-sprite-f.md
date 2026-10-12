# 07 — Sprite — class f

`f.class` 5713 bytes. `final class f` — image/sprite decoder + animator.

## Fields
`boolean a; boolean b,c; Image[] a; int a,b,c,f,g,h,i,j,k,l,d,e; byte[][] a; byte[] a; byte a; int[][] a;`

## Methods
`f(); a(int,int,int); a(byte[],int,boolean); a(boolean); d(int); a(int,int,int[]):int[]; b(int,int,int); a(int,int,int,int):int; a(int,int,int,int,boolean); a(int); a(byte[],int); a(); b(int); a(Graphics,int,int); a(Graphics,int,int,int,int,int,int,int); c(int); b();`

## Bytecode-verified (from javap -c excerpt)
- `f()` sets `g=-1` (invalid frame = unloaded).
- `a(int,int,int)` sets `b=true`, allocates `int[a][f]` frame buffer, optional `byte[size]` mask.
- `a(byte[],int,boolean)` reads `byte[1]&0xFF` as frame count `f` (256 if 0), allocates `int[a][f]`, then loops pixels combining two bytes `((b1&0xFF)<<8|(b2&0xFF))` into frame ints — custom packed sprite format from PACK/a* files.
- `a(Graphics,x,y)` blits current frame. 8-arg version draws sub-rect (clipping/scroll).

> Recommendations moved to `00-recommendations-future-work.md`.
