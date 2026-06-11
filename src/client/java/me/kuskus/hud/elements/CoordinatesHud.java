package me.kuskus.hud.elements;

import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudElement;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.SettingGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

public class CoordinatesHud extends HudElement {
    private final BoolSetting showNether;

    public CoordinatesHud() {
        super("Coordinates", 8, 24);
        SettingGroup coordinates = group("Coordinates");
        showNether = coordinates.add(new BoolSetting("Nether Coords", "Show linked Nether/Overworld coordinates.", true));
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        if (client.player == null) return;
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        String text = String.format("XYZ %.1f %.1f %.1f", playerX, playerY, playerZ);
        if (showNether.get() && client.world != null) {
            boolean nether = client.world.getRegistryKey() == World.NETHER;
            double linkedX = nether ? playerX * 8.0 : playerX / 8.0;
            double linkedZ = nether ? playerZ * 8.0 : playerZ / 8.0;
            text += String.format(" | %s %.1f %.1f", nether ? "Overworld" : "Nether", linkedX, linkedZ);
        }
        width = client.textRenderer.getWidth(text);
        height = 10;
        renderBackground(context);
        RenderUtil.text(context, client.textRenderer, text, x, y, textColor());
    }
}
