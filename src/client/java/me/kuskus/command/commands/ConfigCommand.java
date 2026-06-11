package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.util.ChatUtil;

import java.io.IOException;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", "config save|load|reload|reset|profile save/load/list/delete <name>", "Config management.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Action required.");
        }

        try {
            switch (args[0].toLowerCase()) {
                case "save" -> {
                    KusKusKlient.CONFIG.save();
                    ChatUtil.info("Config saved.");
                }
                case "load" -> {
                    KusKusKlient.CONFIG.load();
                    ChatUtil.info("Config loaded.");
                }
                case "reload" -> KusKusKlient.CONFIG.reload();
                case "reset" -> KusKusKlient.CONFIG.reset();
                case "profile" -> runProfile(args);
                default -> throw new CommandException("Unknown config action.");
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new CommandException(exception.getMessage());
        }
    }

    private void runProfile(String[] args) throws IOException, CommandException {
        if (args.length < 2) {
            throw new CommandException("Profile action required.");
        }

        switch (args[1].toLowerCase()) {
            case "save" -> {
                if (args.length < 3) throw new CommandException("Profile name required.");
                KusKusKlient.PROFILES.save(args[2]);
                ChatUtil.info("Profile saved.");
            }
            case "load" -> {
                if (args.length < 3) throw new CommandException("Profile name required.");
                KusKusKlient.PROFILES.load(args[2]);
                ChatUtil.info("Profile loaded.");
            }
            case "list" -> ChatUtil.info("Profiles: " + String.join(", ", KusKusKlient.PROFILES.list()));
            case "delete" -> {
                if (args.length < 3) throw new CommandException("Profile name required.");
                KusKusKlient.PROFILES.delete(args[2]);
                ChatUtil.info("Profile deleted.");
            }
            default -> throw new CommandException("Unknown profile action.");
        }
    }
}
