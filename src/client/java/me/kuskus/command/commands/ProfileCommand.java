package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.util.ChatUtil;

import java.io.IOException;

public class ProfileCommand extends Command {
    public ProfileCommand() {
        super("profile", "profile save|load|delete|list <name>", "Manage profile snapshots.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Action required.");
        }
        try {
            switch (args[0].toLowerCase()) {
                case "save" -> {
                    if (args.length < 2) throw new CommandException("Name required.");
                    KusKusKlient.PROFILES.save(args[1]);
                    ChatUtil.info("Profile saved.");
                }
                case "load" -> {
                    if (args.length < 2) throw new CommandException("Name required.");
                    KusKusKlient.PROFILES.load(args[1]);
                    ChatUtil.info("Profile loaded.");
                }
                case "delete" -> {
                    if (args.length < 2) throw new CommandException("Name required.");
                    KusKusKlient.PROFILES.delete(args[1]);
                    ChatUtil.info("Profile deleted.");
                }
                case "list" -> ChatUtil.info("Profiles: " + String.join(", ", KusKusKlient.PROFILES.list()));
                default -> throw new CommandException("Unknown action.");
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new CommandException(exception.getMessage());
        }
    }
}
