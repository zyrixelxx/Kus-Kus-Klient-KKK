package me.kuskus.module.modules.client;

import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.SettingGroup;

public class Notifications extends Module {
    public enum Mode {
        CHAT,
        TOAST
    }

    private final EnumSetting<Mode> mode;

    public Notifications() {
        super("Notifications", "Controls module toggle messages.", Category.CLIENT);
        SettingGroup general = group("General");
        mode = general.add(new EnumSetting<>("Mode", "Where notifications are shown.", Mode.TOAST, Mode.class));
        setEnabled(true);
    }

    public Mode mode() {
        return mode.get();
    }
}
