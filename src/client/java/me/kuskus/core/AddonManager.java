package me.kuskus.core;

import me.kuskus.KusKusKlient;
import me.kuskus.addon.Addon;
import me.kuskus.addon.AddonContext;
import me.kuskus.addon.AddonMetadata;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AddonManager {
    private final CopyOnWriteArrayList<Addon> addons = new CopyOnWriteArrayList<>();
    private final Map<String, AddonContext> contexts = new ConcurrentHashMap<>();

    public void init() {
        for (EntrypointContainer<Addon> container : FabricLoader.getInstance().getEntrypointContainers("kuskusklient-addon", Addon.class)) {
            try {
                register(container.getEntrypoint());
            } catch (RuntimeException exception) {
                String provider = container.getProvider().getMetadata().getId();
                KusKusKlient.LOGGER.warn("Failed to initialize addon entrypoint from {}", provider, exception);
            }
        }
    }

    public void register(Addon addon) {
        AddonMetadata metadata = addon.metadata();
        String key = metadata.id().toLowerCase(Locale.ROOT);
        if (contexts.containsKey(key)) {
            KusKusKlient.LOGGER.warn("Addon id '{}' is already registered, skipping duplicate.", metadata.id());
            return;
        }

        AddonContext context = new AddonContext(
            addon,
            LoggerFactory.getLogger(KusKusKlient.NAME + "/" + metadata.id()),
            KusKusKlient.EVENTS,
            KusKusKlient.MODULES,
            KusKusKlient.COMMANDS,
            KusKusKlient.CONFIG,
            KusKusKlient.FRIENDS,
            KusKusKlient.MACROS,
            KusKusKlient.PROFILES,
            KusKusKlient.NOTIFICATIONS,
            KusKusKlient.HUD,
            KusKusKlient.CONFIG.addonRoot(metadata.id()),
            KusKusKlient.VERSION
        );

        try {
            Files.createDirectories(context.dataDirectory());
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to prepare addon data directory for {}", metadata.id(), exception);
        }

        addon.attach(context);
        addon.onInitialize(context);
        addon.modules().forEach(KusKusKlient.MODULES::register);
        addon.commands().forEach(KusKusKlient.COMMANDS::register);
        addons.addIfAbsent(addon);
        contexts.put(key, context);
        KusKusKlient.LOGGER.info("Registered addon {} v{}", metadata.name(), metadata.version());
    }

    public List<Addon> all() {
        return List.copyOf(addons);
    }

    public void shutdown() {
        for (int i = addons.size() - 1; i >= 0; i--) {
            Addon addon = addons.get(i);
            try {
                addon.onShutdown();
            } catch (RuntimeException exception) {
                KusKusKlient.LOGGER.warn("Failed to shut down addon {}", addon.metadata().id(), exception);
            }
        }
    }
}
