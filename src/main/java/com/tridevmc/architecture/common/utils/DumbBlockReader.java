package com.tridevmc.architecture.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class DumbBlockReader implements BlockGetter {

    private BlockState state = Blocks.AIR.defaultBlockState();

    public DumbBlockReader(BlockState state) {
        this.state = state;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.state.getFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos p_45570_) {
        return null;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }
}
