package me.kuskus.event.events;

import me.kuskus.event.Event;
import net.minecraft.client.MinecraftClient;

public class TickEvent extends Event {
    private final MinecraftClient client;

    public TickEvent(MinecraftClient client) {
        this.client = client;
    }

    public MinecraftClient client() {
        return client;
    }
}
