package me.kuskus.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class WorldUtil {
    private WorldUtil() {
    }

    public static World world() {
        return MinecraftClient.getInstance().world;
    }

    public static BlockState getBlock(BlockPos pos) {
        World world = world();
        return world == null ? null : world.getBlockState(pos);
    }

    public static boolean isSolid(BlockPos pos) {
        BlockState state = getBlock(pos);
        return state != null && state.isSolidBlock(world(), pos);
    }

    public static BlockHitResult raycast(Entity entity, Vec3d from, Vec3d to) {
        World world = world();
        if (world == null) {
            return null;
        }

        return world.raycast(new RaycastContext(
            from,
            to,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            entity
        ));
    }
}
