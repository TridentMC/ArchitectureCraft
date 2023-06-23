package com.tridevmc.architecture.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityArchitecture extends BlockEntity {

    public BlockEntityArchitecture(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

}
