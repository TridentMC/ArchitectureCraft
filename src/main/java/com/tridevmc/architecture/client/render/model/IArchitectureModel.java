package com.tridevmc.architecture.client.render.model;

import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import java.util.List;

public interface IArchitectureModel {

    ArchitectureModelData.ModelDataQuads getQuads(BlockState state, IBlockDisplayReader world, BlockPos pos);

    TextureAtlasSprite getDefaultSprite();

    List<BakedQuad> getDefaultModel();
}
