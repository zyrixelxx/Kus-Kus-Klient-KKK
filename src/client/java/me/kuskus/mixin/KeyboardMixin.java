package me.kuskus.mixin;

import me.kuskus.KusKusKlient;
import me.kuskus.event.events.KeyEvent;
import me.kuskus.gui.KusKusScreen;
import me.kuskus.hud.HudEditorScreen;
import me.kuskus.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void kuskus$onKey(long window, int key, KeyInput input, CallbackInfo ci) {
        int keyCode = input.key();
        int scancode = input.scancode();
        int modifiers = input.modifiers();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow().getHandle() != window) {
            return;
        }

        KusKusKlient.EVENTS.post(new KeyEvent(window, keyCode, scancode, GLFW.GLFW_PRESS, modifiers));

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && client.currentScreen instanceof HudEditorScreen) {
            client.currentScreen.close();
            ci.cancel();
            return;
        }

        Optional<Module> hudModule = KusKusKlient.MODULES.find("HUD");
        if (!(client.currentScreen instanceof HudEditorScreen)
            && !(client.currentScreen instanceof KusKusScreen)
            && hudModule.isPresent()
            && hudModule.get().keybind().get() == keyCode) {
            hudModule.get().toggle();
            return;
        }

        Optional<Module> uiModule = KusKusKlient.MODULES.find("UI");
        if (!(client.currentScreen instanceof KusKusScreen)
            && uiModule.isPresent()
            && uiModule.get().keybind().get() == keyCode) {
            uiModule.get().toggle();
            return;
        }

        if (client.currentScreen == null) {
            KusKusKlient.MODULES.onKeyPress(keyCode);
            KusKusKlient.MACROS.onKey(keyCode);
        }
    }
}
