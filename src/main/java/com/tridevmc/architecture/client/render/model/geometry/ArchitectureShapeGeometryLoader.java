package com.tridevmc.architecture.client.render.model.geometry;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.tridevmc.architecture.client.render.model.impl.BakedModelShapeGeneric;
import com.tridevmc.architecture.common.shape.EnumShape;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.Map;
import java.util.function.Function;

public class ArchitectureShapeGeometryLoader implements IGeometryLoader<IArchitectureModelGeometry>, ResourceManagerReloadListener {

    private final Map<EnumShape, IArchitectureModelGeometry> models = Maps.newHashMap();

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
