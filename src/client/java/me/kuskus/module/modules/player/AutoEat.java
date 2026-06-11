package me.kuskus.module.modules.player;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.TickEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.IntSetting;
import me.kuskus.setting.SettingGroup;

public class AutoEat extends Module {
    public AutoEat() {
        super("AutoEat", "Skeleton automatic eating module.", Category.PLAYER);
        SettingGroup general = group("General");
        general.add(new IntSetting("Hunger", "Hunger threshold.", 14, 1, 20));
        general.add(new BoolSetting("Full Health", "Eat even on full health.", false));
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
        // TODO: select food and hold use when hunger matches the configured threshold.
    }
}
