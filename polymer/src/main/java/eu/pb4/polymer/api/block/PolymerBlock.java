package eu.pb4.polymer.api.block;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Interface used for creation of server side blocks
 */
public interface PolymerBlock extends PolymerObject {

    /**
     * Returns main/default block used on client
     *
     * @return Vanilla (or other) Block instance
     */
    Block getPolymerBlock(BlockState state);

    /**
     * Returns block used on client for player
     *
     * @return Vanilla (or other) Block instance
     */
    default Block getPolymerBlock(ServerPlayerEntity player, BlockState state) {
        return this.getPolymerBlock(state);
    }

    /**
     * Generic method used for replacing BlockStates in case of no player context
     * It also controls some server side things like collisions
     *
     * @param state Server side/real BlockState
     * @return BlockState visible on client
     */
    default BlockState getPolymerBlockState(BlockState state) {
        return this.getPolymerBlock(state).getDefaultState();
    }

    /**
     * Main method used for replacing BlockStates for players
     * Keep in mind you should ideally use blocks with the same hitbox as generic/non-player ones!
     *
     * @param player Player viewing it
     * @param state Server side BlocksState
     * @return Client side BlockState
     */
    default BlockState getPolymerBlockState(ServerPlayerEntity player, BlockState state) {
        return this.getPolymerBlockState(state);
    }

    /**
     * This method is called when block gets send to player
     * Allows to add client-only BlockEntities (for signs, heads, etc)
     *
     * @param player Player packets should be send to
     * @param pos Position of block. Keep in mind it's mutable,
     *            so make sure to use {@link BlockPos.Mutable#toImmutable()}
     *            in case of using in packets, as it's reused for other positions!
     * @param blockState Real BlockState of block
     */
    default void onPolymerBlockSend(ServerPlayerEntity player, BlockPos.Mutable pos, BlockState blockState) { }

    /**
     * You can override this method in case of issues with light updates of this block. In most cases it's not needed.
     * @param blockState
     */
    default boolean forceLightUpdates(BlockState blockState) { return false; }
}
