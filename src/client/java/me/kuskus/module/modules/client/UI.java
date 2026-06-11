package me.kuskus.module.modules.client;

import me.kuskus.gui.KusKusScreen;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.ColorSetting;
import me.kuskus.setting.SettingGroup;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class UI extends Module {
    private final ColorSetting accent;
    private final ColorSetting text;
    private final ColorSetting panel;
    private final ColorSetting button;

    public UI() {
        super("UI", "Opens the client click GUI.", Category.CLIENT);
        defaultKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);

        SettingGroup colors = group("Colors");
        accent = colors.add(new ColorSetting("Accent", "Primary client accent color.", KusKusTheme.PRIMARY & 0xFFFFFF));
        text = colors.add(new ColorSetting("Text", "Main GUI text color.", KusKusTheme.TEXT_MAIN & 0xFFFFFF));
        panel = colors.add(new ColorSetting("Panel", "Panel background color.", KusKusTheme.BG_PANEL & 0xFFFFFF));
        button = colors.add(new ColorSetting("Button", "Module button background color.", KusKusTheme.BG_BUTTON & 0xFFFFFF));

        applyTheme();
    }

    public void applyTheme() {
        KusKusTheme.setAccent(accent.get());
        KusKusTheme.setTextColor(text.get());
        KusKusTheme.setPanelColor(panel.get());
        KusKusTheme.setButtonColor(button.get());
        KusKusTheme.setAccent(accent.get());
    }

    @Override
    public void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && !(client.currentScreen instanceof KusKusScreen)) {
            client.setScreen(new KusKusScreen());
        }
    }
}
