package me.kuskus.addon;

import me.kuskus.module.Category;
import me.kuskus.module.Module;

public abstract class AddonModule extends Module {
    protected AddonModule(String name, String description, Category category) {
        super(name, description, category);
    }
}
