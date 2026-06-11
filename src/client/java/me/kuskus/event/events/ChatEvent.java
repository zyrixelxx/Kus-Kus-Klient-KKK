package me.kuskus.event.events;

import me.kuskus.event.Event;

public class ChatEvent extends Event {
    private final String message;

    public ChatEvent(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
