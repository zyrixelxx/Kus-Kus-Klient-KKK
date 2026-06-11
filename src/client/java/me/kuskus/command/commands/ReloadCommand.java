package me.kuskus.command.commands;

import me.kuskus.KusKusKlient;
import me.kuskus.command.Command;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "reload", "Reload the live config.");
    }

    @Override
    public void run(String[] args) {
        KusKusKlient.CONFIG.reload();
    }
}
