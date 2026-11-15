package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IModelResolverBaked;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.resources.model.sprite.Material;
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
    @NotNull
    public Material.Baked particleMaterial() {
        return new Material.Baked(this.resolver.getDefaultSprite(), false);
    }

}
