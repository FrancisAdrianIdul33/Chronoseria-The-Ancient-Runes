# 01 — Entry Lifecycle — FinalFantasy (MIDlet)

Source class: `FinalFantasy.class` 886 bytes, `major 45`, `extends javax.microedition.midlet.MIDlet`.

## javap -p
```
public final class FinalFantasy extends javax.microedition.midlet.MIDlet {
  private static a a;
  private static javax.microedition.lcdui.Display a;
  public FinalFantasy();
  public final void startApp();
  public final void pauseApp();
  public final void destroyApp(boolean);
}
```

## javap -c startApp logic (paraphrased)
- if (a==null): Display d=getDisplay(this); a=new a(this,d); d.setCurrent(a); new Thread(a).start();
- else: d=getDisplay(this); d.setCurrent(a);
- Constructor only calls super(). No game init there — all in `a` constructor + thread.

## pauseApp / destroyApp
- pauseApp: if (a.a>0 && a!=null) a.h(); // a.h = suspend/pause hook, [a.a] is short state flag
- destroyApp: if (a!=null) a.i(); // a.i = teardown/save hook

> Recommendations moved to `00-recommendations-future-work.md`.
