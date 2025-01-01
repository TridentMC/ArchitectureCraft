package com.tridevmc.architecture.client.render.model.geometry;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.util.context.ContextMap;

/**
 * Small extraction off of {@link net.neoforged.neoforge.client.model.ExtendedUnbakedModel} to allow creating
 * very simple resolvers that we can still use with shared base logic for things like parents and transforms.
 */
@FunctionalInterface
public interface IArchitectureBakedModelSupplier {

    BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties);
}
