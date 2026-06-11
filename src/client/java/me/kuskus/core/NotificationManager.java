package me.kuskus.core;

import me.kuskus.KusKusKlient;
import me.kuskus.gui.KusKusScreen;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.hud.HudEditorScreen;
import me.kuskus.module.modules.client.Notifications;
import me.kuskus.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class NotificationManager {
    public enum Type {
        ON(0xFF33CC66, Formatting.GREEN),
        OFF(0xFFCC3344, Formatting.RED),
        INFO(0xFFFF6600, Formatting.GOLD),
        ERROR(0xFF8B1A1A, Formatting.DARK_RED),
        FRIEND(0xFF3388FF, Formatting.AQUA);

        private final int color;
        private final Formatting chatPrefix;

        Type(int color, Formatting chatPrefix) {
            this.color = color;
            this.chatPrefix = chatPrefix;
        }

        public Formatting chatPrefix() {
            return chatPrefix;
        }
    }

    private record Toast(String text, Type type, long created) {
    }

    private final Deque<Toast> toasts = new ArrayDeque<>();
    private boolean muted;

    public void on(String module) {
        push(module + " enabled", Type.ON);
    }

    public void off(String module) {
        push(module + " disabled", Type.OFF);
    }

    public void info(String text) {
        push(text, Type.INFO);
    }

    public void error(String text) {
        push(text, Type.ERROR);
    }

    public void friend(String text) {
        push(text, Type.FRIEND);
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            toasts.clear();
        }
    }

    public void push(String text, Type type) {
        if (muted || text == null || text.isBlank()) {
            return;
        }

        Notifications module = module();
        if (module != null && !module.enabled()) {
            return;
        }

        if (module != null && module.mode() == Notifications.Mode.CHAT) {
            ChatUtil.colored(type.chatPrefix(), text);
            return;
        }

        while (toasts.size() >= 5) {
            toasts.removeFirst();
        }
        toasts.addLast(new Toast(text, type, System.currentTimeMillis()));
    }

    public void render(DrawContext context) {
        if (muted || toasts.isEmpty()) {
            return;
        }

        Notifications module = module();
        if (module != null && (!module.enabled() || module.mode() != Notifications.Mode.TOAST)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }
        if (client.currentScreen instanceof HudEditorScreen || client.currentScreen instanceof KusKusScreen) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int y = height - 24;
        long now = System.currentTimeMillis();
        toasts.removeIf(toast -> now - toast.created > 3600);

        Iterator<Toast> iterator = toasts.descendingIterator();
        while (iterator.hasNext()) {
            Toast toast = iterator.next();
            int age = (int) (now - toast.created);
            int alpha = age > 3000 ? Math.max(0, 255 - (age - 3000) / 2) : 255;
            int boxWidth = Math.max(120, client.textRenderer.getWidth(toast.text) + 18);
            int x = width - boxWidth - 8;
            int bg = (alpha << 24) | (KusKusTheme.BG_PANEL & 0xFFFFFF);
            int stripe = (alpha << 24) | (toast.type.color & 0xFFFFFF);
            int outline = (alpha << 24) | (KusKusTheme.outline() & 0xFFFFFF);

            RenderUtil.rect(context, x, y, boxWidth, 18, bg);
            RenderUtil.rect(context, x, y, 3, 18, stripe);
            RenderUtil.outline(context, x, y, boxWidth, 18, outline);
            RenderUtil.text(context, client.textRenderer, toast.text, x + 8, y + 5, (alpha << 24) | (KusKusTheme.TEXT_MAIN & 0xFFFFFF));
            y -= 22;
        }
    }

    private Notifications module() {
        return KusKusKlient.MODULES.find("Notifications")
            .filter(Notifications.class::isInstance)
            .map(Notifications.class::cast)
            .orElse(null);
    }
}
