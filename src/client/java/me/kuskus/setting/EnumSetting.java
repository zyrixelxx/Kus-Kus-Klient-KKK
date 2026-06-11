package me.kuskus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> type;

    public EnumSetting(String name, String description, E defaultValue, Class<E> type) {
        super(name, description, defaultValue);
        this.type = type;
    }

    public E next() {
        E[] values = type.getEnumConstants();
        int index = (get().ordinal() + 1) % values.length;
        set(values[index]);
        return get();
    }

    public E previous() {
        E[] values = type.getEnumConstants();
        int index = get().ordinal() - 1;
        if (index < 0) {
            index = values.length - 1;
        }
        set(values[index]);
        return get();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get().name());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return;
        }
        try {
            set(Enum.valueOf(type, element.getAsString()));
        } catch (IllegalArgumentException ignored) {
            set(defaultValue());
        }
    }
}
