package com.tridevmc.architecture.client.render.model.loader;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArchitectureModelGeometry implements IUnbakedGeometry<ArchitectureModelGeometry> {

    private final BakedModel bakedModel;
    private final Collection<Material> textures;

    public ArchitectureModelGeometry(BakedModel bakedModel, ResourceLocation... textures) {
        this.bakedModel = bakedModel;
        this.textures = Arrays.stream(textures).map(t -> new Material(TextureAtlas.LOCATION_BLOCKS, t)).collect(Collectors.toList());
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        // TODO: Transforms, sprite getters, all that fun stuff.
        return this.bakedModel;
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function modelGetter, Set missingTextureErrors) {
        return this.textures;
    }

}
