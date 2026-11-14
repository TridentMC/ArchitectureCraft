package com.tridevmc.architecture.client.render.model.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
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

    @Override
    public IQuadMetadataResolver<PolygonData> getMetadataResolver() {
        var sprite = material(Blocks.OAK_PLANKS.defaultBlockState());
        return new ShapeQuadMetadataResolver(sprite, sprite);
    }

    @Override
    public IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state,
                                        IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        var selected = state instanceof BlockStateShape shapeState
                ? ((BlockShape<?>) state.getBlock()).getMeshForState(shapeState) : ShapeMeshes.getMesh(this.shape);
        // Get the tile entity's model data so we can pull in the material states.
        var materials = level.getModelData(pos).get(BlockEntityShape.MATERIALS);
        var base = materials == null ? Blocks.OAK_PLANKS.defaultBlockState() : materials.base();
        var secondary = materials == null ? base : materials.secondary();
        return this.bake(selected, transform, base, secondary);
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver,
                                        ITrans3 transform) {
        // Pull the material states from the item stack, including any secondary material applied with cladding.
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
                .getQuads(null, new ShapeQuadMetadataResolver(key.base, key.secondary), transform);
        QUADS.put(key, quads);
        return quads;
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() { return material(Blocks.OAK_PLANKS.defaultBlockState()).sprite(); }
}
