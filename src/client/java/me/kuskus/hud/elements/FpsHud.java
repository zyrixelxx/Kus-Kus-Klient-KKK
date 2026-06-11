package me.kuskus.hud.elements;

import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsHud extends HudElement {
    public FpsHud() {
        super("FPS", 8, 40);
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        String text = "FPS " + client.getCurrentFps();
        width = client.textRenderer.getWidth(text);
        RenderUtil.text(context, client.textRenderer, text, x, y, KusKusTheme.TEXT_MAIN);
    }
}
