package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ModelResolverShapeGeneric implements IModelResolver<PolygonData> {

    private final EnumShape shape;

    public ModelResolverShapeGeneric(EnumShape shape) {
        this.shape = shape;
    }

    @Override
    public IQuadMetadataResolver<PolygonData> getMetadataResolver() {
        return null;
    }

    @Override
    public IBakedQuadContainer getQuads(LevelAccessor level, BlockPos pos, BlockState state, IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return null;
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return null;
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return null;
    }
}
