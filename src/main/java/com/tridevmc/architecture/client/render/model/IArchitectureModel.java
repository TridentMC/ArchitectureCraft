package com.tridevmc.architecture.client.render.model;

import com.tridevmc.architecture.client.render.model.data.ArchitectureModelDataQuads;
import com.tridevmc.architecture.client.render.model.data.IQuadMetadataResolver;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IArchitectureModel<T> {

    ArchitectureModelDataQuads getQuads(LevelAccessor level, BlockPos pos, BlockState state);

    IQuadMetadataResolver<T> getMetadataResolver(LevelAccessor level, BlockPos pos, BlockState state);

    TextureAtlasSprite getDefaultSprite();

    List<BakedQuad> getDefaultModel();
}
