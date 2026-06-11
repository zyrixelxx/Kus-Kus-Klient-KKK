package me.kuskus.module;

import me.kuskus.KusKusKlient;
import me.kuskus.setting.KeybindSetting;
import me.kuskus.setting.Setting;
import me.kuskus.setting.SettingGroup;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<SettingGroup> groups = new ArrayList<>();
    private final KeybindSetting keybind = new KeybindSetting("Keybind", "Module activation key.", GLFW.GLFW_KEY_UNKNOWN);
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return category;
    }

    public KeybindSetting keybind() {
        return keybind;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
            if (this.enabled) {
                KusKusKlient.NOTIFICATIONS.on(name);
            }
        } else {
            onDisable();
            KusKusKlient.NOTIFICATIONS.off(name);
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void defaultKeybind(int key) {
        keybind.setDefaultValue(key);
        keybind.set(key);
    }

    protected SettingGroup group(String name) {
        SettingGroup group = new SettingGroup(name);
        groups.add(group);
        return group;
    }

    public List<SettingGroup> groups() {
        return Collections.unmodifiableList(groups);
    }

    public void reset() {
        setEnabled(false);
        keybind.reset();
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group.settings()) {
                setting.reset();
            }
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
