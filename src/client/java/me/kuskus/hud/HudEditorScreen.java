package me.kuskus.hud;

import me.kuskus.KusKusKlient;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.ColorSetting;
import me.kuskus.setting.DoubleSetting;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.IntSetting;
import me.kuskus.setting.Setting;
import me.kuskus.setting.SettingGroup;
import me.kuskus.setting.StringSetting;
import me.kuskus.util.ColorUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {
    private static final int GRID = 6;
    private static final int PANEL_W = 268;
    private static final int HEADER_H = 18;
    private static final int SETTING_H = 16;
    private static final int VALUE_W = 58;
    private static final int COLOR_W = 78;
    private static final int STRING_W = 128;
    private static final int PANEL_MIN_H = 96;
    private static final int SELECTOR_BUTTON_W = 18;
    private static final int HUD_PREVIEW_W = 96;
    private static final int HUD_PREVIEW_H = 18;

    private final Screen parent;
    private final List<SettingHitbox> settingHitboxes = new ArrayList<>();
    private HudElement selected;
    private HudElement draggingElement;
    private int elementDragX;
    private int elementDragY;
    private int panelX = -1;
    private int panelY = 12;
    private boolean draggingPanel;
    private int panelDragX;
    private int panelDragY;
    private EditingState editing;

    public HudEditorScreen() {
        this(null);
    }

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Kus Kus Klient HUD"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x66000000);
        drawGrid(context);
        settingHitboxes.clear();
        if (selected == null && !KusKusKlient.HUD.all().isEmpty()) {
            selected = KusKusKlient.HUD.all().get(0);
        }
        if (panelX < 0) {
            panelX = snap(width - PANEL_W - 18);
            panelY = snap(42);
        }
        if (draggingPanel) {
            panelX = snap(panelDragX + mouseX);
            panelY = snap(panelDragY + mouseY);
        }
        clampPanel(contentHeightEstimate());

        super.render(context, mouseX, mouseY, delta);
        renderHudElements(context, mouseX, mouseY);
        renderBrand(context);
        renderSettingsPanel(context, mouseX, mouseY);
        renderHint(context);
    }

    private void drawGrid(DrawContext context) {
        int color = 0x102A2A2A;
        for (int x = 0; x < width; x += GRID) {
            if (x % (GRID * 4) == 0) {
                RenderUtil.lineV(context, x, 0, height, color);
            }
        }
        for (int y = 0; y < height; y += GRID) {
            if (y % (GRID * 4) == 0) {
                RenderUtil.lineH(context, 0, y, width, color);
            }
        }
    }

    private void renderBrand(DrawContext context) {
        String brand = "Kus Kus Klient";
        context.drawText(textRenderer, brand, width - textRenderer.getWidth(brand) - 8, 8, KusKusTheme.TEXT_MAIN, false);
    }

    private void renderHudElements(DrawContext context, int mouseX, int mouseY) {
        for (HudElement element : KusKusKlient.HUD.all()) {
            renderElementPreview(context, element);
            int outline = element == selected
                ? KusKusTheme.PRIMARY
                : ColorUtil.withAlpha(KusKusTheme.LIGHT, previewContains(element, mouseX, mouseY) ? 0xD0 : 0x75);
            RenderUtil.outline(context, element.x() - 2, element.y() - 2, previewWidth(element) + 4, previewHeight() + 4, outline);
        }
    }

    private void renderElementPreview(DrawContext context, HudElement element) {
        int previewW = previewWidth(element);
        int previewH = previewHeight();
        int previewBg = element.visible()
            ? ColorUtil.withAlpha(KusKusTheme.BG_DARK, 0x75)
            : ColorUtil.withAlpha(KusKusTheme.BG_DARK, 0xA5);
        int previewText = element.visible() ? KusKusTheme.TEXT_MAIN : KusKusTheme.TEXT_DIM;

        RenderUtil.rect(context, element.x() - 1, element.y() - 1, previewW + 2, previewH + 2, previewBg);
        context.drawText(textRenderer, element.name(), element.x() + 4, element.y() + 3, previewText, false);
        if (!element.visible()) {
            String hidden = "off";
            context.drawText(textRenderer, hidden, element.x() + previewW - textRenderer.getWidth(hidden) - 4, element.y() + 3, KusKusTheme.TEXT_DIM, false);
        }
    }

    private int previewWidth(HudElement element) {
        int statusW = element.visible() ? 0 : textRenderer.getWidth("off") + 10;
        return Math.max(HUD_PREVIEW_W, textRenderer.getWidth(element.name()) + statusW + 12);
    }

    private int previewHeight() {
        return HUD_PREVIEW_H;
    }

    private boolean previewContains(HudElement element, double mouseX, double mouseY) {
        return mouseX >= element.x()
            && mouseX <= element.x() + previewWidth(element)
            && mouseY >= element.y()
            && mouseY <= element.y() + previewHeight();
    }

    private void renderSettingsPanel(DrawContext context, int mouseX, int mouseY) {
        if (selected == null) {
            return;
        }

        int height = contentHeightEstimate();
        clampPanel(height);

        RenderUtil.softShadow(context, panelX, panelY, PANEL_W, height, 0x33000000);
        RenderUtil.rect(context, panelX, panelY, PANEL_W, height, ColorUtil.withAlpha(KusKusTheme.BG_PANEL, 0xED));
        RenderUtil.outline(context, panelX, panelY, PANEL_W, height, KusKusTheme.outline());
        RenderUtil.rect(context, panelX, panelY, PANEL_W, HEADER_H, KusKusTheme.panelHeader());
        RenderUtil.lineH(context, panelX, panelY + HEADER_H - 1, PANEL_W, KusKusTheme.outline());
        context.drawText(textRenderer, selected.name(), panelX + 8, panelY + 5, KusKusTheme.TEXT_MAIN, false);
        context.drawText(textRenderer, "drag me", panelX + PANEL_W - textRenderer.getWidth("drag me") - 8, panelY + 5, KusKusTheme.TEXT_GRAY, false);

        int toggleX = panelX + 8;
        int toggleY = panelY + 24;
        int selectorY = toggleY + SETTING_H + 6;
        renderElementSelector(context, toggleX, selectorY, PANEL_W - 16, mouseX, mouseY);

        RenderUtil.rect(context, toggleX, toggleY, 70, SETTING_H, selected.visible() ? KusKusTheme.button(true, false) : KusKusTheme.setting(false));
        RenderUtil.centeredText(context, textRenderer, selected.visible() ? "Visible" : "Hidden", toggleX, toggleY + 4, 70, selected.visible() ? KusKusTheme.TEXT_MAIN : KusKusTheme.TEXT_GRAY);
        settingHitboxes.add(new SettingHitbox(null, toggleX, toggleY, 70, SETTING_H, false, 0, 0, 0, 0, true, 0));

        int itemY = selectorY + SETTING_H + 8;
        context.enableScissor(panelX, panelY + HEADER_H, panelX + PANEL_W, panelY + height - 2);
        for (SettingGroup group : selected.groups()) {
            context.drawText(textRenderer, group.name(), panelX + 8, itemY, KusKusTheme.TEXT_DIM, false);
            itemY += 12;
            for (Setting<?> setting : group.settings()) {
                itemY = renderSetting(context, setting, panelX + 8, itemY, PANEL_W - 16, mouseX, mouseY);
            }
        }
        context.disableScissor();
    }

    private void renderElementSelector(DrawContext context, int x, int y, int w, int mouseX, int mouseY) {
        int leftX = x;
        int rightX = x + w - SELECTOR_BUTTON_W;
        int middleX = leftX + SELECTOR_BUTTON_W + 4;
        int middleW = w - (SELECTOR_BUTTON_W * 2) - 8;

        boolean hoverLeft = mouseX >= leftX && mouseX <= leftX + SELECTOR_BUTTON_W && mouseY >= y && mouseY < y + SETTING_H;
        boolean hoverMid = mouseX >= middleX && mouseX <= middleX + middleW && mouseY >= y && mouseY < y + SETTING_H;
        boolean hoverRight = mouseX >= rightX && mouseX <= rightX + SELECTOR_BUTTON_W && mouseY >= y && mouseY < y + SETTING_H;

        RenderUtil.rect(context, leftX, y, SELECTOR_BUTTON_W, SETTING_H, KusKusTheme.setting(hoverLeft));
        RenderUtil.rect(context, middleX, y, middleW, SETTING_H, KusKusTheme.setting(hoverMid));
        RenderUtil.rect(context, rightX, y, SELECTOR_BUTTON_W, SETTING_H, KusKusTheme.setting(hoverRight));

        RenderUtil.outline(context, leftX, y, SELECTOR_BUTTON_W, SETTING_H, KusKusTheme.outline());
        RenderUtil.outline(context, middleX, y, middleW, SETTING_H, KusKusTheme.outline());
        RenderUtil.outline(context, rightX, y, SELECTOR_BUTTON_W, SETTING_H, KusKusTheme.outline());

        RenderUtil.centeredText(context, textRenderer, "<", leftX, y + 4, SELECTOR_BUTTON_W, KusKusTheme.TEXT_MAIN);
        RenderUtil.centeredText(context, textRenderer, fit(selected.name(), middleW - 6), middleX, y + 4, middleW, KusKusTheme.TEXT_MAIN);
        RenderUtil.centeredText(context, textRenderer, ">", rightX, y + 4, SELECTOR_BUTTON_W, KusKusTheme.TEXT_MAIN);

        settingHitboxes.add(new SettingHitbox(null, leftX, y, SELECTOR_BUTTON_W, SETTING_H, false, 0, 0, 0, 0, false, -1));
        settingHitboxes.add(new SettingHitbox(null, rightX, y, SELECTOR_BUTTON_W, SETTING_H, false, 0, 0, 0, 0, false, 1));
    }

    private int renderSetting(DrawContext context, Setting<?> setting, int x, int y, int w, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY < y + SETTING_H;
        boolean editingThis = editing != null && editing.setting == setting;

        if (setting instanceof BoolSetting bool) {
            RenderUtil.rect(context, x, y, w, SETTING_H, bool.get() ? KusKusTheme.button(true, hover) : KusKusTheme.setting(hover));
            context.drawText(textRenderer, fit(setting.name(), w - 6), x + 4, y + 5, bool.get() ? KusKusTheme.TEXT_MAIN : KusKusTheme.TEXT_GRAY, false);
            settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, false, 0, 0, 0, 0, false, 0));
            return y + SETTING_H + 2;
        }

        if (setting instanceof IntSetting integer) {
            int fieldX = x + w - VALUE_W;
            drawNumeric(context, setting.name(), integer.get(), integer.min(), integer.max(), x, y, w - VALUE_W - 4, fieldX, VALUE_W, hover, editingThis);
            settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, true, fieldX, y, VALUE_W, SETTING_H, false, 0));
            return y + SETTING_H + 2;
        }

        if (setting instanceof DoubleSetting decimal) {
            int fieldX = x + w - VALUE_W;
            drawNumeric(context, setting.name(), decimal.get(), decimal.min(), decimal.max(), x, y, w - VALUE_W - 4, fieldX, VALUE_W, hover, editingThis);
            settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, true, fieldX, y, VALUE_W, SETTING_H, false, 0));
            return y + SETTING_H + 2;
        }

        if (setting instanceof ColorSetting color) {
            int fieldX = x + w - COLOR_W;
            RenderUtil.rect(context, x, y, w, SETTING_H, KusKusTheme.setting(hover));
            context.drawText(textRenderer, fit(setting.name(), w - COLOR_W - 6), x + 4, y + 5, KusKusTheme.TEXT_GRAY, false);
            drawValueBox(context, fieldX, y, COLOR_W, SETTING_H, hover || editingThis);
            RenderUtil.rect(context, fieldX + 3, y + 3, 8, 8, 0xFF000000 | (color.get() & 0xFFFFFF));
            context.drawText(textRenderer, fit(editingThis ? editing.buffer + "_" : color.hex(), COLOR_W - 16), fieldX + 13, y + 5, KusKusTheme.TEXT_MAIN, false);
            settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, true, fieldX, y, COLOR_W, SETTING_H, false, 0));
            return y + SETTING_H + 2;
        }

        if (setting instanceof StringSetting string) {
            int fieldX = x + w - STRING_W;
            RenderUtil.rect(context, x, y, w, SETTING_H, KusKusTheme.setting(hover));
            context.drawText(textRenderer, fit(setting.name(), w - STRING_W - 6), x + 4, y + 5, KusKusTheme.TEXT_GRAY, false);
            drawValueBox(context, fieldX, y, STRING_W, SETTING_H, hover || editingThis);
            context.drawText(textRenderer, fit(editingThis ? editing.buffer + "_" : string.get(), STRING_W - 6), fieldX + 3, y + 5, KusKusTheme.TEXT_MAIN, false);
            settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, true, fieldX, y, STRING_W, SETTING_H, false, 0));
            return y + SETTING_H + 2;
        }

        if (setting instanceof EnumSetting<?> enumSetting) {
            int fieldX = x + w - VALUE_W;
            RenderUtil.rect(context, x, y, w, SETTING_H, KusKusTheme.setting(hover));
            context.drawText(textRenderer, fit(setting.name(), w - VALUE_W - 6), x + 4, y + 5, KusKusTheme.TEXT_GRAY, false);
            drawValueBox(context, fieldX, y, VALUE_W, SETTING_H, hover);
            context.drawText(textRenderer, fit(String.valueOf(enumSetting.get()), VALUE_W - 6), fieldX + 3, y + 5, KusKusTheme.TEXT_MAIN, false);
            settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, false, 0, 0, 0, 0, false, 0));
            return y + SETTING_H + 2;
        }

        RenderUtil.rect(context, x, y, w, SETTING_H, KusKusTheme.setting(hover));
        context.drawText(textRenderer, fit(setting.name() + " " + setting.get(), w - 6), x + 4, y + 5, KusKusTheme.TEXT_MAIN, false);
        settingHitboxes.add(new SettingHitbox(setting, x, y, w, SETTING_H, false, 0, 0, 0, 0, false, 0));
        return y + SETTING_H + 2;
    }

    private void drawNumeric(DrawContext context, String name, double value, double min, double max, int x, int y, int sliderW, int fieldX, int fieldW, boolean hover, boolean editingThis) {
        RenderUtil.rect(context, x, y, sliderW, SETTING_H, KusKusTheme.setting(hover));
        double progress = max <= min ? 0.0 : (value - min) / (max - min);
        int fillW = (int) Math.round(sliderW * Math.max(0.0, Math.min(1.0, progress)));
        RenderUtil.rect(context, x, y, fillW, SETTING_H, KusKusTheme.button(true, hover));
        context.drawText(textRenderer, fit(name, sliderW - 6), x + 4, y + 5, KusKusTheme.TEXT_MAIN, false);
        drawValueBox(context, fieldX, y, fieldW, SETTING_H, hover || editingThis);
        context.drawText(textRenderer, fit(editingThis ? editing.buffer + "_" : formatNumber(value), fieldW - 6), fieldX + 3, y + 5, KusKusTheme.TEXT_MAIN, false);
    }

    private void drawValueBox(DrawContext context, int x, int y, int w, int h, boolean active) {
        RenderUtil.rect(context, x, y, w, h, active ? ColorUtil.withAlpha(KusKusTheme.LIGHT, 0x30) : ColorUtil.withAlpha(KusKusTheme.BG_DARK, 0xC0));
        RenderUtil.outline(context, x, y, w, h, active ? KusKusTheme.PRIMARY : KusKusTheme.outline());
    }

    private void renderHint(DrawContext context) {
        // Intentionally minimal: the screen should stay visually clean.
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int button = click.button();

        finishEditing(false);

        if (mouseX >= panelX && mouseX <= panelX + PANEL_W && mouseY >= panelY && mouseY < panelY + HEADER_H) {
            if (button == 0) {
                draggingPanel = true;
                panelDragX = panelX - mouseX;
                panelDragY = panelY - mouseY;
                return true;
            }
        }

        for (SettingHitbox hitbox : settingHitboxes) {
            if (!hitbox.contains(mouseX, mouseY)) {
                continue;
            }
            if (hitbox.visibilityToggle) {
                selected.setVisible(!selected.visible());
                KusKusKlient.HUD.save();
                return true;
            }
            if (hitbox.selectorDirection != 0) {
                cycleSelected(hitbox.selectorDirection);
                return true;
            }
            if (hitbox.hasInput && hitbox.containsInput(mouseX, mouseY)) {
                startEditing(hitbox.setting);
                return true;
            }
            editSetting(hitbox.setting, button, mouseX, hitbox);
            KusKusKlient.HUD.save();
            return true;
        }

        for (HudElement element : KusKusKlient.HUD.all()) {
            if (!previewContains(element, mouseX, mouseY)) {
                continue;
            }
            selected = element;
            if (button == 1) {
                element.setVisible(!element.visible());
                KusKusKlient.HUD.save();
                return true;
            }
            if (button == 0) {
                draggingElement = element;
                elementDragX = mouseX - element.x();
                elementDragY = mouseY - element.y();
                return true;
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
            startEditing(setting);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (draggingPanel) {
            panelX = snap(panelDragX + mouseX);
            panelY = snap(panelDragY + mouseY);
            clampPanel(contentHeightEstimate());
            return true;
        }
        if (draggingElement != null) {
            draggingElement.moveTo(mouseX - elementDragX, mouseY - elementDragY);
            KusKusKlient.HUD.save();
            return true;
        }
        for (SettingHitbox hitbox : settingHitboxes) {
            boolean slider = hitbox.setting instanceof IntSetting || hitbox.setting instanceof DoubleSetting;
            if (slider && hitbox.contains(mouseX, mouseY) && !hitbox.containsInput(mouseX, mouseY)) {
                editSetting(hitbox.setting, 0, mouseX, hitbox);
                KusKusKlient.HUD.save();
                return true;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingPanel = false;
        draggingElement = null;
        clampPanel(contentHeightEstimate());
        return super.mouseReleased(click);
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
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (editing != null) {
            int keyCode = input.key();
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

        if (input.key() == GLFW.GLFW_KEY_DOWN || input.key() == GLFW.GLFW_KEY_RIGHT) {
            cycleSelected(1);
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_UP || input.key() == GLFW.GLFW_KEY_LEFT) {
            cycleSelected(-1);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        finishEditing(true);
        KusKusKlient.HUD.save();
        client.setScreen(parent);
    }

    private void startEditing(Setting<?> setting) {
        editing = new EditingState(setting, editableValue(setting));
    }

    private void finishEditing(boolean apply) {
        if (editing == null) {
            return;
        }

        if (apply) {
            applyEditedValue(editing.setting, editing.buffer);
            KusKusKlient.HUD.save();
        }
        editing = null;
    }

    private void applyEditedValue(Setting<?> setting, String buffer) {
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
        } catch (NumberFormatException ignored) {
        }
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

    private int snap(int value) {
        return Math.round(value / (float) GRID) * GRID;
    }

    private int contentHeightEstimate() {
        if (selected == null) {
            return PANEL_MIN_H;
        }
        int contentHeight = HEADER_H + 28;
        contentHeight += SETTING_H + 8;
        for (SettingGroup group : selected.groups()) {
            contentHeight += 14;
            contentHeight += group.settings().size() * (SETTING_H + 2);
        }
        contentHeight += 10;
        return Math.min(this.height - 12, Math.max(PANEL_MIN_H, contentHeight));
    }

    private void cycleSelected(int direction) {
        List<HudElement> elements = KusKusKlient.HUD.all();
        if (elements.isEmpty()) {
            return;
        }
        if (selected == null) {
            selected = elements.get(0);
            return;
        }
        int index = elements.indexOf(selected);
        if (index < 0) {
            selected = elements.get(0);
            return;
        }
        int next = (index + direction + elements.size()) % elements.size();
        selected = elements.get(next);
    }

    private void clampPanel(int panelHeight) {
        panelX = Math.max(6, Math.min(width - PANEL_W - 6, panelX));
        panelY = Math.max(6, Math.min(height - panelHeight - 6, panelY));
        panelX = snap(panelX);
        panelY = snap(panelY);
    }

    private static final class SettingHitbox {
        private final Setting<?> setting;
        private final float x;
        private final float y;
        private final float w;
        private final float h;
        private final boolean hasInput;
        private final float inputX;
        private final float inputY;
        private final float inputW;
        private final float inputH;
        private final boolean visibilityToggle;
        private final int selectorDirection;

        private SettingHitbox(Setting<?> setting, float x, float y, float w, float h, boolean hasInput, float inputX, float inputY, float inputW, float inputH, boolean visibilityToggle, int selectorDirection) {
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.hasInput = hasInput;
            this.inputX = inputX;
            this.inputY = inputY;
            this.inputW = inputW;
            this.inputH = inputH;
            this.visibilityToggle = visibilityToggle;
            this.selectorDirection = selectorDirection;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY < y + h;
        }

        private boolean containsInput(double mouseX, double mouseY) {
            return hasInput && mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY < inputY + inputH;
        }
    }

    private static final class EditingState {
        private final Setting<?> setting;
        private String buffer;

        private EditingState(Setting<?> setting, String buffer) {
            this.setting = setting;
            this.buffer = buffer;
        }
    }
}
