# 10 — Data — classes b + i

## b — 543 bytes, tiny record
```java
public final class b {
  public byte[] a; public short a; public byte a,b; public int a;
  public short b,c; public byte[] b,c; public byte c,d,e,f,g;
  public short d,e,f; public byte h,i,j,k,l; public byte[][] a;
  public int b,c;
  public b(); public final short a();
}
```
Bytecode:
- `b()` allocates `b=new byte[8]; c=new byte[8]; a=new byte[8][3];`
- `a()` returns scaling: if `a>=6` return `(short)(256<<(a-6))` else `(short)(1<<a)`.

Interpretation: stat-growth record. `a` = level/tier, `a()` = multiplier (1,2,4,8... then 256,512...). `byte[8][3]` = 8 stats x 3 ranks. `b,c` ints = xp/hp.

Copy: `GrowthCurve { int tier; int scale(); }` + `Stats { byte[8][3] }`.

## i — 595 bytes, container
```java
public final class i {
  public b[] a; public short[][] a; public short[] a; public int a,b,c;
  public int[] a; public short a; public byte[] a; public short b,c,d;
  public int[] b,c,d; public short e; public short[] b; public int d;
  public boolean[] a; public boolean[] b;
  public i();
}
```
Bytecode `i()`:
- `a=new b[4]; a=new short[86][2]; a=new short[32]; a,b,c,d=new int[3]; b=new short[36]; d=-1; a=new boolean[50]; b=new boolean[255];` + fills `a[0..3]=new b()`.

Interpretation:
- Party of 4 (`b[4]`).
- 86x2 quest flags / map triggers (`short[86][2]`).
- 32-element palette/status array.
- 3x int[3] = party pos / gold / steps?
- 36 = enemy slots? 50 = story flags, 255 = bestiary/seen flags (`boolean[255]` classic).
- `d=-1` = no selection.

Copy: `Party { Battler[4] }`, `QuestFlags[86][2]`, `StoryFlags[50]`, `Bestiary[255]`.
