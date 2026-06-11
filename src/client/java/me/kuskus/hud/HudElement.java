package me.kuskus.hud;

import me.kuskus.gui.util.RenderUtil;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.ColorSetting;
import me.kuskus.setting.Setting;
import me.kuskus.setting.SettingGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class HudElement {
    private static final int GRID = 6;
    private final String name;
    private final int defaultX;
    private final int defaultY;
    protected int x;
    protected int y;
    protected int width = 80;
    protected int height = 12;
    private boolean visible = true;
    private final List<SettingGroup> groups = new ArrayList<>();
    protected final BoolSetting background;
    protected final ColorSetting backgroundColor;
    protected final ColorSetting textColor;

    protected HudElement(String name, int x, int y) {
        this.name = name;
        this.defaultX = x;
        this.defaultY = y;
        this.x = x;
        this.y = y;
        SettingGroup display = group("Display");
        background = display.add(new BoolSetting("Background", "Draw a subtle HUD background.", false));
        backgroundColor = display.add(new ColorSetting("Background Color", "Background box color.", 0x0D0B0A));
        textColor = display.add(new ColorSetting("Text Color", "Main text color.", 0xFFF5EC));
    }

    public String name() {
        return name;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void moveTo(int x, int y) {
        this.x = Math.max(0, snap(x));
        this.y = Math.max(0, snap(y));
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    protected SettingGroup group(String name) {
        SettingGroup group = new SettingGroup(name);
        groups.add(group);
        return group;
    }

    public List<SettingGroup> groups() {
        return Collections.unmodifiableList(groups);
    }

    public void reset() {
        moveTo(defaultX, defaultY);
        visible = true;
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group.settings()) {
                setting.reset();
            }
        }
    }

    protected int textColor() {
        return 0xFF000000 | (textColor.get() & 0xFFFFFF);
    }

    protected void renderBackground(DrawContext context) {
        if (background.get()) {
            RenderUtil.rect(context, x - 3, y - 3, width + 6, height + 6, 0x99000000 | (backgroundColor.get() & 0xFFFFFF));
        }
    }

    public final void renderIfVisible(DrawContext context, MinecraftClient client) {
        if (visible) {
            render(context, client);
        }
    }

    public abstract void render(DrawContext context, MinecraftClient client);

    private int snap(int value) {
        return Math.round(value / (float) GRID) * GRID;
    }
}
