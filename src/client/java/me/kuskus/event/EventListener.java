package me.kuskus.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}
