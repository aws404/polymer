package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public record PolymerVanillaItemGroupEntry(String identifier, List<ItemStack> stacks) implements BufferWritable {
    public static PolymerVanillaItemGroupEntry of(ItemGroup group, ServerPlayNetworkHandler handler) {
        var stacks = new ArrayList<ItemStack>();
        var list = DefaultedList.<ItemStack>of();
        group.appendStacks(list);

        for (var item : list) {
            var polymerItem = PolymerItemUtils.getPolymerItemStack(item, handler.player);
            if (polymerItem != item || polymerItem.getItem() instanceof PolymerObject) {
                stacks.add(polymerItem);
            }
        }

        return new PolymerVanillaItemGroupEntry(((ItemGroupAccessor) group).getId(), stacks);
    }

    @Override
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeString(identifier);
        buf.writeVarInt(stacks.size());
        for (var item : stacks) {
            buf.writeItemStack(ServerTranslationUtils.parseFor(handler, item));
        }
    }
}
