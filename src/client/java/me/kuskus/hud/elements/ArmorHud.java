package me.kuskus.hud.elements;

import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHud extends HudElement {
    public ArmorHud() {
        super("Armor", 8, 72);
        width = 76;
        height = 28;
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        if (client.player == null) return;
        int offset = 0;
        EquipmentSlot[] slots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
        for (EquipmentSlot slot : slots) {
            ItemStack stack = client.player.getEquippedStack(slot);
            context.drawItem(stack, x + offset, y);
            if (stack.isDamageable()) {
                int durability = stack.getMaxDamage() - stack.getDamage();
                int percent = (int) (durability * 100.0f / stack.getMaxDamage());
                context.drawText(client.textRenderer, String.valueOf(percent), x + offset, y + 18, percent < 20 ? 0xFFFF3333 : KusKusTheme.TEXT_GRAY, false);
            }
            offset += 19;
        }
    }
}
