package me.kuskus.util;

import java.awt.Color;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int blend(int first, int second, float progress) {
        float clamped = MathUtil.clamp(progress, 0.0f, 1.0f);
        int a = Math.round(alpha(first) + (alpha(second) - alpha(first)) * clamped);
        int r = Math.round(red(first) + (red(second) - red(first)) * clamped);
        int g = Math.round(green(first) + (green(second) - green(first)) * clamped);
        int b = Math.round(blue(first) + (blue(second) - blue(first)) * clamped);
        return argb(a, r, g, b);
    }

    public static int rainbow(float speed, float saturation, float brightness, long timeOffset) {
        float hue = ((System.currentTimeMillis() + timeOffset) % (long) Math.max(1.0f, speed)) / Math.max(1.0f, speed);
        return 0xFF000000 | Color.HSBtoRGB(hue, MathUtil.clamp(saturation, 0.0f, 1.0f), MathUtil.clamp(brightness, 0.0f, 1.0f));
    }

    public static int fromHsb(float hue, float saturation, float brightness) {
        return 0xFF000000 | Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static float[] toHsb(int color) {
        return Color.RGBtoHSB(red(color), green(color), blue(color), null);
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }
}
