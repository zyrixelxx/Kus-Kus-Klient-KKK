package me.kuskus.module.modules.world;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.TickEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.IntSetting;
import me.kuskus.setting.SettingGroup;

public class AutoMine extends Module {
    public AutoMine() {
        super("AutoMine", "Skeleton automatic mining module.", Category.WORLD);
        SettingGroup general = group("General");
        general.add(new IntSetting("Radius", "Scan radius.", 3, 1, 5));
        general.add(new BoolSetting("Ores Only", "Only target ores.", true));
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
        // TODO: scan nearby blocks and mine selected targets safely.
    }
}
