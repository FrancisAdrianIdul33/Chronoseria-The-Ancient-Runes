# Chronoseria-The-Ancient-Runes

Chronoseria: The Ancient Runes — original Java RPG (JDK 26, Swing).

- Docs: `docs/` (00-overview through 17-branding)
- Game design: `game-docs/` (gameplay improvement spec)
- Code: `src/main/java/runes/` — Main, GamePanel, GameLoop, BattleEngine, EncounterSystem
- Run: open `src/main/java/runes/Main.java`, Run `Main.main()` (JDK 26)
- Audio: stock Java only, no libraries — the theme ships as WAV
  (`assets/music/bamboo.wav`, converted once from the MP3 upload) because
  plain Java can't decode MP3. Music: `New Game` starts the looped bamboo
  theme; returning to title stops it.
