package com.tridevmc.architecture.client.render.model.loader;

import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArchitectureModelGeometry implements IModelGeometry<ArchitectureModelGeometry> {

    private IBakedModel bakedModel;
    private Collection<Material> textures;

    public ArchitectureModelGeometry(IBakedModel bakedModel, ResourceLocation... textures) {
        this.bakedModel = bakedModel;
        this.textures = Arrays.stream(textures).map(t -> new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, t)).collect(Collectors.toList());
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        // TODO: Transforms, sprite getters, all that fun stuff.
        return this.bakedModel;
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function modelGetter, Set missingTextureErrors) {
        return this.textures;
    }

}
