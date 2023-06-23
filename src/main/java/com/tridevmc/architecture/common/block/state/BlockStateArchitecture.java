package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateArchitecture extends BlockState {

    public BlockStateArchitecture(Block block,
                                  ImmutableMap<Property<?>, Comparable<?>> properties,
                                  MapCodec<BlockState> codec) {
        super(block, properties, codec);
    }

}
