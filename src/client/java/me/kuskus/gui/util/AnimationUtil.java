package me.kuskus.gui.util;

import me.kuskus.util.MathUtil;

public final class AnimationUtil {
    private AnimationUtil() {
    }

    public static float lerp(float from, float to, float delta) {
        return from + (to - from) * MathUtil.clamp(delta, 0.0f, 1.0f);
    }

    public static double lerp(double from, double to, double delta) {
        return from + (to - from) * MathUtil.clamp(delta, 0.0, 1.0);
    }

    public static float easeOutQuad(float value) {
        float t = MathUtil.clamp(value, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    public static float easeInOutCubic(float value) {
        float t = MathUtil.clamp(value, 0.0f, 1.0f);
        return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
    }

    public static float easeOutBack(float value) {
        float t = MathUtil.clamp(value, 0.0f, 1.0f);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float) Math.pow(t - 1.0f, 3.0) + c1 * (float) Math.pow(t - 1.0f, 2.0);
    }

    public static float easeOutBounce(float value) {
        float t = MathUtil.clamp(value, 0.0f, 1.0f);
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (t < 1.0f / d1) {
            return n1 * t * t;
        }
        if (t < 2.0f / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        }
        if (t < 2.5f / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        }

        t -= 2.625f / d1;
        return n1 * t * t + 0.984375f;
    }
}
