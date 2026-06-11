package me.kuskus;

import me.kuskus.core.AddonManager;
import me.kuskus.core.CommandManager;
import me.kuskus.core.ConfigManager;
import me.kuskus.core.FriendManager;
import me.kuskus.core.MacroManager;
import me.kuskus.core.ModuleManager;
import me.kuskus.core.NotificationManager;
import me.kuskus.core.ProfileManager;
import me.kuskus.crash.CrashHandler;
import me.kuskus.event.EventBus;
import me.kuskus.event.events.ChatEvent;
import me.kuskus.event.events.RenderEvent;
import me.kuskus.event.events.TickEvent;
import me.kuskus.hud.HudManager;
import me.kuskus.util.VersionUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KusKusKlient implements ClientModInitializer {
    public static final String MOD_ID = "kuskusklient";
    public static final String NAME = "Kus Kus Klient";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final int DEFAULT_GUI_X = 32;
    public static final int DEFAULT_GUI_Y = 32;
    public static final int DEFAULT_GUI_KEYBIND = GLFW.GLFW_KEY_RIGHT_SHIFT;
    public static final int DEFAULT_GUI_SEARCH_KEYBIND = GLFW.GLFW_KEY_F;

    public static final EventBus EVENTS = new EventBus();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final ConfigManager CONFIG = new ConfigManager();
    public static final FriendManager FRIENDS = new FriendManager();
    public static final AddonManager ADDONS = new AddonManager();
    public static final CommandManager COMMANDS = new CommandManager();
    public static final ProfileManager PROFILES = new ProfileManager();
    public static final MacroManager MACROS = new MacroManager();
    public static final NotificationManager NOTIFICATIONS = new NotificationManager();
    public static final HudManager HUD = new HudManager();

    public static int GUI_X = DEFAULT_GUI_X;
    public static int GUI_Y = DEFAULT_GUI_Y;
    public static int GUI_KEYBIND = DEFAULT_GUI_KEYBIND;
    public static int GUI_SEARCH_KEYBIND = DEFAULT_GUI_SEARCH_KEYBIND;

    @Override
    public void onInitializeClient() {
        CrashHandler.install();
        NOTIFICATIONS.setMuted(true);
        MODULES.init();
        COMMANDS.init();
        HUD.init();
        ADDONS.init();
        CONFIG.load();
        NOTIFICATIONS.setMuted(false);
        VersionUtil.check();

        ClientTickEvents.END_CLIENT_TICK.register(client -> EVENTS.post(new TickEvent(client)));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CONFIG.save();
            ADDONS.shutdown();
        });
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            HUD.render(context);
            NOTIFICATIONS.render(context);
            EVENTS.post(new RenderEvent(context, tickCounter));
        });
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            ChatEvent event = EVENTS.post(new ChatEvent(message));
            return !event.isCancelled() && !COMMANDS.handleChat(message);
        });
        LOGGER.info("{} initialized", NAME);
    }
}
