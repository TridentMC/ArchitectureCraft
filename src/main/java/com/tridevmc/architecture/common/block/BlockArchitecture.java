package com.tridevmc.architecture.common.block;

import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import net.minecraft.world.level.block.Block;

public class BlockArchitecture extends Block {

    public BlockArchitecture(Properties properties) {
        super(properties);
    }

    /**
     * Gets the transform for the given state, used for rendering.
     *
     * @param state The state to get the transform for.
     * @return The transform for the given state.
     */
    public ITrans3Immutable getTransformForState(BlockStateArchitecture state) {
        return ITrans3.ofIdentity();
    }
}
