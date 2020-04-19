package com.tridevmc.architecture.client.render.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

public class ArchitectureModelLoader implements IModelLoader<ArchitectureModelGeometry> {

    private final ArchitectureModelGeometry geometry;

    public ArchitectureModelLoader(ArchitectureModelGeometry geometry) {
        this.geometry = geometry;
    }

    public ArchitectureModelLoader(IBakedModel model, ResourceLocation... textures) {
        this.geometry = new ArchitectureModelGeometry(model, textures);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // NO-OP
    }

    @Override
    public ArchitectureModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return this.geometry;
    }
}
