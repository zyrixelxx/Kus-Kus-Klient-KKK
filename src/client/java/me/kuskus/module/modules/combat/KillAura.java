package me.kuskus.module.modules.combat;

import me.kuskus.KusKusKlient;
import me.kuskus.event.EventHandler;
import me.kuskus.event.events.TickEvent;
import me.kuskus.module.Category;
import me.kuskus.module.Module;
import me.kuskus.setting.BoolSetting;
import me.kuskus.setting.DoubleSetting;
import me.kuskus.setting.EnumSetting;
import me.kuskus.setting.SettingGroup;

public class KillAura extends Module {
    public enum Mode { SINGLE, SWITCH, MULTI }

    public KillAura() {
        super("KillAura", "Skeleton target selection and attack module.", Category.COMBAT);
        SettingGroup general = group("General");
        general.add(new DoubleSetting("Radius", "Target search radius.", 4.0, 1.0, 6.0));
        general.add(new EnumSetting<>("Mode", "Target selection mode.", Mode.SINGLE, Mode.class));
        general.add(new BoolSetting("Players", "Attack players.", true));
        general.add(new BoolSetting("Mobs", "Attack mobs.", false));
        general.add(new BoolSetting("Ignore Friends", "Skip friends.", true));
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
        // TODO: find targets and attack via safe client interaction.
    }
}
