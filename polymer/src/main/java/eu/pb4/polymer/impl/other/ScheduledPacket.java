package eu.pb4.polymer.impl.other;

import net.minecraft.network.Packet;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ScheduledPacket(Packet<?> packet, int time) {
}
