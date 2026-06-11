package me.kuskus.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {
    private RotationUtil() {
    }

    public static float[] rotationsTo(Entity from, Entity target) {
        return rotationsTo(from.getEyePos(), target.getBoundingBox().getCenter());
    }

    public static float[] rotationsTo(Vec3d from, Vec3d target) {
        Vec3d delta = target.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
        return new float[] { MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch) };
    }

    public static float smooth(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        return current + MathUtil.clamp(delta, -maxStep, maxStep);
    }
}
