package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;


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
    public boolean hasBlockEntity() {
        return this.getBlock().hasBlockEntity(this);
    }
}
