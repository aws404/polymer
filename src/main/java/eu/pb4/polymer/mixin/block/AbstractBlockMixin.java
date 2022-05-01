package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this instanceof PolymerBlock block) {
            var clientState = PolymerBlockUtils.getBlockStateSafely(block, state);
            if (!(clientState.getBlock() instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getOutlineShape(world, pos, context));
            }
        }
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceCollision(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this instanceof PolymerBlock block) {
            var clientState = PolymerBlockUtils.getBlockStateSafely(block, state);
            if (!(clientState.getBlock() instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getCollisionShape(world, pos, context));
            }
        }
    }
}
