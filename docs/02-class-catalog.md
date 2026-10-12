# 02 — Class Catalog — All 12 Classes

Source folder: `Final Fantasy/` — 12 classes, 284205 bytes. No .java source.

| Class | Size | Super | Role guess | Key evidence |
|-------|------|-------|------------|--------------|
| FinalFantasy | 886 | MIDlet | entry | startApp creates `a`, sets Display, starts Thread |
| a | 29839 | Canvas+Runnable | main screen/loop/input/save/audio | paint(Graphics), run(), keyPressed/Released, Player[], RecordStore, Random, Image |
| b | 543 | Object | tiny data record | byte[8] b,c, byte[8][3] a, short a() scaling |
| c | 64838 | h | major state #2 | extends h, 100+ methods, Graphics render |
| d | 33364 | h | state/menu | extends h, a()/b()/d() + a(Graphics) |
| e | 18249 | h | state/menu | extends h, similar lifecycle |
| f | 5713 | Object | sprite/image decoder | Image[], byte[][], int[][], a(Graphics,x,y) |
| g | 87632 | h | main engine/world/battle | biggest, 150+ fields, 100+ methods, holds c,d refs |
| h | 26302 | Object abstract | shared base/tables/draw/save | abstract a(), a(Graphics), 100+ static helpers |
| i | 595 | Object | container | b[4], short[86][2], short[32], int[3]x4, bool[50], bool[255] |
| j | 6715 | Object | entity/state machine | pos/flags/anim, k ref, a(Graphics) |
| k | 9529 | Object | tile/anim unit | short[]/byte[] frames, draw(Graphics) |

Sizes sorted: g>c>d>a>h>e>k>j>f>FinalFantasy>i>b.

Obfuscation: a,c,d,e,f,g,h,j,k single-letter = ProGuard/Metaflow. b,i also single but data holders.

Lifecycle pattern in c/d/e/g (from javap):
`public final void c(); public final void a(); public final int a(); public final void b(); public final void d(); public final void a(Graphics);`
h defines `abstract int a(); abstract void a(Graphics); void b(); void d();`

Copy for Ancient Runes: keep same state split, rename to meaningful names.
