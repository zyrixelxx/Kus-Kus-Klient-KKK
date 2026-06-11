package me.kuskus.module.modules.client;

import me.kuskus.hud.HudEditorScreen;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class HUD extends Module {
    public HUD() {
        super("HUD", "Opens the HUD editor.", Category.CLIENT);
        defaultKeybind(GLFW.GLFW_KEY_F12);
    }

    @Override
    public void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && !(client.currentScreen instanceof HudEditorScreen)) {
            client.setScreen(new HudEditorScreen());
        }
    }
}
