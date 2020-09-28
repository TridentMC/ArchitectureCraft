package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

@MethodsReturnNonnullByDefault
public class BlockStateArchitecture extends BlockState {
    public BlockStateArchitecture(BlockArchitecture block, ImmutableMap<Property<?>, Comparable<?>> propertyComparableImmutableMap, MapCodec<BlockState> blockStateMapCodec) {
        super(block, propertyComparableImmutableMap, blockStateMapCodec);
    }

    @Override
    public BlockArchitecture getBlock() {
        return (BlockArchitecture) super.getBlock();
    }

    @Override
    public float getBlockHardness(IBlockReader world, BlockPos pos) {
        return this.getBlock().getBlockHardness(this.getSelf(), world, pos, super.getBlockHardness(world, pos));
    }
}
