package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.util.ChatUtil;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "help [page]", "Show commands.");
    }

    @Override
    public void run(String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Math.max(1, Integer.parseInt(args[0]));
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }

        int perPage = 8;
        var commands = KusKusKlient.COMMANDS.all();
        int pages = Math.max(1, (int) Math.ceil(commands.size() / (double) perPage));
        page = Math.min(page, pages);
        ChatUtil.info("Commands " + page + "/" + pages);
        for (int i = (page - 1) * perPage; i < Math.min(commands.size(), page * perPage); i++) {
            Command command = commands.get(i);
            ChatUtil.info(KusKusKlient.COMMANDS.prefix() + command.usage() + " - " + command.description());
        }
    }
}
