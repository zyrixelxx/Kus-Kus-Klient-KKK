package me.kuskus.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public final class EntityUtil {
    private EntityUtil() {
    }

    public static boolean isAlive(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive();
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof PlayerEntity;
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof HostileEntity;
    }

    public static double distanceTo(Entity first, Entity second) {
        return first.distanceTo(second);
    }

    public static double distanceTo(Entity entity, Vec3d pos) {
        return Math.sqrt(entity.squaredDistanceTo(pos));
    }

    public static boolean canSee(Entity source, Entity target) {
        BlockHitResult hitResult = WorldUtil.raycast(source, source.getEyePos(), target.getBoundingBox().getCenter());
        return hitResult == null || hitResult.getType() == HitResult.Type.MISS;
    }
}
