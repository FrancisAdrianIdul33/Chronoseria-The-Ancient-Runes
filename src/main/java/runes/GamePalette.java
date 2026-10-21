package runes;

import java.awt.Color;

/** Ancient Fantasy palette: 60-30-10 + feedback colors. Single source of truth. */
public final class GamePalette {
    private GamePalette() {}

    public static final Color PARCHMENT = Color.decode("#E8D8B0");
    public static final Color PARCHMENT_DARK = Color.decode("#C9B88A");
    public static final Color ROYAL_BLUE = Color.decode("#253653");
    public static final Color ROYAL_DEEP = Color.decode("#1A2538");
    public static final Color LEGENDARY_GOLD = Color.decode("#C69A3A");

    public static final Color CRIMSON_RED = Color.decode("#8F3434");
    public static final Color FOREST_GREEN = Color.decode("#477A52");
    public static final Color ARCANE_BLUE = Color.decode("#638EB8");
    public static final Color MYSTIC_VIOLET = Color.decode("#A66AC2");

    // Inn interior colors
    public static final Color BLUE_FLOOR = Color.decode("#3A7BD5");
    public static final Color GRAY_WALL = Color.decode("#808080");
    public static final Color WHITE_TOP = Color.decode("#FFFFFF");
    public static final Color DARK_GRAY = Color.decode("#404040");
    public static final Color RED_BED = Color.decode("#CC0000");
}
