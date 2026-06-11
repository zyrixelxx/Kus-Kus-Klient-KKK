package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.util.ChatUtil;

public class ModsCommand extends Command {
    public ModsCommand() {
        super("mods", "mods [category]", "List modules.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        Category category = null;
        if (args.length > 0) {
            try {
                category = Category.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new CommandException("Unknown category.");
            }
        }

        for (Module module : KusKusKlient.MODULES.all()) {
            if (category == null || module.category() == category) {
                ChatUtil.info(module.category() + " / " + module.name() + " / " + (module.enabled() ? "on" : "off"));
            }
        }
    }
}
