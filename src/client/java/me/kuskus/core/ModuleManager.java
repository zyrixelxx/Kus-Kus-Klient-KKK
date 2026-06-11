package me.kuskus.core;

import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.module.modules.client.HUD;
import me.kuskus.module.modules.client.Notifications;
import me.kuskus.module.modules.client.UI;
import me.kuskus.module.modules.combat.AutoArmor;
import me.kuskus.module.modules.combat.KillAura;
import me.kuskus.module.modules.misc.DiscordPresence;
import me.kuskus.module.modules.movement.Flight;
import me.kuskus.module.modules.movement.Sprint;
import me.kuskus.module.modules.player.AutoEat;
import me.kuskus.module.modules.render.ESP;
import me.kuskus.module.modules.world.AutoMine;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleManager {
    private final CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<>();

    public void init() {
        register(new KillAura());
        register(new AutoArmor());
        register(new Sprint());
        register(new Flight());
        register(new AutoEat());
        register(new ESP());
        register(new AutoMine());
        register(new DiscordPresence());
        register(new UI());
        register(new HUD());
        register(new Notifications());
    }

    public void register(Module module) {
        modules.addIfAbsent(module);
    }

    public List<Module> all() {
        return List.copyOf(modules);
    }

    public List<Module> byCategory(Category category) {
        return modules.stream().filter(module -> module.category() == category).toList();
    }

    public List<Module> enabled() {
        return modules.stream().filter(Module::enabled).toList();
    }

    public Optional<Module> find(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return modules.stream()
            .filter(module -> module.name().toLowerCase(Locale.ROOT).startsWith(normalized))
            .findFirst()
            .or(() -> modules.stream().filter(module -> module.name().toLowerCase(Locale.ROOT).contains(normalized)).findFirst());
    }

    public List<Module> search(String query) {
        if (query == null || query.isBlank()) {
            return all();
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.name().toLowerCase(Locale.ROOT).contains(normalized)
                || module.description().toLowerCase(Locale.ROOT).contains(normalized)
                || module.category().name().toLowerCase(Locale.ROOT).contains(normalized)) {
                result.add(module);
            }
        }
        return result;
    }

    public void onKeyPress(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }
        for (Module module : modules) {
            if (module.keybind().get() == key) {
                module.toggle();
            }
        }
    }
}
