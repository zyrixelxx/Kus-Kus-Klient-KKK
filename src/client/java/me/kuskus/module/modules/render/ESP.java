package me.kuskus.module.modules.render;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.RenderEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.ColorSetting;
import me.kuskus.setting.SettingGroup;

public class ESP extends Module {
    public ESP() {
        super("ESP", "Skeleton entity render module.", Category.RENDER);
        SettingGroup general = group("General");
        general.add(new BoolSetting("Players", "Render players.", true));
        general.add(new BoolSetting("Mobs", "Render mobs.", false));
        general.add(new ColorSetting("Player Color", "Player box color.", 0xFF6600));
        general.add(new ColorSetting("Mob Color", "Mob box color.", 0x00CC44));
        general.add(new BoolSetting("Through Walls", "Render through walls.", true));
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
    private void onRender(RenderEvent event) {
        // TODO: render entity boxes through the world render extraction pipeline.
    }
}
