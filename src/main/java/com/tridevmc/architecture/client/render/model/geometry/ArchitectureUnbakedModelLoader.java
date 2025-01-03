package com.tridevmc.architecture.client.render.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

import javax.annotation.Nullable;


public class ArchitectureUnbakedModelLoader implements UnbakedModelLoader<ArchitectureUnbakedModel>, ResourceManagerReloadListener {

    private final IArchitectureBakedModelSupplier bakedModelSupplier;
    private ArchitectureUnbakedModel cachedGeometry;

    public ArchitectureUnbakedModelLoader(IArchitectureBakedModelSupplier bakedModelSupplier) {
        this.bakedModelSupplier = bakedModelSupplier;
    }

    private ArchitectureUnbakedModel getGeometry(@Nullable ResourceLocation parentName, @Nullable ItemTransforms itemTransforms, @Nullable Boolean ambientOcclusion, @Nullable UnbakedModel.GuiLight guiLight) {
        if (this.cachedGeometry == null) {
            this.cachedGeometry = new ArchitectureUnbakedModel(this.bakedModelSupplier, parentName, itemTransforms, ambientOcclusion, guiLight);
        }
        return this.cachedGeometry;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.cachedGeometry = null;
    }

    @Override
    public ArchitectureUnbakedModel read(JsonObject root, JsonDeserializationContext ctx) {
        JsonObject jsonobject = root.getAsJsonObject();
        String parentName = this.getParentName(jsonobject);
        Boolean ambientOcclusionNullable = this.getAmbientOcclusion(jsonobject);
        ItemTransforms itemTransforms = null;
        if (jsonobject.has("display")) {
            JsonObject displayObject = GsonHelper.getAsJsonObject(jsonobject, "display");
            itemTransforms = ctx.deserialize(displayObject, ItemTransforms.class);
        }
        UnbakedModel.GuiLight guiLight = null;
        if (jsonobject.has("gui_light")) {
            guiLight = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(jsonobject, "gui_light"));
        }
        ResourceLocation resourcelocation = parentName.isEmpty() ? null : ResourceLocation.parse(parentName);

        return this.getGeometry(resourcelocation, itemTransforms, ambientOcclusionNullable, guiLight);
    }

    private String getParentName(JsonObject json) {
        return GsonHelper.getAsString(json, "parent", "");
    }

    @Nullable
    protected Boolean getAmbientOcclusion(JsonObject json) {
        return json.has("ambientocclusion") ? GsonHelper.getAsBoolean(json, "ambientocclusion") : null;
    }
}
