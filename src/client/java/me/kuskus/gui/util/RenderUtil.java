package me.kuskus.gui.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class RenderUtil {
    private RenderUtil() {
    }

    public static void rect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    public static void outline(DrawContext context, int x, int y, int width, int height, int color) {
        rect(context, x, y, width, 1, color);
        rect(context, x, y + height - 1, width, 1, color);
        rect(context, x, y, 1, height, color);
        rect(context, x + width - 1, y, 1, height, color);
    }

    public static void softShadow(DrawContext context, int x, int y, int width, int height, int color) {
        rect(context, x + 1, y + 1, width, height, color);
        rect(context, x + 2, y + 2, width, height, color & 0x66FFFFFF);
    }

    public static void lineH(DrawContext context, int x, int y, int width, int color) {
        rect(context, x, y, width, 1, color);
    }

    public static void lineV(DrawContext context, int x, int y, int height, int color) {
        rect(context, x, y, 1, height, color);
    }

    public static void text(DrawContext context, TextRenderer renderer, String text, int x, int y, int color) {
        context.drawText(renderer, text, x, y, color, false);
    }

    public static void centeredText(DrawContext context, TextRenderer renderer, String text, int x, int y, int width, int color) {
        text(context, renderer, text, x + (width - renderer.getWidth(text)) / 2, y, color);
    }
}
