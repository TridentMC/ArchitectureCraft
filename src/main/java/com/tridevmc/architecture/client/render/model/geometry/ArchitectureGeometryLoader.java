package com.tridevmc.architecture.client.render.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.function.Function;
import java.util.function.Supplier;

public class ArchitectureGeometryLoader implements IGeometryLoader<IArchitectureModelGeometry>, ResourceManagerReloadListener {

    private final Supplier<IArchitectureModelGeometry> geometrySupplier;
    private IArchitectureModelGeometry cachedGeometry;

    public ArchitectureGeometryLoader(Supplier<IArchitectureModelGeometry> geometrySupplier) {
        this.geometrySupplier = geometrySupplier;
    }

    private IArchitectureModelGeometry getGeometry() {
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
    public IArchitectureModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        return this.getGeometry();
    }

}
