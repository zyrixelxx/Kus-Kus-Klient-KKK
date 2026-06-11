package me.kuskus.hud.elements;

import me.kuskus.KusKusKlient;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class WatermarkHud extends HudElement {
    public WatermarkHud() {
        super("Watermark", 10000, 6);
        background.set(true);
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        String text = "Kus Kus Klient " + KusKusKlient.VERSION;
        width = client.textRenderer.getWidth(text);
        height = 10;
        int targetX = client.getWindow().getScaledWidth() - width - 6;
        if (x > targetX) {
            moveTo(targetX, y);
        }
        renderBackground(context);
        RenderUtil.text(context, client.textRenderer, text, x, y, textColor());
    }
}
