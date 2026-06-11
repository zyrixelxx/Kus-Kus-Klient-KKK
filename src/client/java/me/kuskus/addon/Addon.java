package me.kuskus.addon;

import me.kuskus.command.Command;
import me.kuskus.module.Module;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Addon {
    private AddonContext context;

    public abstract String name();

    public AddonMetadata metadata() {
        return AddonMetadata.basic(name());
    }

    public void onInitialize(AddonContext context) {
    }

    public void onShutdown() {
    }

    public List<Module> modules() {
        return List.of();
    }

    public List<Command> commands() {
        return List.of();
    }

    public final String id() {
        return metadata().id();
    }

    public final Optional<AddonContext> context() {
        return Optional.ofNullable(context);
    }

    public final AddonContext requireContext() {
        return Objects.requireNonNull(context, "Addon has not been initialized yet.");
    }

    public final void attach(AddonContext context) {
        this.context = context;
    }
}
