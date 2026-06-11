package me.kuskus.addon;

import me.kuskus.KusKusKlient;
import me.kuskus.core.CommandManager;
import me.kuskus.core.ConfigManager;
import me.kuskus.core.FriendManager;
import me.kuskus.core.MacroManager;
import me.kuskus.core.ModuleManager;
import me.kuskus.core.NotificationManager;
import me.kuskus.core.ProfileManager;
import me.kuskus.event.EventBus;
import me.kuskus.hud.HudManager;
import org.slf4j.Logger;

import java.nio.file.Path;

public record AddonContext(
    Addon addon,
    Logger logger,
    EventBus events,
    ModuleManager modules,
    CommandManager commands,
    ConfigManager config,
    FriendManager friends,
    MacroManager macros,
    ProfileManager profiles,
    NotificationManager notifications,
    HudManager hud,
    Path dataDirectory,
    String clientVersion
) {
    public boolean isClient(String version) {
        return KusKusKlient.VERSION.equalsIgnoreCase(version);
    }
}
