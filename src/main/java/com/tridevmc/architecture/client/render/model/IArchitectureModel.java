package com.tridevmc.architecture.client.render.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import java.util.ArrayList;
import java.util.List;

public interface IArchitectureModel {

    ArchitectureModelData.ModelDataQuads getQuads(BlockState state, ILightReader world, BlockPos pos);

    TextureAtlasSprite getDefaultSprite();

    List<BakedQuad> getDefaultModel();
}
