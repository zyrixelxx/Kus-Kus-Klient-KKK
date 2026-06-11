package me.kuskus.gui;

import me.kuskus.KusKusKlient;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.module.modules.client.UI;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.ColorSetting;
import me.kuskus.setting.DoubleSetting;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.IntSetting;
import me.kuskus.setting.Setting;
import me.kuskus.setting.SettingGroup;
import me.kuskus.setting.StringSetting;
import me.kuskus.util.ColorUtil;
import me.kuskus.util.KeyUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class KusKusScreen extends Screen {
    private static final Category[] CATEGORY_ORDER = {
        Category.COMBAT,
        Category.PLAYER,
        Category.MOVEMENT,
        Category.RENDER,
        Category.WORLD,
        Category.MISC,
        Category.CLIENT
    };

    private static final int PANEL_W = 132;
    private static final int PANEL_H = 18;
    private static final int PANEL_GAP = 136;
    private static final int GRID = 6;
    private static final int MODULE_H = 15;
    private static final int SETTING_H = 14;
    private static final int GROUP_H = 10;
    private static final int SEARCH_W = 220;
    private static final int SEARCH_H = 30;
    private static final int VALUE_W = 50;
    private static final int COLOR_W = 72;
    private static final int STRING_W = 92;

    private static final Map<Category, PanelState> PANELS = new EnumMap<>(Category.class);

    private final List<ModuleHitbox> moduleHitboxes = new ArrayList<>();
    private final List<SettingHitbox> settingHitboxes = new ArrayList<>();
    private Module expanded;
    private Module hoveredModule;
    private Setting<?> hoveredSetting;
    private Module pendingSliderSave;
    private PanelState dragging;
    private String searchQuery = "";
    private boolean searchOpen;
    private boolean searchFocused;
    private int dragX;
    private int dragY;
    private boolean listeningKeybind;
    private EditingState editing;

    public KusKusScreen() {
        super(Text.literal("Kus Kus Klient"));
        initPanels();
    }

    public KusKusScreen(Module selected) {
        this();
        expanded = selected;
    }

    @Override
    protected void init() {
        ensurePanelLayout();
        relayoutPanels();
        super.init();
    }

    private static void initPanels() {
        if (!PANELS.isEmpty()) {
            return;
        }

        int x = KusKusKlient.GUI_X - PANEL_GAP;
        for (Category category : CATEGORY_ORDER) {
            PANELS.put(category, new PanelState(x += PANEL_GAP, KusKusKlient.GUI_Y, true));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x52000000);
        drawGrid(context);
        moduleHitboxes.clear();
        settingHitboxes.clear();
        hoveredModule = null;
        hoveredSetting = null;

        ensurePanelLayout();
        renderBrand(context);
        for (Category category : CATEGORY_ORDER) {
            drawPanel(context, category, mouseX, mouseY);
        }
        drawSearchPopup(context);
        renderTooltip(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext context, Category category, int mouseX, int mouseY) {
        PanelState panel = PANELS.get(category);
        if (panel == null) {
            return;
        }

        if (panel.drag) {
            panel.x = snap(dragX + mouseX);
            panel.y = snap(dragY + mouseY);
        }

        List<Module> modules = new ArrayList<>(KusKusKlient.MODULES.byCategory(category));
        if (searchOpen && !searchQuery.isBlank()) {
            modules.removeIf(module -> !moduleMatches(module));
        }

        float totalItemHeight = panel.open ? totalItemHeight(modules) : 0.0f;
        clampPanel(panel, (int) (PANEL_H + totalItemHeight));
        boolean headerHover = hovering(mouseX, mouseY, panel.x, panel.y, PANEL_W, PANEL_H);

        RenderUtil.softShadow(context, panel.x, panel.y, PANEL_W, (int) (PANEL_H + totalItemHeight), 0x33000000);
        RenderUtil.rect(context, panel.x, panel.y, PANEL_W, PANEL_H, headerHover ? KusKusTheme.panelHeaderHover() : KusKusTheme.panelHeader());
        RenderUtil.lineH(context, panel.x, panel.y + PANEL_H - 1, PANEL_W, KusKusTheme.outline());
        RenderUtil.text(context, textRenderer, title(category), panel.x + 4, panel.y + 5, KusKusTheme.TEXT_MAIN);
        RenderUtil.text(context, textRenderer, panel.open ? "-" : "+", panel.x + PANEL_W - 8, panel.y + 5, KusKusTheme.TEXT_MAIN);

        if (!panel.open) {
            return;
        }

        RenderUtil.rect(context, panel.x, panel.y + PANEL_H, PANEL_W, (int) totalItemHeight, ColorUtil.withAlpha(KusKusTheme.BG_PANEL, 0xDA));
        RenderUtil.outline(context, panel.x, panel.y, PANEL_W, (int) (PANEL_H + totalItemHeight), KusKusTheme.outline());

        float itemY = panel.y + PANEL_H + 2.0f;
        for (Module module : modules) {
            itemY = drawModuleButton(context, module, panel.x + 2.0f, itemY, PANEL_W - 4, mouseX, mouseY);
        }
    }

    private float drawModuleButton(DrawContext context, Module module, float x, float y, int itemW, int mouseX, int mouseY) {
        boolean hover = hovering(mouseX, mouseY, x, y, itemW, MODULE_H);
        boolean searchMatch = searchOpen && !searchQuery.isBlank() && moduleMatches(module);
        if (hover) {
            hoveredModule = module;
        }

        int buttonColor = KusKusTheme.button(module.enabled(), hover);
        if (searchMatch && !module.enabled()) {
            buttonColor = ColorUtil.blend(buttonColor, KusKusTheme.PRIMARY, 0.15f);
        }

        RenderUtil.rect(context, (int) x, (int) y, itemW, MODULE_H, buttonColor);
        if (module.enabled() || searchMatch) {
            RenderUtil.rect(context, (int) x, (int) y, 2, MODULE_H, searchMatch ? KusKusTheme.LIGHT : KusKusTheme.PRIMARY);
        }

        String bind = "[" + KeyUtil.name(module.keybind().get()) + "]";
        int bindWidth = Math.min(textRenderer.getWidth(bind), 38);
        String bindText = fit(bind, bindWidth);
        String name = fit(module.name(), Math.max(18, itemW - bindWidth - 12));
        RenderUtil.text(context, textRenderer, name, (int) x + 5, (int) y + 4, module.enabled() ? KusKusTheme.TEXT_MAIN : KusKusTheme.TEXT_GRAY);
        RenderUtil.text(context, textRenderer, bindText, (int) x + itemW - textRenderer.getWidth(bindText) - 4, (int) y + 4, KusKusTheme.TEXT_DIM);
        moduleHitboxes.add(new ModuleHitbox(module, x, y, itemW, MODULE_H));
        y += MODULE_H + 2.0f;

        if (module == expanded) {
            y = drawSettings(context, module, x + 1.0f, y, itemW - 2, mouseX, mouseY);
        }

        return y;
    }

    private float drawSettings(DrawContext context, Module module, float x, float y, int itemW, int mouseX, int mouseY) {
        boolean keyHover = hovering(mouseX, mouseY, x, y, itemW, SETTING_H);
        RenderUtil.rect(context, (int) x, (int) y, itemW, SETTING_H, KusKusTheme.setting(keyHover));
        String bind = listeningKeybind ? "Press key..." : "Bind " + KeyUtil.name(module.keybind().get());
        RenderUtil.text(context, textRenderer, fit(bind, itemW - 6), (int) x + 3, (int) y + 4, KusKusTheme.TEXT_GRAY);
        settingHitboxes.add(new SettingHitbox(module, null, x, y, itemW, SETTING_H, true, false, 0, 0, 0, 0));
        y += SETTING_H + 2.0f;

        for (SettingGroup group : module.groups()) {
            if (!group.settings().isEmpty()) {
                RenderUtil.text(context, textRenderer, group.name(), (int) x + 2, (int) y + 1, KusKusTheme.TEXT_DIM);
                y += GROUP_H;
            }
            for (Setting<?> setting : group.settings()) {
                y = drawSetting(context, module, setting, x, y, itemW, mouseX, mouseY);
            }
        }

        return y + 1.0f;
    }

    private float drawSetting(DrawContext context, Module module, Setting<?> setting, float x, float y, int itemW, int mouseX, int mouseY) {
        boolean hover = hovering(mouseX, mouseY, x, y, itemW, SETTING_H);
        if (hover) {
            hoveredSetting = setting;
        }

        boolean editingThis = editing != null && editing.setting == setting;
        int bg = KusKusTheme.setting(hover);

        if (setting instanceof BoolSetting bool) {
            RenderUtil.rect(context, (int) x, (int) y, itemW, SETTING_H, bool.get() ? KusKusTheme.button(true, hover) : bg);
            RenderUtil.text(context, textRenderer, fit(setting.name(), itemW - 6), (int) x + 3, (int) y + 4, bool.get() ? KusKusTheme.TEXT_MAIN : KusKusTheme.TEXT_GRAY);
            settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, false, 0, 0, 0, 0));
            return y + SETTING_H + 2.0f;
        }

        if (setting instanceof IntSetting integer) {
            int fieldX = (int) x + itemW - VALUE_W;
            int sliderW = itemW - VALUE_W - 4;
            drawNumericSetting(context, setting.name(), integer.get(), integer.min(), integer.max(), x, y, sliderW, fieldX, VALUE_W, hover, editingThis);
            settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, true, fieldX, y, VALUE_W, SETTING_H));
            return y + SETTING_H + 2.0f;
        }

        if (setting instanceof DoubleSetting decimal) {
            int fieldX = (int) x + itemW - VALUE_W;
            int sliderW = itemW - VALUE_W - 4;
            drawNumericSetting(context, setting.name(), decimal.get(), decimal.min(), decimal.max(), x, y, sliderW, fieldX, VALUE_W, hover, editingThis);
            settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, true, fieldX, y, VALUE_W, SETTING_H));
            return y + SETTING_H + 2.0f;
        }

        if (setting instanceof ColorSetting color) {
            int fieldX = (int) x + itemW - COLOR_W;
            RenderUtil.rect(context, (int) x, (int) y, itemW, SETTING_H, bg);
            RenderUtil.text(context, textRenderer, fit(setting.name(), itemW - COLOR_W - 8), (int) x + 3, (int) y + 4, KusKusTheme.TEXT_GRAY);
            drawValueBox(context, fieldX, (int) y, COLOR_W, SETTING_H, hover || editingThis);
            RenderUtil.rect(context, fieldX + 3, (int) y + 3, 8, 8, 0xFF000000 | (color.get() & 0xFFFFFF));
            String value = editingThis ? editing.buffer + "_" : color.hex();
            RenderUtil.text(context, textRenderer, fit(value, COLOR_W - 16), fieldX + 13, (int) y + 4, KusKusTheme.TEXT_MAIN);
            settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, true, fieldX, y, COLOR_W, SETTING_H));
            return y + SETTING_H + 2.0f;
        }

        if (setting instanceof StringSetting string) {
            int fieldX = (int) x + itemW - STRING_W;
            RenderUtil.rect(context, (int) x, (int) y, itemW, SETTING_H, bg);
            RenderUtil.text(context, textRenderer, fit(setting.name(), itemW - STRING_W - 8), (int) x + 3, (int) y + 4, KusKusTheme.TEXT_GRAY);
            drawValueBox(context, fieldX, (int) y, STRING_W, SETTING_H, hover || editingThis);
            String value = editingThis ? editing.buffer + "_" : string.get();
            RenderUtil.text(context, textRenderer, fit(value, STRING_W - 6), fieldX + 3, (int) y + 4, KusKusTheme.TEXT_MAIN);
            settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, true, fieldX, y, STRING_W, SETTING_H));
            return y + SETTING_H + 2.0f;
        }

        if (setting instanceof EnumSetting<?> enumSetting) {
            RenderUtil.rect(context, (int) x, (int) y, itemW, SETTING_H, bg);
            String value = String.valueOf(enumSetting.get());
            RenderUtil.text(context, textRenderer, fit(setting.name(), itemW - VALUE_W - 8), (int) x + 3, (int) y + 4, KusKusTheme.TEXT_GRAY);
            drawValueBox(context, (int) x + itemW - VALUE_W, (int) y, VALUE_W, SETTING_H, hover);
            RenderUtil.text(context, textRenderer, fit(value, VALUE_W - 6), (int) x + itemW - VALUE_W + 3, (int) y + 4, KusKusTheme.TEXT_MAIN);
            settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, false, 0, 0, 0, 0));
            return y + SETTING_H + 2.0f;
        }

        RenderUtil.rect(context, (int) x, (int) y, itemW, SETTING_H, KusKusTheme.button(true, hover));
        RenderUtil.text(context, textRenderer, fit(setting.name() + " " + valueText(setting), itemW - 6), (int) x + 3, (int) y + 4, KusKusTheme.TEXT_MAIN);
        settingHitboxes.add(new SettingHitbox(module, setting, x, y, itemW, SETTING_H, false, false, 0, 0, 0, 0));
        return y + SETTING_H + 2.0f;
    }

    private void drawNumericSetting(DrawContext context, String name, double value, double min, double max, float x, float y, int sliderW, int fieldX, int fieldW, boolean hover, boolean editingThis) {
        RenderUtil.rect(context, (int) x, (int) y, sliderW, SETTING_H, KusKusTheme.setting(hover));
        double progress = max <= min ? 0.0 : (value - min) / (max - min);
        int fillW = (int) Math.round(sliderW * Math.max(0.0, Math.min(1.0, progress)));
        RenderUtil.rect(context, (int) x, (int) y, fillW, SETTING_H, KusKusTheme.button(true, hover));
        RenderUtil.text(context, textRenderer, fit(name, sliderW - 6), (int) x + 3, (int) y + 4, KusKusTheme.TEXT_MAIN);
        drawValueBox(context, fieldX, (int) y, fieldW, SETTING_H, hover || editingThis);
        String valueText = editingThis ? editing.buffer + "_" : formatNumber(value);
        RenderUtil.text(context, textRenderer, fit(valueText, fieldW - 6), fieldX + 3, (int) y + 4, KusKusTheme.TEXT_MAIN);
    }

    private void drawValueBox(DrawContext context, int x, int y, int w, int h, boolean active) {
        RenderUtil.rect(context, x, y, w, h, active ? ColorUtil.withAlpha(KusKusTheme.LIGHT, 0x30) : ColorUtil.withAlpha(KusKusTheme.BG_DARK, 0xC0));
        RenderUtil.outline(context, x, y, w, h, active ? KusKusTheme.PRIMARY : KusKusTheme.outline());
    }

    private void drawSearchPopup(DrawContext context) {
        if (!searchOpen) {
            return;
        }

        int x = 4;
        int y = height - SEARCH_H - 4;
        RenderUtil.softShadow(context, x, y, SEARCH_W, SEARCH_H, 0x33000000);
        RenderUtil.rect(context, x, y, SEARCH_W, SEARCH_H, ColorUtil.withAlpha(KusKusTheme.BG_PANEL, 0xF0));
        RenderUtil.outline(context, x, y, SEARCH_W, SEARCH_H, searchFocused ? KusKusTheme.PRIMARY : KusKusTheme.outline());
        String label = searchQuery.isBlank() ? "Search modules..." : searchQuery;
        RenderUtil.text(context, textRenderer, fit(label, SEARCH_W - 12), x + 6, y + 10, searchQuery.isBlank() ? KusKusTheme.TEXT_DIM : KusKusTheme.TEXT_MAIN);
    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        String line1 = null;
        String line2 = null;

        if (hoveredSetting != null && hoveredSetting.description() != null && !hoveredSetting.description().isBlank()) {
            line1 = hoveredSetting.name();
            line2 = hoveredSetting.description();
        } else if (hoveredModule != null && hoveredModule.description() != null && !hoveredModule.description().isBlank()) {
            line1 = hoveredModule.name();
            line2 = hoveredModule.description();
        }

        if (line1 == null) {
            return;
        }

        line1 = fit(line1, 180);
        line2 = fit(line2, 220);
        int boxW = Math.max(textRenderer.getWidth(line1), textRenderer.getWidth(line2)) + 10;
        int x = Math.min(width - boxW - 4, mouseX + 12);
        int y = Math.min(height - 26 - 4, mouseY + 12);

        RenderUtil.softShadow(context, x, y, boxW, 26, 0x33000000);
        RenderUtil.rect(context, x, y, boxW, 26, ColorUtil.withAlpha(KusKusTheme.BG_DARK, 0xF0));
        RenderUtil.lineH(context, x, y, boxW, KusKusTheme.PRIMARY);
        RenderUtil.text(context, textRenderer, line1, x + 5, y + 5, KusKusTheme.TEXT_MAIN);
        RenderUtil.text(context, textRenderer, line2, x + 5, y + 15, KusKusTheme.TEXT_GRAY);
    }

    private float totalItemHeight(List<Module> modules) {
        float total = 0.0f;
        for (Module module : modules) {
            total += MODULE_H + 2.0f;
            if (module == expanded) {
                total += SETTING_H + 2.0f;
                for (SettingGroup group : module.groups()) {
                    if (!group.settings().isEmpty()) {
                        total += GROUP_H;
                    }
                    total += group.settings().size() * (SETTING_H + 2.0f);
                }
                total += 1.0f;
            }
        }
        return total;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int button = click.button();

        finishEditing(false);

        for (Category category : CATEGORY_ORDER) {
            PanelState panel = PANELS.get(category);
            if (panel != null && hovering(mouseX, mouseY, panel.x, panel.y, PANEL_W, PANEL_H)) {
                if (button == 0) {
                    for (PanelState other : PANELS.values()) {
                        other.drag = false;
                    }
                    panel.drag = true;
                    dragging = panel;
                    dragX = panel.x - mouseX;
                    dragY = panel.y - mouseY;
                    return true;
                }
                if (button == 1) {
                    panel.open = !panel.open;
                    savePanelAnchor();
                    return true;
                }
            }
        }

        if (searchOpen) {
            int sx = 4;
            int sy = height - SEARCH_H - 4;
            if (hovering(mouseX, mouseY, sx, sy, SEARCH_W, SEARCH_H)) {
                searchFocused = true;
                return true;
            }
            if (button == 0 && !hovering(mouseX, mouseY, sx, sy, SEARCH_W, SEARCH_H)) {
                searchFocused = false;
            }
        } else if (button == 0) {
            searchFocused = false;
        }

        for (SettingHitbox hitbox : settingHitboxes) {
            if (!hitbox.contains(mouseX, mouseY)) {
                continue;
            }

            if (hitbox.keybind) {
                listeningKeybind = true;
                return true;
            }

            if (hitbox.hasInput && hitbox.containsInput(mouseX, mouseY)) {
                startEditing(hitbox.module, hitbox.setting);
                return true;
            }

            editSetting(hitbox.setting, button, mouseX, hitbox);
            onModuleSettingsChanged(hitbox.module);
            return true;
        }

        for (ModuleHitbox hitbox : moduleHitboxes) {
            if (hitbox.contains(mouseX, mouseY)) {
                if (button == 0) {
                    hitbox.module.toggle();
                    KusKusKlient.CONFIG.saveModule(hitbox.module);
                    return true;
                }
                if (button == 1) {
                    expanded = expanded == hitbox.module ? null : hitbox.module;
                    listeningKeybind = false;
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void editSetting(Setting<?> setting, int button, double mouseX, SettingHitbox hitbox) {
        if (setting instanceof BoolSetting bool) {
            bool.set(!bool.get());
        } else if (setting instanceof EnumSetting enumSetting) {
            if (button == 1) {
                enumSetting.previous();
            } else {
                enumSetting.next();
            }
        } else if (setting instanceof IntSetting integer) {
            if (button == 1) {
                integer.set(integer.get() - 1);
            } else {
                double progress = (mouseX - hitbox.x) / Math.max(1.0, hitbox.inputX > 0 ? hitbox.inputX - hitbox.x - 2.0f : hitbox.w);
                integer.set(integer.min() + (int) (Math.max(0.0, Math.min(1.0, progress)) * (integer.max() - integer.min())));
            }
        } else if (setting instanceof DoubleSetting decimal) {
            if (button == 1) {
                decimal.set(decimal.get() - 0.1);
            } else {
                double progress = (mouseX - hitbox.x) / Math.max(1.0, hitbox.inputX > 0 ? hitbox.inputX - hitbox.x - 2.0f : hitbox.w);
                decimal.set(Math.round((decimal.min() + Math.max(0.0, Math.min(1.0, progress)) * (decimal.max() - decimal.min())) * 10.0) / 10.0);
            }
        } else if (setting instanceof StringSetting || setting instanceof ColorSetting) {
            startEditing(hitbox.module, setting);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging != null && dragging.drag) {
            dragging.x = dragX + (int) click.x();
            dragging.y = dragY + (int) click.y();
            return true;
        }

        for (SettingHitbox hitbox : settingHitboxes) {
            boolean slider = !hitbox.keybind && (hitbox.setting instanceof IntSetting || hitbox.setting instanceof DoubleSetting);
            if (slider && hitbox.contains(click.x(), click.y()) && !hitbox.containsInput(click.x(), click.y())) {
                editSetting(hitbox.setting, 0, click.x(), hitbox);
                pendingSliderSave = hitbox.module;
                return true;
            }
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            for (PanelState panel : PANELS.values()) {
                snapPanel(panel);
                panel.drag = false;
            }
            dragging = null;
            if (pendingSliderSave != null) {
                onModuleSettingsChanged(pendingSliderSave);
                pendingSliderSave = null;
            }
            savePanelAnchor();
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (editing != null) {
            char typed = input.asString().isEmpty() ? '\0' : input.asString().charAt(0);
            if (acceptsChar(editing.setting, typed)) {
                editing.buffer += input.asString();
                return true;
            }
            return false;
        }

        if (searchOpen && searchFocused && input.isValidChar()) {
            searchQuery += input.asString();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (editing != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                finishEditing(false);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                finishEditing(true);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !editing.buffer.isEmpty()) {
                editing.buffer = editing.buffer.substring(0, editing.buffer.length() - 1);
                return true;
            }
            return true;
        }

        if ((input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == KusKusKlient.GUI_SEARCH_KEYBIND) {
            searchOpen = !searchOpen;
            searchFocused = searchOpen;
            return true;
        }
        if (searchOpen && searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                searchOpen = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            }
        }
        if (listeningKeybind && expanded != null) {
            expanded.keybind().set(keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            KusKusKlient.CONFIG.saveModule(expanded);
            listeningKeybind = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        finishEditing(true);
        savePanelAnchor();
        KusKusKlient.CONFIG.save();
        super.close();
    }

    private void startEditing(Module module, Setting<?> setting) {
        listeningKeybind = false;
        editing = new EditingState(module, setting, editableValue(setting));
    }

    private void finishEditing(boolean apply) {
        if (editing == null) {
            return;
        }

        if (apply) {
            applyEditedValue(editing.module, editing.setting, editing.buffer);
        }
        editing = null;
    }

    private void applyEditedValue(Module module, Setting<?> setting, String buffer) {
        try {
            if (setting instanceof StringSetting string) {
                string.set(buffer);
            } else if (setting instanceof IntSetting integer) {
                integer.set(Integer.parseInt(buffer.trim()));
            } else if (setting instanceof DoubleSetting decimal) {
                decimal.set(Double.parseDouble(buffer.trim()));
            } else if (setting instanceof ColorSetting color) {
                String raw = buffer.trim().replace("#", "");
                if (!raw.isEmpty()) {
                    color.set(Integer.parseInt(raw, 16));
                }
            }
            onModuleSettingsChanged(module);
        } catch (NumberFormatException ignored) {
        }
    }

    private void onModuleSettingsChanged(Module module) {
        if (module instanceof UI ui) {
            ui.applyTheme();
        }
        KusKusKlient.CONFIG.saveModule(module);
    }

    private String editableValue(Setting<?> setting) {
        if (setting instanceof StringSetting string) {
            return string.get();
        }
        if (setting instanceof IntSetting integer) {
            return String.valueOf(integer.get());
        }
        if (setting instanceof DoubleSetting decimal) {
            return formatNumber(decimal.get());
        }
        if (setting instanceof ColorSetting color) {
            return color.hex();
        }
        return String.valueOf(setting.get());
    }

    private boolean acceptsChar(Setting<?> setting, char c) {
        if (!Character.isDefined(c) || Character.isISOControl(c)) {
            return false;
        }
        if (setting instanceof StringSetting) {
            return true;
        }
        if (setting instanceof IntSetting) {
            return Character.isDigit(c) || c == '-';
        }
        if (setting instanceof DoubleSetting) {
            return Character.isDigit(c) || c == '-' || c == '.';
        }
        if (setting instanceof ColorSetting) {
            return Character.digit(c, 16) != -1 || c == '#';
        }
        return false;
    }

    private void savePanelAnchor() {
        PanelState first = PANELS.get(CATEGORY_ORDER[0]);
        if (first != null) {
            KusKusKlient.GUI_X = first.x - 3;
            KusKusKlient.GUI_Y = first.y;
        }
    }

    private void ensurePanelLayout() {
        if (width <= 0 || height <= 0 || PANELS.size() != CATEGORY_ORDER.length) {
            return;
        }

        boolean invalid = false;
        for (Category category : CATEGORY_ORDER) {
            PanelState panel = PANELS.get(category);
            if (panel == null) {
                invalid = true;
                break;
            }
            if (panel.x < 0 || panel.x + PANEL_W > width || panel.y < 0 || panel.y + PANEL_H > height) {
                invalid = true;
                break;
            }
        }

        if (!invalid && KusKusKlient.GUI_X != 32) {
            return;
        }

        int totalWidth = totalPanelWidth();
        int startX = Math.max(6, (width - totalWidth) / 2);
        int startY = snap(12);
        int x = startX;
        for (Category category : CATEGORY_ORDER) {
            PANELS.put(category, new PanelState(x, startY, true));
            x += PANEL_GAP;
        }
        KusKusKlient.GUI_X = startX + 3;
        KusKusKlient.GUI_Y = startY;
    }

    private void relayoutPanels() {
        int startX = Math.max(6, (width - totalPanelWidth()) / 2);
        int targetY = 12;
        int x = startX;
        for (Category category : CATEGORY_ORDER) {
            PanelState panel = PANELS.get(category);
            if (panel != null) {
                panel.x = x;
                panel.y = targetY;
                panel.open = true;
            }
            x += PANEL_GAP;
        }
        KusKusKlient.GUI_X = startX + 3;
        KusKusKlient.GUI_Y = targetY;
    }

    private String title(Category category) {
        return switch (category) {
            case WORLD -> "Exploit";
            case MISC -> "Misc";
            case CLIENT -> "Client";
            default -> {
                String lower = category.name().toLowerCase();
                yield Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            }
        };
    }

    private String valueText(Setting<?> setting) {
        if (setting instanceof ColorSetting color) {
            return color.hex();
        }
        return String.valueOf(setting.get());
    }

    private boolean moduleMatches(Module module) {
        if (searchQuery.isBlank()) {
            return false;
        }
        String needle = searchQuery.toLowerCase();
        return module.name().toLowerCase().contains(needle)
            || module.description().toLowerCase().contains(needle)
            || module.category().name().toLowerCase().contains(needle);
    }

    private void snapPanel(PanelState panel) {
        panel.x = snap(panel.x);
        panel.y = snap(panel.y);

        for (PanelState other : PANELS.values()) {
            if (other == panel) {
                continue;
            }

            if (Math.abs(panel.x - (other.x + PANEL_W + 3)) <= GRID) {
                panel.x = other.x + PANEL_W + 3;
            } else if (Math.abs(other.x - (panel.x + PANEL_W + 3)) <= GRID) {
                panel.x = other.x - PANEL_W - 3;
            }

            if (Math.abs(panel.y - other.y) <= GRID) {
                panel.y = other.y;
            }
        }
        clampPanel(panel, PANEL_H);
    }

    private void clampPanel(PanelState panel, int panelHeight) {
        panel.x = Math.max(4, Math.min(width - PANEL_W - 4, panel.x));
        panel.y = Math.max(20, Math.min(height - panelHeight - 4, panel.y));
    }

    private int totalPanelWidth() {
        return CATEGORY_ORDER.length * PANEL_W + (CATEGORY_ORDER.length - 1) * (PANEL_GAP - PANEL_W);
    }

    private int snap(int value) {
        return Math.round(value / (float) GRID) * GRID;
    }

    private String fit(String text, float maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String dots = "..";
        int limit = Math.max(1, (int) maxWidth - textRenderer.getWidth(dots));
        String result = text;
        while (!result.isEmpty() && textRenderer.getWidth(result) > limit) {
            result = result.substring(0, result.length() - 1);
        }
        return result + dots;
    }

    private String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    private boolean hovering(double mouseX, double mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY < y + h;
    }

    private void renderBrand(DrawContext context) {
        String brand = "Kus Kus Klient " + KusKusKlient.VERSION;
        RenderUtil.text(context, textRenderer, brand, width - textRenderer.getWidth(brand) - 6, 6, KusKusTheme.TEXT_MAIN);
    }

    private void drawGrid(DrawContext context) {
        int minor = 0x08000000;
        int major = 0x12000000;
        for (int x = 0; x < width; x += GRID) {
            RenderUtil.lineV(context, x, 0, height, x % (GRID * 4) == 0 ? major : minor);
        }
        for (int y = 0; y < height; y += GRID) {
            RenderUtil.lineH(context, 0, y, width, y % (GRID * 4) == 0 ? major : minor);
        }
    }

    private static final class PanelState {
        private int x;
        private int y;
        private boolean open;
        private boolean drag;

        private PanelState(int x, int y, boolean open) {
            this.x = x;
            this.y = y;
            this.open = open;
        }
    }

    private static final class ModuleHitbox {
        private final Module module;
        private final float x;
        private final float y;
        private final float w;
        private final float h;

        private ModuleHitbox(Module module, float x, float y, float w, float h) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY < y + h;
        }
    }

    private static final class SettingHitbox {
        private final Module module;
        private final Setting<?> setting;
        private final float x;
        private final float y;
        private final float w;
        private final float h;
        private final boolean keybind;
        private final boolean hasInput;
        private final float inputX;
        private final float inputY;
        private final float inputW;
        private final float inputH;

        private SettingHitbox(Module module, Setting<?> setting, float x, float y, float w, float h, boolean keybind, boolean hasInput, float inputX, float inputY, float inputW, float inputH) {
            this.module = module;
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.keybind = keybind;
            this.hasInput = hasInput;
            this.inputX = inputX;
            this.inputY = inputY;
            this.inputW = inputW;
            this.inputH = inputH;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY < y + h;
        }

        private boolean containsInput(double mouseX, double mouseY) {
            return hasInput && mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY < inputY + inputH;
        }
    }

    private static final class EditingState {
        private final Module module;
        private final Setting<?> setting;
        private String buffer;

        private EditingState(Module module, Setting<?> setting, String buffer) {
            this.module = module;
            this.setting = setting;
            this.buffer = buffer;
        }
    }
}
