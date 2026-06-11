package me.kuskus.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kuskus.KusKusKlient;
import me.kuskus.friend.Friend;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.module.Module;
import me.kuskus.module.modules.client.UI;
import me.kuskus.setting.Setting;
import me.kuskus.setting.SettingGroup;
import me.kuskus.util.KeyUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ConfigManager {
    public static final int CONFIG_VERSION = 3;

    private static final String GLOBAL_FILE = "config.json";
    private static final String FRIENDS_FILE = "friends.json";
    private static final String HUD_FILE = "hud.json";
    private static final String MACROS_FILE = "macros.json";
    private static final String MODULES_DIR = "modules";
    private static final String PROFILES_DIR = "profiles";
    private static final String CONFIGS_DIR = "configs";
    private static final String ADDONS_DIR = "addons";
    private static final String CRASHES_DIR = "crashes";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path root = FabricLoader.getInstance().getGameDir().resolve("kuskusklient");

    public Path root() {
        return root;
    }

    public Path addonRoot(String addonId) {
        return root.resolve(ADDONS_DIR).resolve(normalizeSnapshotName(addonId));
    }

    public String normalizeSnapshotName(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9._-]+", "-")
            .replaceAll("^-+|-+$", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Name must contain at least one letter or number.");
        }
        return normalized;
    }

    public void load() {
        try {
            withMutedNotifications(() -> {
                ensureRuntimeDirectories();
                resetRuntimeState();
                loadGlobal();
                loadModules();
                loadFriends();
                KusKusKlient.HUD.load();
                KusKusKlient.MACROS.load();
            });
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to load Kus Kus Klient config", exception);
        }
    }

    public void save() {
        try {
            ensureRuntimeDirectories();
            saveGlobal();
            saveModules();
            saveFriends();
            KusKusKlient.HUD.save();
            KusKusKlient.MACROS.save();
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to save Kus Kus Klient config", exception);
        }
    }

    public void reload() {
        save();
        load();
        KusKusKlient.NOTIFICATIONS.info("Config reloaded.");
    }

    public void reset() {
        try {
            withMutedNotifications(() -> {
                ensureRuntimeDirectories();
                resetRuntimeState();
                clearLiveState();
                save();
            });
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to reset Kus Kus Klient config", exception);
        }
        KusKusKlient.NOTIFICATIONS.info("Config reset to defaults.");
    }

    public void saveNamedConfig(String name) throws IOException {
        saveSnapshot(root.resolve(CONFIGS_DIR).resolve(normalizeSnapshotName(name)));
    }

    public void loadNamedConfig(String name) throws IOException {
        loadSnapshot(root.resolve(CONFIGS_DIR).resolve(normalizeSnapshotName(name)));
    }

    public void deleteNamedConfig(String name) throws IOException {
        deleteSnapshot(root.resolve(CONFIGS_DIR).resolve(normalizeSnapshotName(name)));
    }

    public List<String> listNamedConfigs() throws IOException {
        return listSnapshots(root.resolve(CONFIGS_DIR));
    }

    public void saveSnapshot(Path snapshotRoot) throws IOException {
        ensureRuntimeDirectories();
        save();
        clearSnapshotDirectory(snapshotRoot);
        copyManagedState(root, snapshotRoot);
    }

    public void loadSnapshot(Path snapshotRoot) throws IOException {
        ensureRuntimeDirectories();
        snapshotRoot = migrateLegacySnapshotIfNeeded(snapshotRoot);
        if (!Files.isDirectory(snapshotRoot)) {
            throw new IOException("Snapshot not found: " + snapshotRoot.getFileName());
        }
        Path finalSnapshotRoot = snapshotRoot;

        withMutedNotifications(() -> {
            resetRuntimeState();
            clearLiveState();
            copyManagedState(finalSnapshotRoot, root);
            load();
        });
    }

    public void deleteSnapshot(Path snapshotRoot) throws IOException {
        snapshotRoot = migrateLegacySnapshotIfNeeded(snapshotRoot);
        if (!Files.exists(snapshotRoot)) {
            return;
        }
        deleteRecursively(snapshotRoot);
    }

    public List<String> listSnapshots(Path snapshotsRoot) throws IOException {
        Files.createDirectories(snapshotsRoot);
        try (Stream<Path> stream = Files.list(snapshotsRoot)) {
            return stream
                .filter(Files::isDirectory)
                .map(path -> path.getFileName().toString())
                .sorted()
                .toList();
        }
    }

    private void loadGlobal() throws IOException {
        JsonObject object = readObject(root.resolve(GLOBAL_FILE));
        if (object == null) {
            return;
        }
        if (object.has("accent")) {
            KusKusTheme.setAccent(Integer.parseInt(object.get("accent").getAsString().replace("#", ""), 16));
        }
        if (object.has("prefix")) {
            KusKusKlient.COMMANDS.setPrefix(object.get("prefix").getAsString());
        }
        if (object.has("guiX")) {
            KusKusKlient.GUI_X = object.get("guiX").getAsInt();
        }
        if (object.has("guiY")) {
            KusKusKlient.GUI_Y = object.get("guiY").getAsInt();
        }
        if (object.has("guiKeybind")) {
            KusKusKlient.GUI_KEYBIND = KeyUtil.code(object.get("guiKeybind").getAsString());
        }
        if (object.has("guiSearchKeybind")) {
            KusKusKlient.GUI_SEARCH_KEYBIND = KeyUtil.code(object.get("guiSearchKeybind").getAsString());
        }
    }

    private void saveGlobal() throws IOException {
        JsonObject object = baseObject();
        object.addProperty("accent", String.format("#%06X", KusKusTheme.PRIMARY & 0xFFFFFF));
        object.addProperty("prefix", KusKusKlient.COMMANDS.prefix());
        object.addProperty("guiX", KusKusKlient.GUI_X);
        object.addProperty("guiY", KusKusKlient.GUI_Y);
        object.addProperty("guiKeybind", KeyUtil.name(KusKusKlient.GUI_KEYBIND));
        object.addProperty("guiSearchKeybind", KeyUtil.name(KusKusKlient.GUI_SEARCH_KEYBIND));
        write(root.resolve(GLOBAL_FILE), object);
    }

    private void loadModules() throws IOException {
        for (Module module : KusKusKlient.MODULES.all()) {
            JsonObject object = readObject(resolveModulePathForLoad(module));
            if (object == null) {
                continue;
            }
            if (object.has("keybind")) {
                module.keybind().fromJson(object.get("keybind"));
            }
            JsonObject settings = object.has("settings") && object.get("settings").isJsonObject()
                ? object.getAsJsonObject("settings")
                : new JsonObject();
            for (SettingGroup group : module.groups()) {
                for (Setting<?> setting : group.settings()) {
                    if (settings.has(setting.name())) {
                        setting.fromJson(settings.get(setting.name()));
                    }
                }
            }
            if (module instanceof UI ui) {
                ui.applyTheme();
            }
            if (object.has("enabled")) {
                module.setEnabled(object.get("enabled").getAsBoolean());
            }
        }
    }

    private void saveModules() throws IOException {
        for (Module module : KusKusKlient.MODULES.all()) {
            saveModule(module);
        }
    }

    public void saveModule(Module module) {
        try {
            JsonObject object = baseObject();
            object.addProperty("enabled", module.enabled());
            object.add("keybind", module.keybind().toJson());
            JsonObject settings = new JsonObject();
            for (SettingGroup group : module.groups()) {
                for (Setting<?> setting : group.settings()) {
                    settings.add(setting.name(), setting.toJson());
                }
            }
            object.add("settings", settings);
            write(modulePath(module), object);
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to save module config for {}", module.name(), exception);
        }
    }

    private void loadFriends() throws IOException {
        JsonObject object = readObject(root.resolve(FRIENDS_FILE));
        if (object == null || !object.has("friends")) {
            return;
        }
        KusKusKlient.FRIENDS.clear();
        JsonArray friends = object.getAsJsonArray("friends");
        for (JsonElement element : friends) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject friend = element.getAsJsonObject();
            KusKusKlient.FRIENDS.add(friend.get("name").getAsString(), friend.has("note") ? friend.get("note").getAsString() : "");
        }
    }

    private void saveFriends() throws IOException {
        JsonObject object = baseObject();
        JsonArray array = new JsonArray();
        for (Friend friend : KusKusKlient.FRIENDS.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", friend.name());
            entry.addProperty("note", friend.note());
            array.add(entry);
        }
        object.add("friends", array);
        write(root.resolve(FRIENDS_FILE), object);
    }

    private JsonObject readObject(Path path) throws IOException {
        if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            int version = object.has("configVersion") ? object.get("configVersion").getAsInt() : 0;
            if (version < CONFIG_VERSION) {
                Path backup = path.resolveSibling(path.getFileName() + ".backup.json");
                Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
                KusKusKlient.LOGGER.warn("Config {} was migrated from v{} to v{}", path.getFileName(), version, CONFIG_VERSION);
            }
            return object;
        } catch (RuntimeException exception) {
            Path corrupted = path.resolveSibling(path.getFileName() + ".corrupted.json");
            Files.move(path, corrupted, StandardCopyOption.REPLACE_EXISTING);
            KusKusKlient.LOGGER.warn("Corrupted config moved to {}", corrupted, exception);
            return null;
        }
    }

    private JsonObject baseObject() {
        JsonObject object = new JsonObject();
        object.addProperty("configVersion", CONFIG_VERSION);
        return object;
    }

    private Path modulePath(Module module) {
        String fileName = module.getClass().getName()
            .replace('.', '_')
            .replace('$', '_')
            .toLowerCase(Locale.ROOT) + ".json";
        return root.resolve(MODULES_DIR).resolve(fileName);
    }

    private Path legacyModulePath(Module module) {
        String legacyName = module.name().replace(" ", "_").toLowerCase(Locale.ROOT) + ".json";
        return root.resolve(MODULES_DIR).resolve(legacyName);
    }

    private Path resolveModulePathForLoad(Module module) throws IOException {
        Path modern = modulePath(module);
        if (Files.exists(modern)) {
            return modern;
        }

        Path legacy = legacyModulePath(module);
        if (Files.exists(legacy)) {
            Files.createDirectories(modern.getParent());
            Files.move(legacy, modern, StandardCopyOption.REPLACE_EXISTING);
            return modern;
        }
        return modern;
    }

    private void write(Path path, JsonObject object) throws IOException {
        Files.createDirectories(path.getParent());
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp)) {
            gson.toJson(object, writer);
        }
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void ensureRuntimeDirectories() throws IOException {
        Files.createDirectories(root);
        Files.createDirectories(root.resolve(MODULES_DIR));
        Files.createDirectories(root.resolve(PROFILES_DIR));
        Files.createDirectories(root.resolve(CONFIGS_DIR));
        Files.createDirectories(root.resolve(ADDONS_DIR));
        Files.createDirectories(root.resolve(CRASHES_DIR));
    }

    private void resetRuntimeState() {
        KusKusTheme.reset();
        KusKusKlient.COMMANDS.setPrefix(".");
        KusKusKlient.GUI_X = KusKusKlient.DEFAULT_GUI_X;
        KusKusKlient.GUI_Y = KusKusKlient.DEFAULT_GUI_Y;
        KusKusKlient.GUI_KEYBIND = KusKusKlient.DEFAULT_GUI_KEYBIND;
        KusKusKlient.GUI_SEARCH_KEYBIND = KusKusKlient.DEFAULT_GUI_SEARCH_KEYBIND;

        for (Module module : KusKusKlient.MODULES.all()) {
            module.reset();
            if (module instanceof UI ui) {
                ui.applyTheme();
            }
        }

        KusKusKlient.FRIENDS.clear();
        KusKusKlient.MACROS.clear();
        KusKusKlient.HUD.reset();
    }

    private void clearLiveState() throws IOException {
        deleteIfExists(root.resolve(GLOBAL_FILE));
        deleteIfExists(root.resolve(FRIENDS_FILE));
        deleteIfExists(root.resolve(HUD_FILE));
        deleteIfExists(root.resolve(MACROS_FILE));
        deleteRecursively(root.resolve(MODULES_DIR));
        deleteRecursively(root.resolve(ADDONS_DIR));
        Files.createDirectories(root.resolve(MODULES_DIR));
        Files.createDirectories(root.resolve(ADDONS_DIR));
    }

    private void clearSnapshotDirectory(Path snapshotRoot) throws IOException {
        deleteRecursively(snapshotRoot);
        Files.createDirectories(snapshotRoot);
    }

    private void copyManagedState(Path fromRoot, Path toRoot) throws IOException {
        Files.createDirectories(toRoot);
        copyIfExists(fromRoot.resolve(GLOBAL_FILE), toRoot.resolve(GLOBAL_FILE));
        copyIfExists(fromRoot.resolve(FRIENDS_FILE), toRoot.resolve(FRIENDS_FILE));
        copyIfExists(fromRoot.resolve(HUD_FILE), toRoot.resolve(HUD_FILE));
        copyIfExists(fromRoot.resolve(MACROS_FILE), toRoot.resolve(MACROS_FILE));
        copyDirectory(fromRoot.resolve(MODULES_DIR), toRoot.resolve(MODULES_DIR));
        copyDirectory(fromRoot.resolve(ADDONS_DIR), toRoot.resolve(ADDONS_DIR));
    }

    private Path migrateLegacySnapshotIfNeeded(Path snapshotRoot) throws IOException {
        if (Files.exists(snapshotRoot) || !snapshotRoot.getFileName().toString().matches("[a-z0-9._-]+")) {
            return snapshotRoot;
        }

        Path legacyJson = snapshotRoot.resolveSibling(snapshotRoot.getFileName() + ".json");
        if (!Files.exists(legacyJson)) {
            return snapshotRoot;
        }

        Files.createDirectories(snapshotRoot);
        Files.copy(legacyJson, snapshotRoot.resolve(GLOBAL_FILE), StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(legacyJson);
        return snapshotRoot;
    }

    private void copyIfExists(Path source, Path target) throws IOException {
        if (!Files.exists(source) || Files.isDirectory(source)) {
            return;
        }
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.isDirectory(sourceDir)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            for (Path source : stream.toList()) {
                Path relative = sourceDir.relativize(source);
                Path target = targetDir.resolve(relative);
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void deleteIfExists(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(path)) {
            for (Path current : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(current);
            }
        }
    }

    private void withMutedNotifications(IoRunnable runnable) throws IOException {
        try {
            KusKusKlient.NOTIFICATIONS.setMuted(true);
            runnable.run();
        } finally {
            KusKusKlient.NOTIFICATIONS.setMuted(false);
        }
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }
}
