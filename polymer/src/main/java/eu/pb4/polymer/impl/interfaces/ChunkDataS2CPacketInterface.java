package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.api.x.BlockMapper;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ChunkDataS2CPacketInterface {
    WorldChunk polymer_getWorldChunk();
    BlockMapper polymer_getMapper();
    boolean polymer_hasPlayerDependentBlocks();
}
