package com.tridevmc.architecture.client.render.model.impl;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMeshCached;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.resolver.functional.FunctionalQuadMetadataResolver;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.item.component.ComponentMaterial;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Objects;

public class ModelResolverShapeGeneric implements IModelResolver<PolygonData> {

    private static final Map<BlockState, Material.Baked> MATERIAL_CACHE = Maps.newConcurrentMap();
    private static final IQuadMetadataResolver<PolygonData> METADATA_RESOLVER;

    static {
        var builder = FunctionalQuadMetadataResolver.<PolygonData>builder();
        METADATA_RESOLVER = builder.materialResolver(
                d -> getMaterialForState(Blocks.OAK_PLANKS.defaultBlockState())
        ).blockMaterialResolver(
                (level, pos, state, metadata) -> {
                    if (level == BlockAndTintGetter.EMPTY) {
                        return getMaterialForState(Blocks.OAK_PLANKS.defaultBlockState());
                    }
                    // Get the tile entity so we can pull in the material states.
                    var shapeBe = level.getModelData(pos).get(BlockEntityShape.MATERIALS);
                    Objects.requireNonNull(shapeBe, "Shape tile entity was null when resolving block texture.");
                    return getMaterialForState(metadata.textureIndex() == 0 ? shapeBe.base() : shapeBe.secondary());
                }
        ).itemMaterialResolver(
                (stack, metadata) -> {
                    // Pull the shape and material states from the item stack, including secondary states applied with cladding.
                    var shape = stack.getItem() instanceof ItemCladding
                            ? EnumShape.CLADDING_SHEET : ItemShape.getShapeFromStack(stack);
                    var material = stack.getOrDefault(ArchitectureMod.CONTENT.componentMaterial, ComponentMaterial.DEFAULT);
                    Objects.requireNonNull(shape, "Shape was null when resolving item texture.");
                    return getMaterialForState(metadata.textureIndex() == 0 ? material.safeBase() : material.safeSecondary());
                }
        ).tintIndexResolver(
                d -> -1
        ).build();
    }

    private final BakedQuadContainerProviderMesh<String, PolygonData> mesh;
    private final EnumShape shape;

    public ModelResolverShapeGeneric(EnumShape shape) {
        this.shape = shape;
        this.mesh = new BakedQuadContainerProviderMeshCached<>(Objects.requireNonNull(ShapeMeshes.getMesh(shape), "Shape mesh was null: " + shape));
    }

    public static void clearCache() {
        MATERIAL_CACHE.clear();
    }

    private static Material.Baked getMaterialForState(BlockState state) {
        return MATERIAL_CACHE.computeIfAbsent(state, ModelResolverShapeGeneric::calculateMaterialForState);
    }

    private static Material.Baked calculateMaterialForState(BlockState state) {
        if (state != null)
            return Minecraft.getInstance().getModelManager()
                    .getBlockStateModelSet().getParticleMaterial(state);
        else
            return null;
    }

    @Override
    public IQuadMetadataResolver<PolygonData> getMetadataResolver() {
        return METADATA_RESOLVER;
    }

    @Override
    public IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state,
                                        IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return mesh.getQuads(null, level, pos, state, resolver, transform, false);
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return mesh.getQuads(null, stack, resolver, transform, false);
    }

    @Override
    public Material.Baked getDefaultMaterial() {
        return ModelResolverShapeGeneric.getMaterialForState(Blocks.OAK_PLANKS.defaultBlockState());
    }
}
