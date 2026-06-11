package me.kuskus.module.modules.misc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.RichPresence;
import me.kuskus.KusKusKlient;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.SettingGroup;
import net.minecraft.client.MinecraftClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordPresence extends Module {
    private static final long APP_ID = 1510397978031165471L;

    private final BoolSetting showIp;
    private final BoolSetting showDimension;
    private final BoolSetting showTime;
    private ScheduledExecutorService scheduler;
    private IPCClient ipcClient;
    private long sessionStart;

    public DiscordPresence() {
        super("DiscordPresence", "Discord Rich Presence.", Category.MISC);
        SettingGroup general = group("General");
        showIp = general.add(new BoolSetting("Show IP", "Show server address.", false));
        showDimension = general.add(new BoolSetting("Show Dimension", "Show current dimension.", true));
        showTime = general.add(new BoolSetting("Show Time", "Show session time.", true));
    }

    @Override
    protected void onEnable() {
        sessionStart = System.currentTimeMillis();
        KusKusKlient.LOGGER.info("Discord Presence enabling with app id {}", APP_ID);
        scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "Kus Kus Discord Presence");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, throwable) -> KusKusKlient.LOGGER.error("Discord Presence thread crashed", throwable));
            return thread;
        });
        scheduler.execute(this::connectQuietly);
    }

    @Override
    protected void onDisable() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (ipcClient != null) {
            try {
                ipcClient.close();
            } catch (RuntimeException exception) {
                KusKusKlient.LOGGER.debug("Discord IPC close failed", exception);
            }
            ipcClient = null;
        }
    }

    private void connectQuietly() {
        try {
            ipcClient = new IPCClient(APP_ID);
            ipcClient.connect();
            updateQuietly();
            KusKusKlient.NOTIFICATIONS.info("Discord Presence connected");
            KusKusKlient.LOGGER.info("Discord Presence connected");
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.scheduleAtFixedRate(this::updateQuietly, 5, 5, TimeUnit.SECONDS);
            }
        } catch (Throwable exception) {
            KusKusKlient.LOGGER.error("Discord Presence connection failed", exception);
            KusKusKlient.NOTIFICATIONS.error("Discord Presence disabled");
            setEnabled(false);
        }
    }

    private void updateQuietly() {
        try {
            if (ipcClient == null) {
                return;
            }
            RichPresence.Builder builder = new RichPresence.Builder()
                .setDetails("Kus Kus Klient v" + KusKusKlient.VERSION)
                .setState(state());
            if (showTime.get()) {
                builder.setStartTimestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(sessionStart), ZoneOffset.UTC));
            }
            ipcClient.sendRichPresence(builder.build());
        } catch (RuntimeException exception) {
            KusKusKlient.LOGGER.warn("Discord Presence update failed", exception);
            KusKusKlient.NOTIFICATIONS.error("Discord Presence disabled");
            setEnabled(false);
        }
    }

    private String state() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return "Main Menu";
        }
        if (client.isInSingleplayer()) {
            return "Singleplayer";
        }
        if (showDimension.get() && client.world.getRegistryKey() != null) {
            return "Multiplayer | " + client.world.getRegistryKey().getValue().getPath();
        }
        if (showIp.get() && client.getCurrentServerEntry() != null) {
            return "Server: " + client.getCurrentServerEntry().address;
        }
        return "Multiplayer";
    }
}
