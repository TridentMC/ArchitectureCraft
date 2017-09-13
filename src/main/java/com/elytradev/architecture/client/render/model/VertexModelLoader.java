package com.elytradev.architecture.client.render.model;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import static com.elytradev.architecture.legacy.common.ArchitectureCraft.MOD_ID;

public class VertexModelLoader implements ICustomModelLoader {

    public static VertexModel MODEL;

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourcePath().equals(MOD_ID);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        if (MODEL == null)
            MODEL = new VertexModel();

        return MODEL;
    }


    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        MODEL = null;
    }
}
