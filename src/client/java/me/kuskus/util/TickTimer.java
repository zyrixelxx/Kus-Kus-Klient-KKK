package me.kuskus.util;

public class TickTimer {
    private int ticks;

    public void reset() {
        ticks = 0;
    }

    public void tick() {
        ticks++;
    }

    public int ticks() {
        return ticks;
    }

    public boolean passed(int amount) {
        return ticks >= amount;
    }
}
