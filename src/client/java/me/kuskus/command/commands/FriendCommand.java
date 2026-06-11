package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.friend.Friend;
import me.kuskus.util.ChatUtil;

import java.util.Arrays;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "friend add/remove/list/note <name> [note]", "Friend list management.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Action required.");
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) throw new CommandException("Name required.");
                KusKusKlient.FRIENDS.add(args[1], args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "");
                KusKusKlient.CONFIG.save();
                KusKusKlient.NOTIFICATIONS.friend("Friend added: " + args[1]);
            }
            case "remove" -> {
                if (args.length < 2) throw new CommandException("Name required.");
                KusKusKlient.FRIENDS.remove(args[1]);
                KusKusKlient.CONFIG.save();
                ChatUtil.info("Friend removed: " + args[1]);
            }
            case "list" -> {
                for (Friend friend : KusKusKlient.FRIENDS.all()) {
                    ChatUtil.info(friend.name() + (friend.note().isBlank() ? "" : " - " + friend.note()));
                }
            }
            case "note" -> {
                if (args.length < 3) throw new CommandException("Name and note required.");
                KusKusKlient.FRIENDS.add(args[1], String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                KusKusKlient.CONFIG.save();
                ChatUtil.info("Friend note updated: " + args[1]);
            }
            default -> throw new CommandException("Unknown friend action.");
        }
    }
}
