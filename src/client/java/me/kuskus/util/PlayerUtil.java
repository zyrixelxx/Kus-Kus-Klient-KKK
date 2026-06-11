package me.kuskus.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class PlayerUtil {
    private PlayerUtil() {
    }

    public static PlayerEntity player() {
        return MinecraftClient.getInstance().player;
    }

    public static float health(PlayerEntity player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    public static int hunger(PlayerEntity player) {
        return player.getHungerManager().getFoodLevel();
    }

    public static ItemStack heldItem(PlayerEntity player) {
        return player.getMainHandStack();
    }

    public static boolean isInLiquid(PlayerEntity player) {
        return player.isSubmergedInWater() || player.isInLava();
    }

    public static boolean onGround(PlayerEntity player) {
        return player.isOnGround();
    }

    public static boolean isMoving(PlayerEntity player) {
        return player.forwardSpeed != 0.0f || player.sidewaysSpeed != 0.0f;
    }

    public static boolean hasHaste(PlayerEntity player) {
        return player.hasStatusEffect(StatusEffects.HASTE);
    }
}
