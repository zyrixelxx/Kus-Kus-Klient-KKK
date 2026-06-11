package me.kuskus.module.modules.movement;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.TickEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.SettingGroup;

public class Sprint extends Module {
    public enum Mode { LEGIT, RAGE }

    public Sprint() {
        super("Sprint", "Skeleton automatic sprint module.", Category.MOVEMENT);
        SettingGroup general = group("General");
        general.add(new EnumSetting<>("Mode", "Sprint mode.", Mode.LEGIT, Mode.class));
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
        if (event.client().player != null && event.client().player.input != null) {
            event.client().player.setSprinting(event.client().player.input.hasForwardMovement());
        }
    }
}
