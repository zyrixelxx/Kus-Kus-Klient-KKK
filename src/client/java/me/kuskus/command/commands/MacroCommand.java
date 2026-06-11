package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;
import me.kuskus.command.CommandException;
import me.kuskus.core.MacroManager;
import me.kuskus.util.ChatUtil;
import me.kuskus.util.KeyUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "macro create|delete|list|run <name>", "Manage command macros.");
    }

    @Override
    public void run(String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Action required.");
        }
        switch (args[0].toLowerCase()) {
            case "create" -> create(args);
            case "delete" -> {
                if (args.length < 2) throw new CommandException("Name required.");
                KusKusKlient.MACROS.delete(args[1]);
                KusKusKlient.MACROS.save();
                ChatUtil.info("Macro deleted.");
            }
            case "list" -> list();
            case "run" -> runMacro(args);
            default -> throw new CommandException("Unknown action.");
        }
    }

    private void create(String[] args) throws CommandException {
        if (args.length < 4) {
            throw new CommandException("Usage: macro create <name> <key> <command1 ;; command2>");
        }

        int key = KeyUtil.code(args[2]);
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            throw new CommandException("Unknown key.");
        }

        String rawCommands = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        List<String> commands = Arrays.stream(rawCommands.split("\\s*;;\\s*"))
            .map(String::trim)
            .filter(command -> !command.isBlank())
            .toList();
        if (commands.isEmpty()) {
            throw new CommandException("At least one command is required.");
        }

        KusKusKlient.MACROS.create(args[1], key, commands);
        KusKusKlient.MACROS.save();
        ChatUtil.info("Macro saved with " + commands.size() + " command(s).");
    }

    private void list() {
        if (KusKusKlient.MACROS.all().isEmpty()) {
            ChatUtil.info("No macros saved.");
            return;
        }

        for (MacroManager.Macro macro : KusKusKlient.MACROS.all()) {
            ChatUtil.info(macro.name() + " [" + KeyUtil.name(macro.key()) + "] x" + macro.commands().size());
        }
    }

    private void runMacro(String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("Name required.");
        }

        MacroManager.Macro macro = KusKusKlient.MACROS.all().stream()
            .filter(candidate -> candidate.name().equalsIgnoreCase(args[1]))
            .findFirst()
            .orElseThrow(() -> new CommandException("Macro not found."));
        macro.commands().forEach(KusKusKlient.COMMANDS::dispatch);
    }
}
