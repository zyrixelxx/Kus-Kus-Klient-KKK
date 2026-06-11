package me.kuskus.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // HUD rendering is registered through Fabric rendering events in KusKusKlient.
}
