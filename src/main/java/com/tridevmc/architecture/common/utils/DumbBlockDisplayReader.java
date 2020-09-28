package com.tridevmc.architecture.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;

public class DumbBlockDisplayReader extends DumbBlockReader implements IBlockDisplayReader {

    private IBlockDisplayReader realWorld;

    public DumbBlockDisplayReader(IBlockDisplayReader world, BlockState state) {
        super(state);
        this.realWorld = world;
    }

    @Override
    public float func_230487_a_(Direction dir, boolean diffuse) {
        return realWorld.func_230487_a_(dir, diffuse);
    }

    @Override
    public WorldLightManager getLightManager() {
        return this.realWorld.getLightManager();
    }

    @Override
    public int getBlockColor(BlockPos pos, ColorResolver colorResolver) {
        return this.realWorld.getBlockColor(pos, colorResolver);
    }
}
