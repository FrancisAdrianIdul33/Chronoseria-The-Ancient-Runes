# 04 — Engine — class g (87KB, biggest)

`g.class` 87632 bytes. `public final class g extends h`. Main world/battle engine.

## Structure
- Holds `private c a; private d a;` = sub-states embedded.
- 150+ fields: `int a..bi, long a, boolean a..z,A, int[] a..S, String a, String[][] a, byte[] f..o, short[][] g,h,i, k i,j,k,l,m,n, j a,e,f`
- Static: `Image[] a; j a,e,f; int[] T; int a,bg; byte[] h,i,j,l,n; short[] a,b;`

## Method groups (from javap, ~120)
- Lifecycle: `a(), b(), d(), c(), a(Graphics), a():int, b():void`
- Render splits: `k(Graphics), l(Graphics), m(Graphics), n(Graphics), o(Graphics), p(Graphics), q(Graphics), r(Graphics), s(Graphics), t(Graphics), u(Graphics), v(Graphics), w(Graphics), x(Graphics), y(Graphics), z(Graphics), A(Graphics)`
- Logic ticks: `G(), I(), J(), N(), O(), P(), Q(), R(), S(), T(), U(), V(), W(), X(), Y(), Z(), aa()..ag()`
- Queries: `q(),r(),s(),t(),u(),v(),w(),x(),y(),z(),A(),B(),C(),D(),E(),F(),G(),H(),I(), p(), d(int):boolean, e(int):boolean, f(int):boolean, f(int,int):boolean`
- Data: `a():byte[][], a():int[], b(byte[]), c(byte[]), a(int,int,int), m(int)`

## Interpretation
- Multi-phase renderer = map layer + entities + effects + UI overlay, one method per layer.
- Boolean queries = turn/encounter/story flags (e.g., `q()=canEncounter?`, `D()=isBossDead?`).
- `c a + d a` = battle uses field state + menu state without reload.
- Static `Image[]` = tileset cache. `j` refs = party + enemies.

> Recommendations moved to `00-recommendations-future-work.md`.
