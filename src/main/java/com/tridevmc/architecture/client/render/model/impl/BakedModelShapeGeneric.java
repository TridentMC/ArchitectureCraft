package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IModelResolverBaked;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

public class BakedModelShapeGeneric implements IModelResolverBaked<PolygonData> {

    private final ModelResolverShapeGeneric resolver;
    private final EnumShape shape;

    public BakedModelShapeGeneric(EnumShape shape) {
        this.shape = shape;
        this.resolver = new ModelResolverShapeGeneric(shape);
    }

    @Override
    public IModelResolver<PolygonData> getModelResolver() {
        return this.resolver;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleIcon() {
        return this.resolver.getDefaultSprite();
    }

    @Override
    @NotNull
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
