package com.tridevmc.architecture.client.render.model.geometry;


import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Extension of {@link ExtendedUnbakedModel}, used for making stacktrace errors more readable.
 */
public class ArchitectureUnbakedModel implements ExtendedUnbakedModel {

    @NotNull
    private final IArchitectureBakedModelSupplier supplier;
    @Nullable
    private UnbakedModel parent;
    @Nullable
    private final ResourceLocation parentLocation;
    @Nullable
    private final ItemTransforms itemTransforms;
    @Nullable
    private final Boolean ambientOcclusion;
    @Nullable
    private final GuiLight guiLight;

    public ArchitectureUnbakedModel(@NotNull IArchitectureBakedModelSupplier supplier, @Nullable ResourceLocation parentLocation, @Nullable ItemTransforms itemTransforms, @Nullable Boolean ambientOcclusion, @Nullable GuiLight guiLight) {
        this.supplier = supplier;
        this.parentLocation = parentLocation;
        this.itemTransforms = itemTransforms;
        this.ambientOcclusion = ambientOcclusion;
        this.guiLight = guiLight;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        if (this.parentLocation != null) {
            this.parent = resolver.resolve(this.parentLocation);
        }
    }

    @Override
    @Nullable
    public UnbakedModel getParent() {
        return this.parent;
    }

    @Override
    public @Nullable Boolean getAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    @Override
    public @Nullable GuiLight getGuiLight() {
        return this.guiLight;
    }

    @Override
    public @Nullable ItemTransforms getTransforms() {
        return this.itemTransforms;
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        return this.supplier.bake(textures, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms, additionalProperties);
    }
}
