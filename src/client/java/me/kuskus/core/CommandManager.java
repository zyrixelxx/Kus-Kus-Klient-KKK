package me.kuskus.core;

import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.command.commands.ConfigCommand;
import me.kuskus.command.commands.FriendCommand;
import me.kuskus.command.commands.HelpCommand;
import me.kuskus.command.commands.PrefixCommand;
import me.kuskus.util.ChatUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandManager {
    private final CopyOnWriteArrayList<Command> commands = new CopyOnWriteArrayList<>();
    private String prefix = ".";

    public void init() {
        register(new HelpCommand());
        register(new FriendCommand());
        register(new PrefixCommand());
        register(new ConfigCommand());
    }

    public void register(Command command) {
        boolean duplicate = commands.stream().anyMatch(existing -> existing.name().equalsIgnoreCase(command.name()));
        if (!duplicate) {
            commands.add(command);
        }
    }

    public List<Command> all() {
        return commands.stream().sorted((a, b) -> a.name().compareToIgnoreCase(b.name())).toList();
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix != null && !prefix.isBlank()) {
            this.prefix = prefix.substring(0, 1);
        }
    }

    public boolean handleChat(String message) {
        if (message == null || !message.startsWith(prefix)) {
            return false;
        }
        dispatch(message.substring(prefix.length()));
        return true;
    }

    public void dispatch(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        String[] split = trimmed.split("\\s+");
        String name = split[0].toLowerCase(Locale.ROOT);
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        Optional<Command> command = commands.stream()
            .filter(candidate -> candidate.name().equalsIgnoreCase(name))
            .findFirst();

        if (command.isEmpty()) {
            ChatUtil.info("Unknown command: " + name);
            return;
        }

        try {
            command.get().run(args);
        } catch (CommandException exception) {
            ChatUtil.info(exception.getMessage() + " Usage: " + prefix + command.get().usage());
        } catch (RuntimeException exception) {
            ChatUtil.info("Command error: " + exception.getMessage());
        }
    }
}
