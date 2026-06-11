package me.kuskus.module.modules.movement;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.TickEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.DoubleSetting;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.SettingGroup;

public class Flight extends Module {
    public enum Mode { VANILLA, PACKET, ELYTRA }

    public Flight() {
        super("Flight", "Skeleton flight module.", Category.MOVEMENT);
        SettingGroup general = group("General");
        general.add(new EnumSetting<>("Mode", "Flight mode.", Mode.VANILLA, Mode.class));
        general.add(new DoubleSetting("Speed", "Horizontal speed.", 1.0, 0.1, 5.0));
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
        // TODO: implement movement through dedicated packet and movement hooks.
    }
}
