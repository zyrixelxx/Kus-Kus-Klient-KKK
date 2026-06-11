package me.kuskus.hud.elements;

import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudElement;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.SettingGroup;
import me.kuskus.setting.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class TotemHud extends HudElement {
    private final StringSetting itemId;
    private final BoolSetting showIcon;

    public TotemHud() {
        super("Totem", 8, 104);
        SettingGroup item = group("Item");
        itemId = item.add(new StringSetting("Item", "Item id to count, for example minecraft:totem_of_undying.", "minecraft:totem_of_undying"));
        showIcon = item.add(new BoolSetting("Icon", "Draw the counted item icon.", true));
    }

    @Override
    public void render(DrawContext context, MinecraftClient client) {
        if (client.player == null) return;
        Item item = resolveItem();
        int count = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }
        String text = "x" + count;
        int textX = showIcon.get() ? x + 20 : x;
        width = (showIcon.get() ? 20 : 0) + client.textRenderer.getWidth(text);
        height = 18;
        renderBackground(context);
        if (showIcon.get()) {
            context.drawItem(new ItemStack(item), x, y);
        }
        RenderUtil.text(context, client.textRenderer, text, textX, y + 5, textColor());
    }

    private Item resolveItem() {
        String raw = itemId.get().trim();
        if (!raw.contains(":")) {
            raw = "minecraft:" + raw;
        }
        Identifier id = Identifier.tryParse(raw);
        if (id == null) {
            return Items.TOTEM_OF_UNDYING;
        }
        return Registries.ITEM.getOptionalValue(id).orElse(Items.TOTEM_OF_UNDYING);
    }
}
