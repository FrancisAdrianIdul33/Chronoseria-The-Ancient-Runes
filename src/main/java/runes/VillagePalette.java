package runes;

import java.awt.Color;

/**
 * Coneria-style town palette. Single source of truth for scene tiles.
 * No pure black or pure white: outlines use near-black brown,
 * highlights use off-white parchment.
 */
public final class VillagePalette {
    private VillagePalette() {}

    public static final Color GRASS = Color.decode("#5B9E3C");
    public static final Color GRASS_TONE2 = Color.decode("#4F8F34");
    public static final Color GRASS_DARK = Color.decode("#478530");

    public static final Color PATH = Color.decode("#C9A66B");
    public static final Color PATH_EDGE = Color.decode("#A8854F");

    public static final Color PLAZA = Color.decode("#B8B2A7");
    public static final Color PLAZA_EDGE = Color.decode("#8E887E");

    public static final Color WATER = Color.decode("#4FA3E0");
    public static final Color RIM = Color.decode("#8E887E");

    public static final Color WALL = Color.decode("#E8D9B5");
    public static final Color WALL_SHADE = Color.decode("#B9A883");

    public static final Color DOOR = Color.decode("#4A2E1A");
    public static final Color DOOR_FRAME = Color.decode("#2B1A0E");

    public static final Color WINDOW_LIT = Color.decode("#F5D76E");
    public static final Color WINDOW_DAY = Color.decode("#9CC9E8");
    public static final Color WINDOW_FRAME = Color.decode("#3B2A1A");

    public static final Color ROOF_INN = Color.decode("#D64541");
    public static final Color ROOF_INN_DARK = Color.decode("#B23733");
    public static final Color ROOF_WEAPON = Color.decode("#B8362E");
    public static final Color ROOF_WEAPON_DARK = Color.decode("#9C2D27");
    public static final Color ROOF_ARMOR = Color.decode("#7A8089");
    public static final Color ROOF_ARMOR_DARK = Color.decode("#676C74");
    public static final Color ROOF_ITEM = Color.decode("#3F9A4F");
    public static final Color ROOF_ITEM_DARK = Color.decode("#358243");
    public static final Color ROOF_MAGIC = Color.decode("#7B4AB0");
    public static final Color ROOF_MAGIC_DARK = Color.decode("#683E95");
    public static final Color ROOF_HOUSE = Color.decode("#B5651D");
    public static final Color ROOF_HOUSE_DARK = Color.decode("#995518");

    public static final Color TRUNK = Color.decode("#5A3A1E");
    public static final Color CANOPY_LIGHT = Color.decode("#2F7D32");
    public static final Color CANOPY_DARK = Color.decode("#1F5E26");

    public static final Color FENCE = Color.decode("#8B5E34");
    public static final Color SKIN = Color.decode("#F2C9A0");

    public static final Color PLAYER_TUNIC = Color.decode("#C0392B");
    public static final Color OUTLINE_OFFWHITE = Color.decode("#F5F0E0");
    public static final Color OUTLINE_DARK = Color.decode("#2B1A0E");
}
