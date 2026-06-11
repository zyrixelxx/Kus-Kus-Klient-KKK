package me.kuskus.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MathUtil {
    private MathUtil() {
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float from, float to, float delta) {
        return MathHelper.lerp(clamp(delta, 0.0f, 1.0f), from, to);
    }

    public static double lerp(double from, double to, double delta) {
        return MathHelper.lerp(clamp(delta, 0.0, 1.0), from, to);
    }

    public static float wrapDegrees(float degrees) {
        return MathHelper.wrapDegrees(degrees);
    }

    public static double wrapDegrees(double degrees) {
        return MathHelper.wrapDegrees((float) degrees);
    }

    public static double square(double value) {
        return value * value;
    }

    public static double distance3D(Vec3d from, Vec3d to) {
        return from.distanceTo(to);
    }
}
