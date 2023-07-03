package com.tridevmc.architecture.common.shape.rule;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A rule that determines whether a block can connect to another block on a given side.
 */
@FunctionalInterface
public interface INeighbourConnectionRule {

    /**
     * Determines whether a block can connect to another block on a given side.
     *
     * @param other the block that is being connected to.
     * @param side  the side of the block that is being connected to,
     *              relative to the block that is trying to connect.
     * @return true if the block can connect, false otherwise.
     */
    boolean connectsToOnSide(BlockState other,
                             Direction side);

}
