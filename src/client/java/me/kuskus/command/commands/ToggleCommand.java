package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.module.Module;
import me.kuskus.util.ChatUtil;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "toggle <module>", "Enable or disable a module.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Module required.");
        }
        Module module = KusKusKlient.MODULES.find(String.join(" ", args)).orElseThrow(() -> new CommandException("Module not found."));
        module.toggle();
        KusKusKlient.CONFIG.saveModule(module);
        ChatUtil.info(module.name() + ": " + (module.enabled() ? "on" : "off"));
    }
}
