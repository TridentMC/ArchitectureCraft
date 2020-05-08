package com.tridevmc.architecture.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class DumbBlockReader implements IBlockReader {

    private BlockState state = Blocks.AIR.getDefaultState();

    public DumbBlockReader(BlockState state){
        this.state = state;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.state;
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return this.state.getFluidState();
    }
}
