package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IModelResolverBaked;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

public class BakedModelShapeGeneric implements IModelResolverBaked<PolygonData> {

    private final ModelResolverShapeGeneric resolver;
    private final EnumShape shape;
    private final ItemTransforms transforms;

    public BakedModelShapeGeneric(EnumShape shape, ItemTransforms transforms) {
        this.shape = shape;
        this.resolver = new ModelResolverShapeGeneric(shape);
        this.transforms = transforms;
    }

    @Override
    @NotNull
    public ItemTransforms getTransforms() {
        return this.transforms;
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
        return true;
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
