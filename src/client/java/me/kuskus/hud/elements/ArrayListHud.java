package me.kuskus.hud.elements;

import me.kuskus.KusKusKlient;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudElement;
import me.kuskus.module.Module;
import me.kuskus.util.KeyUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class ArrayListHud extends HudElement {
    public ArrayListHud() {
        super("ArrayList", 8, 8);
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        int yOffset = 0;
        int maxWidth = 80;
        for (Module module : KusKusKlient.MODULES.enabled().stream().sorted(Comparator.comparingInt((Module m) -> client.textRenderer.getWidth(m.name())).reversed()).toList()) {
            String text = module.name();
            if (module.keybind().get() != GLFW.GLFW_KEY_UNKNOWN) {
                text += " [" + KeyUtil.name(module.keybind().get()) + "]";
            }
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(text));
            RenderUtil.text(context, client.textRenderer, text, x, y + yOffset, textColor());
            yOffset += 11;
        }
        width = maxWidth;
        height = Math.max(12, yOffset);
    }
}
