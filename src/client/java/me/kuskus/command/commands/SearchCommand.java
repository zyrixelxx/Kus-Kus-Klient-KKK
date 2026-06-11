package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.module.Module;
import me.kuskus.util.ChatUtil;

import java.util.List;

public class SearchCommand extends Command {
    public SearchCommand() {
        super("search", "search <query>", "Search modules.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Query required.");
        }

        List<Module> matches = KusKusKlient.MODULES.search(String.join(" ", args));
        if (matches.isEmpty()) {
            ChatUtil.info("No modules matched.");
            return;
        }

        for (Module module : matches) {
            ChatUtil.info(module.name() + " [" + module.category() + "] - " + module.description());
        }
    }
}
