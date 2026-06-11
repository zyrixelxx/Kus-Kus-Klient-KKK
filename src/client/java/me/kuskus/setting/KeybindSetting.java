package me.kuskus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import me.kuskus.util.KeyUtil;

public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, String description, int defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(KeyUtil.name(get()));
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            set(KeyUtil.code(element.getAsString()));
        }
    }
}
