package me.kuskus.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kuskus.KusKusKlient;
import me.kuskus.gui.KusKusScreen;
import me.kuskus.hud.elements.*;
import me.kuskus.setting.Setting;
import me.kuskus.setting.SettingGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HudManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final CopyOnWriteArrayList<HudElement> elements = new CopyOnWriteArrayList<>();

    public void init() {
        elements.add(new ArrayListHud());
        elements.add(new CoordinatesHud());
        elements.add(new FpsHud());
        elements.add(new PingHud());
        elements.add(new ArmorHud());
        elements.add(new TotemHud());
        elements.add(new WatermarkHud());
    }

    public List<HudElement> all() {
        return List.copyOf(elements);
    }

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        if (client.currentScreen instanceof HudEditorScreen || client.currentScreen instanceof KusKusScreen) {
            return;
        }
        for (HudElement element : elements) {
            element.renderIfVisible(context, client);
        }
    }

    public void load() {
        Path path = KusKusKlient.CONFIG.root().resolve("hud.json");
        if (!Files.exists(path)) {
            return;
        }
        try {
            JsonObject object = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            for (HudElement element : elements) {
                if (!object.has(element.name())) {
                    continue;
                }
                JsonObject item = object.getAsJsonObject(element.name());
                element.moveTo(item.get("x").getAsInt(), item.get("y").getAsInt());
                element.setVisible(item.get("visible").getAsBoolean());
                JsonObject settings = item.has("settings") && item.get("settings").isJsonObject()
                    ? item.getAsJsonObject("settings") : new JsonObject();
                for (SettingGroup group : element.groups()) {
                    for (Setting<?> setting : group.settings()) {
                        if (settings.has(setting.name())) {
                            setting.fromJson(settings.get(setting.name()));
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            KusKusKlient.LOGGER.warn("Failed to load HUD config", exception);
        }
    }

    public void save() {
        Path path = KusKusKlient.CONFIG.root().resolve("hud.json");
        JsonObject object = new JsonObject();
        for (HudElement element : elements) {
            JsonObject item = new JsonObject();
            item.addProperty("x", element.x());
            item.addProperty("y", element.y());
            item.addProperty("visible", element.visible());
            JsonObject settings = new JsonObject();
            for (SettingGroup group : element.groups()) {
                for (Setting<?> setting : group.settings()) {
                    settings.add(setting.name(), setting.toJson());
                }
            }
            item.add("settings", settings);
            object.add(element.name(), item);
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(object));
        } catch (IOException exception) {
            KusKusKlient.LOGGER.warn("Failed to save HUD config", exception);
        }
    }

    public void reset() {
        for (HudElement element : elements) {
            element.reset();
        }
    }
}
