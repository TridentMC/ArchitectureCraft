package com.tridevmc.architecture.client.render.model.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.item.component.ComponentMaterial;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ModelResolverShapeGeneric implements IModelResolver<PolygonData> {
    private static final Cache<RenderKey, IBakedQuadContainer> QUADS = CacheBuilder.newBuilder()
            .maximumSize(1024).build();
    private final EnumShape shape;

    private record RenderKey(String meshName, ITrans3Immutable transform,
                             Material.Baked base, Material.Baked secondary) {}

    public ModelResolverShapeGeneric(EnumShape shape) { this.shape = shape; }

    public static void clearCache() { QUADS.invalidateAll(); }

    private static Material.Baked material(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state);
    }

    private static IQuadMetadataResolver<PolygonData> metadata(Material.Baked base, Material.Baked secondary) {
        return new IQuadMetadataResolver<>() {
            @Override
            public TextureAtlasSprite getTexture(PolygonData data) {
                return (data.textureIndex() == 0 ? base : secondary).sprite();
            }

            @Override
            public Material.Baked getMaterial(BlockGetter level, BlockPos pos, BlockState state,
                                               ItemStack stack, PolygonData data) {
                return data.textureIndex() == 0 ? base : secondary;
            }

            @Override
            public int getTintIndex(PolygonData data) { return -1; }
        };
    }

    @Override
    public IQuadMetadataResolver<PolygonData> getMetadataResolver() {
        var sprite = material(Blocks.OAK_PLANKS.defaultBlockState());
        return metadata(sprite, sprite);
    }

    @Override
    public IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state,
                                        IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        var selected = ShapeMeshes.getMesh(this.shape);
        var materials = level.getModelData(pos).get(BlockEntityShape.MATERIALS);
        var base = materials == null ? Blocks.OAK_PLANKS.defaultBlockState() : materials.base();
        var secondary = materials == null ? base : materials.secondary();
        return this.bake(selected, transform, base, secondary);
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver,
                                        ITrans3 transform) {
        var material = stack.getOrDefault(ArchitectureMod.CONTENT.componentMaterial, ComponentMaterial.DEFAULT);
        return this.bake(ShapeMeshes.getMesh(this.shape), transform,
                material.safeBase(), material.safeSecondary());
    }

    private IBakedQuadContainer bake(IMesh<String, PolygonData> mesh, ITrans3 transform,
                                     BlockState baseState, BlockState secondaryState) {
        var key = new RenderKey(mesh.getName(), transform.asImmutable(), material(baseState), material(secondaryState));
        var cached = QUADS.getIfPresent(key);
        if (cached != null) return cached;
        var quads = new BakedQuadContainerProviderMesh<>(mesh)
                .getQuads(null, metadata(key.base, key.secondary), transform);
        QUADS.put(key, quads);
        return quads;
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() { return material(Blocks.OAK_PLANKS.defaultBlockState()).sprite(); }
}
