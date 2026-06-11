package me.kuskus.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ChatUtil {
    private static final String PREFIX = "[Kus Kus] ";

    private ChatUtil() {
    }

    public static void info(String message) {
        message(Formatting.GOLD, message);
    }

    public static void warning(String message) {
        message(Formatting.YELLOW, message);
    }

    public static void error(String message) {
        message(Formatting.RED, message);
    }

    public static void colored(Formatting color, String message) {
        message(color, message);
    }

    public static void sendCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.networkHandler == null || command == null || command.isBlank()) {
            return;
        }

        client.player.networkHandler.sendChatCommand(command.startsWith("/") ? command.substring(1) : command);
    }

    private static void message(Formatting color, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(prefix().append(Text.literal(message).formatted(color)));
        }
    }

    private static MutableText prefix() {
        return Text.literal(PREFIX).formatted(Formatting.GOLD, Formatting.BOLD);
    }
}
