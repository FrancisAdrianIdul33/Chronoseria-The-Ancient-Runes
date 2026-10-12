# 11 — Battle System (synthesized from g,c,d,e,h,j,k,f,b,i,a)

> No single Battle class — battle is split across g (engine) + c (screen) + j (battlers) + k (anims) + f (sprites) + b/i (stats) + h (helpers). This doc reconstructs the flow.

## 1. Party model (from i + b + j)
- Party = `i.a = b[4]` = 4 slots. Each `b` holds `byte[8][3]` stats + `short a` tier + `a():short` scale `1<<a` or `256<<(a-6)`.
- Runtime battler = `j`: `int a..h` = x,y,hp,mp,atk,def,spd,state; `boolean a..j` = alive/active/visible/status; `k a` anim; `byte[][]` AI script.
- Enemy slots: `i.b = short[36]` (36 = max enemies per zone?), `i.b = boolean[255]` bestiary seen.

## 2. Turn flow (from g + c method names)
```
g.B() / g.C() = canAct? / isTurnEnd?
g.q()..z() = per-battler queries (alive? canMove? isStunned?)
c.b() = battle tick, c.a(Graphics) = battle render
d.a(Graphics,boolean) / e.a(Graphics) = damage numbers / status overlay
h.a(Graphics,String,x,y) = menu text, h.b(j) = focus battler
j.b()/c()/d() = update/physics/anim per frame
k.a(Graphics...) = play attack anim (boolean=mirror for enemy side)
f.a(Graphics,x,y) = blit sprite frame
```

## 3. Damage formula (from h + b)
- `h.a(int,int,int):int`, `b(int):int`, `a(int,int,byte):int` = base + variance + element.
- `b.a()` scale multiplies base: `dmg = (atk * scale(tier) + rand) - def`.
- `h.c(int):int`, `d(int):int`, `e(int):int` = clamp/resist/crit (names inferred, bodies need CFR decompile).
- `j.a(int[])` = apply damage array [hp,mp,status].

## 4. Targeting / menus (from d + e)
- `d` holds `String[] a,b` = command names (Attack/Magic/Item/Run) + `j a,e..n` = targets.
- `e` holds `char[]`, `char a,b` cursors + `String[] a` = scrolling message box (“X casts...”).
- Flow: select command (d) -> select target (d.a(j...)) -> play anim (k+f) -> apply (j) -> message (e).

> Recommendations moved to `00-recommendations-future-work.md`.
