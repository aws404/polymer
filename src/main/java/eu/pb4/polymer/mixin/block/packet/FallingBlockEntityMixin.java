package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

    @ModifyArg(method = "createSpawnPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtual(BlockState state) {
        if (state.getBlock() instanceof PolymerBlock virtualBlock) {
            return PolymerBlockUtils.getBlockStateSafely(virtualBlock, state);
        } else {
            return state;
        }
    }
}
