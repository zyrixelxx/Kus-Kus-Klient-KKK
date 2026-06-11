package me.kuskus.util;

import me.kuskus.KusKusKlient;
import net.fabricmc.loader.api.FabricLoader;

public final class VersionUtil {
    private VersionUtil() {
    }

    public static void check() {
        FabricLoader loader = FabricLoader.getInstance();
        loader.getModContainer("fabricloader").ifPresent(container ->
            KusKusKlient.LOGGER.info("Fabric Loader {}", container.getMetadata().getVersion().getFriendlyString()));
        if (loader.getModContainer("fabric-api").isEmpty()) {
            KusKusKlient.LOGGER.warn("Fabric API is not present");
            KusKusKlient.NOTIFICATIONS.error("Fabric API не найден");
        }
    }
}
