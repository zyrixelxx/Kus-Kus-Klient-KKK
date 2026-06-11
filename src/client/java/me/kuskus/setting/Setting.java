package me.kuskus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private T defaultValue;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public void reset() {
        set(defaultValue);
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public JsonElement toJson() {
        T current = get();
        return current == null ? new JsonPrimitive("") : new JsonPrimitive(String.valueOf(current));
    }

    public abstract void fromJson(JsonElement element);
}
