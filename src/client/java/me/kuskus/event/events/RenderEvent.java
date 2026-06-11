package me.kuskus.event.events;

import me.kuskus.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class RenderEvent extends Event {
    private final DrawContext context;
    private final RenderTickCounter tickCounter;

    public RenderEvent(DrawContext context, RenderTickCounter tickCounter) {
        this.context = context;
        this.tickCounter = tickCounter;
    }

    public DrawContext context() {
        return context;
    }

    public RenderTickCounter tickCounter() {
        return tickCounter;
    }
}
