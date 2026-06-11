package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.util.ChatUtil;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix", "prefix <symbol>", "Change command prefix and save it.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Prefix symbol required.");
        }
        KusKusKlient.COMMANDS.setPrefix(args[0]);
        KusKusKlient.CONFIG.save();
        ChatUtil.info("Prefix: " + KusKusKlient.COMMANDS.prefix());
    }
}
