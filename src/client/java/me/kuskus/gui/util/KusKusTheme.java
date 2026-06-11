package me.kuskus.gui.util;

import me.kuskus.util.ColorUtil;

public final class KusKusTheme {
    public static final int DEFAULT_PRIMARY = 0xFFFF6600;
    public static final int DEFAULT_PRIMARY_DIM = 0x66FF6600;
    public static final int DEFAULT_LIGHT = 0xFFFF9944;
    public static final int DEFAULT_DARK = 0xFFCC4400;
    public static final int DEFAULT_TEXT_MAIN = 0xFFF5F0E8;
    public static final int DEFAULT_TEXT_GRAY = 0xFF8A8078;
    public static final int DEFAULT_TEXT_DIM = 0xFF4A4440;
    public static final int DEFAULT_BG_DARK = 0xEE0F0D0C;
    public static final int DEFAULT_BG_PANEL = 0xEE1A1614;
    public static final int DEFAULT_BG_BUTTON = 0xEE241E1C;
    public static final int DEFAULT_BG_ACTIVE = 0xEE3D1A00;

    public static int PRIMARY = DEFAULT_PRIMARY;
    public static int PRIMARY_DIM = DEFAULT_PRIMARY_DIM;
    public static int LIGHT = DEFAULT_LIGHT;
    public static int DARK = DEFAULT_DARK;
    public static int TEXT_MAIN = DEFAULT_TEXT_MAIN;
    public static int TEXT_GRAY = DEFAULT_TEXT_GRAY;
    public static int TEXT_DIM = DEFAULT_TEXT_DIM;
    public static int BG_DARK = DEFAULT_BG_DARK;
    public static int BG_PANEL = DEFAULT_BG_PANEL;
    public static int BG_BUTTON = DEFAULT_BG_BUTTON;
    public static int BG_ACTIVE = DEFAULT_BG_ACTIVE;

    private KusKusTheme() {
    }

    public static void reset() {
        PRIMARY = DEFAULT_PRIMARY;
        PRIMARY_DIM = DEFAULT_PRIMARY_DIM;
        LIGHT = DEFAULT_LIGHT;
        DARK = DEFAULT_DARK;
        TEXT_MAIN = DEFAULT_TEXT_MAIN;
        TEXT_GRAY = DEFAULT_TEXT_GRAY;
        TEXT_DIM = DEFAULT_TEXT_DIM;
        BG_DARK = DEFAULT_BG_DARK;
        BG_PANEL = DEFAULT_BG_PANEL;
        BG_BUTTON = DEFAULT_BG_BUTTON;
        BG_ACTIVE = DEFAULT_BG_ACTIVE;
    }

    public static void setAccent(int rgb) {
        PRIMARY = 0xFF000000 | (rgb & 0xFFFFFF);
        PRIMARY_DIM = ColorUtil.withAlpha(PRIMARY, 0x66);
        LIGHT = shift(rgb, 1.22f);
        DARK = shift(rgb, 0.72f);
        BG_ACTIVE = ColorUtil.withAlpha(DARK, 0xEE);
    }

    public static void setTextColor(int rgb) {
        TEXT_MAIN = 0xFF000000 | (rgb & 0xFFFFFF);
        TEXT_GRAY = ColorUtil.blend(TEXT_MAIN, BG_PANEL, 0.45f);
        TEXT_DIM = ColorUtil.blend(TEXT_MAIN, BG_DARK, 0.70f);
    }

    public static void setPanelColor(int rgb) {
        BG_PANEL = ColorUtil.withAlpha(0xFF000000 | (rgb & 0xFFFFFF), 0xEE);
        BG_DARK = ColorUtil.withAlpha(ColorUtil.blend(BG_PANEL, 0xFF000000, 0.45f), 0xEE);
    }

    public static void setButtonColor(int rgb) {
        BG_BUTTON = ColorUtil.withAlpha(0xFF000000 | (rgb & 0xFFFFFF), 0xEE);
    }

    public static int panelHeader() {
        return ColorUtil.withAlpha(DARK, 0xF2);
    }

    public static int panelHeaderHover() {
        return ColorUtil.withAlpha(LIGHT, 0xF2);
    }

    public static int outline() {
        return ColorUtil.withAlpha(PRIMARY, 0x58);
    }

    public static int button(boolean active, boolean hover) {
        if (active) {
            return hover ? ColorUtil.blend(PRIMARY, LIGHT, 0.35f) : BG_ACTIVE;
        }
        return hover ? ColorUtil.withAlpha(LIGHT, 0x44) : BG_BUTTON;
    }

    public static int setting(boolean hover) {
        return hover ? ColorUtil.withAlpha(LIGHT, 0x38) : ColorUtil.withAlpha(BG_BUTTON, 0xCC);
    }

    private static int shift(int rgb, float brightness) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * brightness));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * brightness));
        int b = Math.min(255, Math.round((rgb & 0xFF) * brightness));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
