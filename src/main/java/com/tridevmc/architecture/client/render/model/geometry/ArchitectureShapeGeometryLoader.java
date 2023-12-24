package com.tridevmc.architecture.client.render.model.geometry;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.tridevmc.architecture.client.render.model.impl.BakedModelShapeGeneric;
import com.tridevmc.architecture.common.shape.EnumShape;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.util.Map;

public class ArchitectureShapeGeometryLoader implements IGeometryLoader<IArchitectureModelGeometry>, ResourceManagerReloadListener {

    private final Map<EnumShape, IArchitectureModelGeometry> models = Maps.newConcurrentMap();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.models.clear();
    }

    @Override
    public IArchitectureModelGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        var shapeName = modelContents.get("shapeName").getAsString();
        var shape = EnumShape.byName(shapeName);
        if (shape == null) {
            throw new IllegalArgumentException("Unknown shape: " + shapeName);
        }

        return this.models.computeIfAbsent(shape, s -> (context, baker, spriteGetter, modelState, overrides, modelLocation) -> new BakedModelShapeGeneric(shape, context.getTransforms()));
    }

}
