package me.kuskus.command;

public abstract class Command {
    private final String name;
    private final String usage;
    private final String description;

    protected Command(String name, String usage, String description) {
        this.name = name;
        this.usage = usage;
        this.description = description;
    }

    public String name() {
        return name;
    }

    public String usage() {
        return usage;
    }

    public String description() {
        return description;
    }

    public abstract void run(String[] args) throws CommandException;
}
