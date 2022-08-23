package com.tridevmc.architecture.client.render.model.loader;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.tridevmc.architecture.client.render.model.baked.ShapeBakedModel;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.behaviour.ShapeBehaviourModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.Map;

public class ArchitectureShapeModelLoader implements IGeometryLoader<ArchitectureModelGeometry>, ResourceManagerReloadListener {

    private final Map<EnumShape, ArchitectureModelGeometry> models = Maps.newHashMap();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.models.clear();
    }

    @Override
    public ArchitectureModelGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        String shapeName = modelContents.get("shapeName").getAsString();
        boolean generateUVs = !modelContents.has("generateUVs") || modelContents.get("generateUVs").getAsBoolean();
        EnumShape shape = EnumShape.forName(shapeName);
        ArchitectureModelGeometry geometry = null;
        if (this.models.containsKey(shape)) {
            geometry = this.models.get(shape);
        } else {
            if (shape.behaviour instanceof ShapeBehaviourModel) {
                geometry = new ArchitectureModelGeometry(new ShapeBakedModel(shape, generateUVs));
            } else {
                // TODO: Dummy fallback for non models.
                geometry = new ArchitectureModelGeometry(new ShapeBakedModel(EnumShape.POLE, generateUVs));
            }
            this.models.put(shape, geometry);
        }
        return geometry;
    }
}
