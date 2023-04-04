package com.tridevmc.architecture.client.render.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class ArchitectureGeometryLoader implements IGeometryLoader<ArchitectureModelGeometry>, ResourceManagerReloadListener {

    private final ArchitectureModelGeometry geometry;

    public ArchitectureGeometryLoader(ArchitectureModelGeometry geometry) {
        this.geometry = geometry;
    }

    public ArchitectureGeometryLoader(BakedModel model, ResourceLocation... textures) {
        this.geometry = new ArchitectureModelGeometry(model, textures);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // NO-OP
    }

    @Override
    public ArchitectureModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        return this.geometry;
    }

}
