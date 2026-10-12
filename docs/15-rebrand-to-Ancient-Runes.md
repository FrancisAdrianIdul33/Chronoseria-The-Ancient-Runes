# 15 — Rebrand to Chronoseria: The Ancient Runes + JDK26 Plan

## Rename map (no FF terms)
| Old | New | Notes |
|-----|-----|-------|
| FinalFantasy | RuneOriginApp / Main | entry |
| a (Canvas) | RuneCanvas / GamePanel | main loop |
| g | WorldEngine + BattleEngine | split biggest |
| c | BattleScreen / DungeonHub | TBD after CFR |
| d | MenuState | shop/party menu |
| e | DialogueBox | text scroll |
| h | GameState (interface) + BaseState | keep pattern |
| f | RuneSprite | packed loader kept |
| j | RuneEntity / Battler | |
| k | RuneTile + RuneAnim | split |
| b | GrowthCurve + Stats | |
| i | Party + QuestFlags + Bestiary | split container |
| PACK0..4 | arcs/arc1..arc5 | 5 original arcs |
| a0..a71, bg0... | assets/... | re-author art |

## MIDP -> JDK26 table
| MIDP | JDK26 | Class |
|------|-------|-------|
| MIDlet startApp/pause/destroy | main() + WindowListener | Main |
| Display.setCurrent(Canvas) | JFrame.setContentPane(JPanel) | Main |
| Canvas paint(Graphics) | JPanel paintComponent(Graphics2D) | GamePanel |
| keyPressed/Released(int) | KeyListener / InputMap | InputManager |
| lcdui.Graphics drawImage/Region | Graphics2D drawImage + AffineTransform | Renderer |
| lcdui.Image.createImage(bytes) | ImageIO.read / BufferedImage | AssetLoader |
| RecordStore open/read/write | java.nio.file.Files + JSON | SaveManager |
| media.Player + InputStream[] | javax.sound.sampled.Clip | AudioManager |
| Random (same) | java.util.Random (same) | keep |
| Thread(Runnable) loop | ScheduledExecutor / game Thread + vsync | GameLoop |

## Rules
1. Original folder read-only. Only `javap` reads.
2. New art/text/story — inspired by logic, not copied.
3. Keep systems: 4-party, 255 bestiary, 50 story flags, 86 triggers, step-roll encounters, tier-scale damage, 5 arcs x ~15 maps, 5 langs.

> Recommendations moved to `00-recommendations-future-work.md`.
