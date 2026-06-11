package me.kuskus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, String description, int defaultValue) {
        super(name, description, defaultValue);
    }

    public String hex() {
        return String.format("#%06X", get() & 0xFFFFFF);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(hex());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return;
        }
        String text = element.getAsString().replace("#", "");
        try {
            set(Integer.parseInt(text, 16));
        } catch (NumberFormatException ignored) {
            set(defaultValue());
        }
    }
}
