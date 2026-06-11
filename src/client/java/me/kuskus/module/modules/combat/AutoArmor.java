package me.kuskus.module.modules.combat;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.TickEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.SettingGroup;

public class AutoArmor extends Module {
    public enum Priority { PROTECTION, UNBREAKING, THORNS }

    public AutoArmor() {
        super("AutoArmor", "Skeleton auto armor module.", Category.COMBAT);
        SettingGroup general = group("General");
        general.add(new EnumSetting<>("Priority", "Armor evaluation priority.", Priority.PROTECTION, Priority.class));
    }

    @Override
    protected void onEnable() {
        KusKusKlient.EVENTS.subscribe(this);
    }

    @Override
    protected void onDisable() {
        KusKusKlient.EVENTS.unsubscribe(this);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        // TODO: evaluate inventory armor and equip the best safe option.
    }
}
