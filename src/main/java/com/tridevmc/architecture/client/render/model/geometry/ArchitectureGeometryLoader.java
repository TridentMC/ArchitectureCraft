package com.tridevmc.architecture.client.render.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

import java.util.function.Supplier;

public class ArchitectureGeometryLoader implements UnbakedModelLoader<IArchitectureUnbakedModel>, ResourceManagerReloadListener {

    private final Supplier<IArchitectureUnbakedModel> geometrySupplier;
    private IArchitectureUnbakedModel cachedGeometry;

    public ArchitectureGeometryLoader(Supplier<IArchitectureUnbakedModel> geometrySupplier) {
        this.geometrySupplier = geometrySupplier;
    }

    private IArchitectureUnbakedModel getGeometry() {
        if (this.cachedGeometry == null) {
            this.cachedGeometry = this.geometrySupplier.get();
        }
        return this.cachedGeometry;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.cachedGeometry = null;
    }

    @Override
    public IArchitectureUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        return this.getGeometry();
    }

}
