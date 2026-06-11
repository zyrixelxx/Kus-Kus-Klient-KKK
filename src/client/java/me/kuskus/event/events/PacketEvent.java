package me.kuskus.event.events;

import me.kuskus.event.Event;
import net.minecraft.network.packet.Packet;

public abstract class PacketEvent extends Event {
    private final Packet<?> packet;

    protected PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> packet() {
        return packet;
    }

    public static final class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }

    public static final class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }
}
