package wtf.choco.veinminer.pattern;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.block.BlockAccessor;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMinerConfig;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * Represents a pattern used to allocate blocks for vein mining.
 */
public interface VeinMiningPattern {

    /**
     * Get the {@link NamespacedKey} of this pattern.
     *
     * @return the key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Allocate all {@link BlockPosition BlockPositions} that should be mined according to the input values.
     *
     * @param blockAccessor the block accessor
     * @param origin the position at which the vein mining was initiated
     * @param block the type of {@link VeinMinerBlock} that was broken at the origin
     * @param config the configuration applicable for this instance of vein mining
     * @param aliasList a {@link BlockList} of all blocks that should also be considered. May be empty
     *
     * @return the allocated block positions
     */
    @NotNull
    public Set<BlockPosition> allocateBlocks(@NotNull BlockAccessor blockAccessor, @NotNull BlockPosition origin, @NotNull VeinMinerBlock block, @NotNull VeinMinerConfig config, @Nullable BlockList aliasList);

    /**
     * Allocate all {@link BlockPosition BlockPositions} that should be mined according to the input values.
     *
     * @param blockAccessor the block accessor
     * @param origin the position at which the vein mining was initiated
     * @param block the type of {@link VeinMinerBlock} that was broken at the origin
     * @param config the configuration applicable for this instance of vein mining
     *
     * @return the allocated block positions
     */
    @NotNull
    public default Set<BlockPosition> allocateBlocks(@NotNull BlockAccessor blockAccessor, @NotNull BlockPosition origin, @NotNull VeinMinerBlock block, @NotNull VeinMinerConfig config) {
        return allocateBlocks(blockAccessor, origin, block, config, null);
    }

}
