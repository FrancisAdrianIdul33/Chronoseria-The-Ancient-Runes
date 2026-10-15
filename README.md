# Chronoseria-The-Ancient-Runes

Chronoseria: The Ancient Runes — original Java RPG (JDK 26, Swing).

- Docs: `docs/` (00-overview through 17-branding)
- Game design: `game-docs/` (gameplay improvement spec)
- Code: `src/main/java/runes/` — Main, GamePanel, GameLoop, BattleEngine, EncounterSystem
- Run: open `src/main/java/runes/Main.java`, Run `Main.main()` (JDK 26)
- Audio: music needs `lib/jlayer-1.0.1.jar` at runtime (MP3 decode; JDK
  `sampled` has no MP3 SPI). Compiling needs nothing extra — JLayer loads by
  reflection and the game runs silent without it. In IntelliJ: File > Project
  Structure > Libraries > add `lib/jlayer-1.0.1.jar` to the run config once.
  Music: `New Game` starts the looped `assets/music/bamboo.mp3`;
  returning to title stops it.
