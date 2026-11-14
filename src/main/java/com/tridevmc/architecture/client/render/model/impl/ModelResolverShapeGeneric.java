package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMeshCached;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.item.component.ComponentMaterial;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.core.math.ITrans3;
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

    private final BakedQuadContainerProviderMesh<String, PolygonData> mesh;

    public ModelResolverShapeGeneric(EnumShape shape) {
        this.mesh = new BakedQuadContainerProviderMeshCached<>(ShapeMeshes.getMesh(shape));
    }

    private static Material.Baked getMaterialForState(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state);
    }

    @Override
    public IQuadMetadataResolver<PolygonData> getMetadataResolver() {
        var material = getMaterialForState(Blocks.OAK_PLANKS.defaultBlockState());
        return new ShapeQuadMetadataResolver(material, material);
    }

    @Override
    public IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state,
                                        IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        // Get the tile entity's model data so we can pull in the material states.
        var materials = level.getModelData(pos).get(BlockEntityShape.MATERIALS);
        var base = materials == null ? Blocks.OAK_PLANKS.defaultBlockState() : materials.base();
        var secondary = materials == null ? base : materials.secondary();
        return this.mesh.getQuads(null,
                new ShapeQuadMetadataResolver(getMaterialForState(base), getMaterialForState(secondary)), transform);
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver,
                                        ITrans3 transform) {
        // Pull the material states from the item stack, including any secondary material applied with cladding.
        var material = stack.getOrDefault(ArchitectureMod.CONTENT.componentMaterial, ComponentMaterial.DEFAULT);
        return this.mesh.getQuads(null, new ShapeQuadMetadataResolver(
                getMaterialForState(material.safeBase()), getMaterialForState(material.safeSecondary())), transform);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return getMaterialForState(Blocks.OAK_PLANKS.defaultBlockState()).sprite();
    }
}
