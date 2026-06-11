package me.kuskus.gui;

import me.kuskus.KusKusKlient;
import me.kuskus.gui.util.KusKusTheme;
import me.kuskus.gui.util.RenderUtil;
import me.kuskus.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SearchOverlay extends Screen {
    private String query = "";

    public SearchOverlay() {
        super(Text.literal("Kus Kus Klient Search"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = 12;
        int h = 118;
        int y = this.height - h - 12;
        int w = 230;

        RenderUtil.rect(context, x, y, w, h, 0xCC0F0D0C);
        RenderUtil.outline(context, x, y, w, h, KusKusTheme.PRIMARY_DIM);
        RenderUtil.rect(context, x + 8, y + 8, w - 16, 18, KusKusTheme.BG_BUTTON);
        RenderUtil.text(context, textRenderer, query.isBlank() ? "Search modules..." : query, x + 13, y + 13, query.isBlank() ? KusKusTheme.TEXT_DIM : KusKusTheme.TEXT_MAIN);

        List<Module> modules = results();
        int cy = y + 34;
        for (int i = 0; i < Math.min(6, modules.size()); i++) {
            Module module = modules.get(i);
            int rowColor = module.enabled() ? KusKusTheme.BG_ACTIVE : KusKusTheme.BG_BUTTON;
            if (inside(mouseX, mouseY, x + 8, cy, w - 16, 15)) rowColor = 0xEE312A26;
            RenderUtil.rect(context, x + 8, cy, w - 16, 15, rowColor);
            RenderUtil.text(context, textRenderer, module.name(), x + 12, cy + 4, KusKusTheme.TEXT_MAIN);
            String category = module.category().name();
            RenderUtil.text(context, textRenderer, category, x + w - textRenderer.getWidth(category) - 12, cy + 4, KusKusTheme.TEXT_DIM);
            cy += 17;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int x = 12;
        int h = 118;
        int y = this.height - h - 12;
        int w = 230;
        int cy = y + 34;
        List<Module> modules = results();
        for (int i = 0; i < Math.min(6, modules.size()); i++) {
            if (inside(click.x(), click.y(), x + 8, cy, w - 16, 15)) {
                Module module = modules.get(i);
                if (click.button() == 0) {
                    module.toggle();
                    KusKusKlient.CONFIG.saveModule(module);
                } else if (click.button() == 1) {
                    client.setScreen(new KusKusScreen(module));
                }
                return true;
            }
            cy += 17;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (input.isValidChar()) {
            query += input.asString();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || ((input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && key == GLFW.GLFW_KEY_F)) {
            close();
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = query.substring(0, query.length() - 1);
            return true;
        }
        return super.keyPressed(input);
    }

    private List<Module> results() {
        return query.isBlank() ? KusKusKlient.MODULES.all() : KusKusKlient.MODULES.search(query);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx <= x + w && my <= y + h;
    }
}
