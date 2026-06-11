package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.module.Module;
import me.kuskus.util.ChatUtil;
import me.kuskus.util.KeyUtil;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "bind <module> <key|none>", "Assign a keybind to a module.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("Module and key required.");
        }
        Module module = KusKusKlient.MODULES.find(args[0]).orElseThrow(() -> new CommandException("Module not found."));
        module.keybind().set(KeyUtil.code(args[1]));
        KusKusKlient.CONFIG.saveModule(module);
        ChatUtil.info(module.name() + " bind: " + KeyUtil.name(module.keybind().get()));
    }
}
