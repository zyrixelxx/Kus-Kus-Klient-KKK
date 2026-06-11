package me.kuskus.util;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class KeyUtil {
    private static final Map<String, Integer> NAMES = new HashMap<>();
    private static final Map<Integer, String> CODES = new HashMap<>();

    static {
        for (char c = 'A'; c <= 'Z'; c++) {
            add(String.valueOf(c), GLFW.GLFW_KEY_A + (c - 'A'));
        }
        for (char c = '0'; c <= '9'; c++) {
            add(String.valueOf(c), GLFW.GLFW_KEY_0 + (c - '0'));
        }
        for (int i = 1; i <= 12; i++) {
            add("F" + i, GLFW.GLFW_KEY_F1 + i - 1);
        }
        add("NONE", GLFW.GLFW_KEY_UNKNOWN);
        add("LSHIFT", GLFW.GLFW_KEY_LEFT_SHIFT);
        add("RSHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT);
        add("LCTRL", GLFW.GLFW_KEY_LEFT_CONTROL);
        add("RCTRL", GLFW.GLFW_KEY_RIGHT_CONTROL);
        add("LALT", GLFW.GLFW_KEY_LEFT_ALT);
        add("RALT", GLFW.GLFW_KEY_RIGHT_ALT);
        add("SPACE", GLFW.GLFW_KEY_SPACE);
        add("ENTER", GLFW.GLFW_KEY_ENTER);
        add("TAB", GLFW.GLFW_KEY_TAB);
        add("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE);
        add("DELETE", GLFW.GLFW_KEY_DELETE);
        add("ESC", GLFW.GLFW_KEY_ESCAPE);
        add("UP", GLFW.GLFW_KEY_UP);
        add("DOWN", GLFW.GLFW_KEY_DOWN);
        add("LEFT", GLFW.GLFW_KEY_LEFT);
        add("RIGHT", GLFW.GLFW_KEY_RIGHT);
        add("HOME", GLFW.GLFW_KEY_HOME);
        add("END", GLFW.GLFW_KEY_END);
        add("INSERT", GLFW.GLFW_KEY_INSERT);
        add("MOUSE1", GLFW.GLFW_MOUSE_BUTTON_1);
        add("MOUSE2", GLFW.GLFW_MOUSE_BUTTON_2);
        add("MOUSE3", GLFW.GLFW_MOUSE_BUTTON_3);
        add("MOUSE4", GLFW.GLFW_MOUSE_BUTTON_4);
        add("MOUSE5", GLFW.GLFW_MOUSE_BUTTON_5);
    }

    private KeyUtil() {
    }

    private static void add(String name, int code) {
        NAMES.put(name, code);
        CODES.put(code, name);
    }

    public static int code(String name) {
        if (name == null) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        return NAMES.getOrDefault(name.toUpperCase(Locale.ROOT), GLFW.GLFW_KEY_UNKNOWN);
    }

    public static String name(int code) {
        return CODES.getOrDefault(code, "NONE");
    }
}
