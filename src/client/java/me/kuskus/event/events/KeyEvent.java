package me.kuskus.event.events;

import me.kuskus.event.Event;

public class KeyEvent extends Event {
    private final long window;
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    public KeyEvent(long window, int key, int scancode, int action, int modifiers) {
        this.window = window;
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }

    public long window() {
        return window;
    }

    public int key() {
        return key;
    }

    public int scancode() {
        return scancode;
    }

    public int action() {
        return action;
    }

    public int modifiers() {
        return modifiers;
    }
}
