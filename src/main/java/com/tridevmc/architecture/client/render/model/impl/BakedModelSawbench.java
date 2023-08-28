package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IModelResolverBaked;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BakedModelSawbench implements IModelResolverBaked<PolygonData> {

    private static ModelResolverSawbench RESOLVER;
    private final ItemTransforms itemTransforms;

    public BakedModelSawbench() {
        this(ItemTransforms.NO_TRANSFORMS);
    }

    public BakedModelSawbench(ItemTransforms itemTransforms) {
        if (RESOLVER == null) {
            RESOLVER = new ModelResolverSawbench();
        }
        this.itemTransforms = itemTransforms;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.itemTransforms;
    }

    @Override
    public IModelResolver<PolygonData> getModelResolver() {
        return RESOLVER;
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
    public TextureAtlasSprite getParticleIcon() {
        return RESOLVER.getDefaultSprite();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

}
