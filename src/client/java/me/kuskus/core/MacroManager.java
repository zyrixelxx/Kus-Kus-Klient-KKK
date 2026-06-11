package me.kuskus.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.kuskus.KusKusKlient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MacroManager {
    public record Macro(String name, int key, List<String> commands) {
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final CopyOnWriteArrayList<Macro> macros = new CopyOnWriteArrayList<>();

    public List<Macro> all() {
        return List.copyOf(macros);
    }

    public void create(String name, int key, List<String> commands) {
        delete(name);
        macros.add(new Macro(name, key, new ArrayList<>(commands)));
    }

    public void delete(String name) {
        macros.removeIf(macro -> macro.name().equalsIgnoreCase(name));
    }

    public void clear() {
        macros.clear();
    }

    public void onKey(int key) {
        for (Macro macro : macros) {
            if (macro.key() == key) {
                for (String command : macro.commands()) {
                    KusKusKlient.COMMANDS.dispatch(command);
                }
            }
        }
    }

    public void load() {
        Path path = KusKusKlient.CONFIG.root().resolve("macros.json");
        if (!Files.exists(path)) {
            return;
        }
        try {
            Type type = new TypeToken<List<Macro>>() {}.getType();
            List<Macro> loaded = gson.fromJson(Files.readString(path), type);
            macros.clear();
            if (loaded != null) {
                macros.addAll(loaded);
            }
        } catch (IOException | RuntimeException exception) {
            KusKusKlient.LOGGER.warn("Failed to load macros", exception);
        }
    }

    public void save() {
        Path path = KusKusKlient.CONFIG.root().resolve("macros.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(macros));
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to save macros", exception);
        }
    }
}
