package me.kuskus.gui;

import me.kuskus.gui.util.KusKusTheme;

public final class ThemesPanel {
    public static final int[] PRESETS = {
        0xFF6600,
        0x2196F3,
        0x9900FF,
        0x00CC44,
        0xCC2200
    };

    private ThemesPanel() {
    }

    public static void apply(int index) {
        if (index >= 0 && index < PRESETS.length) {
            KusKusTheme.setAccent(PRESETS[index]);
        }
    }
}
