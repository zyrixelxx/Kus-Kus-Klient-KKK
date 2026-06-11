package me.kuskus.hud.elements;

import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class PingHud extends HudElement {
    public PingHud() {
        super("Ping", 8, 56);
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        int ping = 0;
        if (client.player != null && client.getNetworkHandler() != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        String text = "Ping " + ping + "ms";
        width = client.textRenderer.getWidth(text);
        RenderUtil.text(context, client.textRenderer, text, x, y, KusKusTheme.TEXT_MAIN);
    }
}
