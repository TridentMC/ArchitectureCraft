package com.tridevmc.architecture.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;

public class DumbLightReader extends DumbBlockReader implements ILightReader {

    private ILightReader realWorld;

    public DumbLightReader(ILightReader world, BlockState state) {
        super(state);
        this.realWorld = world;
    }

    @Override
    public WorldLightManager getLightManager() {
        return this.realWorld.getLightManager();
    }

    @Override
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return this.realWorld.getBlockColor(blockPosIn, colorResolverIn);
    }
}
