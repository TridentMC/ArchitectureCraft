package com.tridevmc.architecture.client.render.model;

import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IArchitectureModel {

    ArchitectureModelData.ModelDataQuads getQuads(BlockState state, LevelAccessor world, BlockPos pos);

    TextureAtlasSprite getDefaultSprite();

    List<BakedQuad> getDefaultModel();
}
