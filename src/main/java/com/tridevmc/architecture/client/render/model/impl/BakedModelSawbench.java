package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IModelResolverBaked;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.resources.model.sprite.Material;

public class BakedModelSawbench implements IModelResolverBaked<PolygonData> {

    private static ModelResolverSawbench RESOLVER;

    public BakedModelSawbench() {
        if (RESOLVER == null) {
            RESOLVER = new ModelResolverSawbench();
        }
    }

    @Override
    public IModelResolver<PolygonData> getModelResolver() {
        return RESOLVER;
    }

    @Override
    public Material.Baked particleMaterial() {
        return RESOLVER.getDefaultMaterial();
    }

}
