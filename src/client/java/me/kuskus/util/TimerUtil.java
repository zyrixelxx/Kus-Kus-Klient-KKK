package me.kuskus.util;

public class TimerUtil {
    private long time = System.currentTimeMillis();

    public boolean passed(long delayMs) {
        return System.currentTimeMillis() - time >= delayMs;
    }

    public long elapsed() {
        return System.currentTimeMillis() - time;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }

    public void set(long time) {
        this.time = time;
    }
}
