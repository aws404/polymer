package eu.pb4.polymer.mixin.client.item.packet;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotC2SPacketMixin {
    @Inject(method = "getStack", at = @At("TAIL"), cancellable = true)
    private void polymer_replaceWithReal(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(PolymerItemUtils.getRealItemStack(cir.getReturnValue()));
    }

    @Inject(method = "getModifiedStacks", at = @At("TAIL"), cancellable = true)
    private void polymer_replaceMultipleReal(CallbackInfoReturnable<Int2ObjectMap<ItemStack>> cir) {
        Int2ObjectMap map = new Int2ObjectArrayMap();

        for (Int2ObjectMap.Entry<ItemStack> entry : cir.getReturnValue().int2ObjectEntrySet()) {
            map.put(entry.getIntKey(), PolymerItemUtils.getRealItemStack(entry.getValue()));
        }

        cir.setReturnValue(map);
    }
}
