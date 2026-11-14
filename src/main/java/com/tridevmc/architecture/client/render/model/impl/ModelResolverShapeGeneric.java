package com.tridevmc.architecture.client.render.model.impl;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMeshCached;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.resolver.functional.FunctionalQuadMetadataResolver;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.item.ItemShape;
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

import java.util.Map;
import java.util.Objects;

public class ModelResolverShapeGeneric implements IModelResolver<PolygonData> {

    private static final Map<BlockState, Material.Baked> TEXTURE_CACHE = Maps.newConcurrentMap();
    private static final IQuadMetadataResolver<PolygonData> METADATA_RESOLVER;

    static {
        var builder = FunctionalQuadMetadataResolver.<PolygonData>builder();
        METADATA_RESOLVER = builder.textureResolver(
                d -> getTextureForState(Blocks.OAK_PLANKS.defaultBlockState())
        ).blockTextureResolver(
                (level, pos, state, metadata) -> {
                    // Get the tile entity so we can pull in the material states.
                    var shapeBe = level.getModelData(pos).get(BlockEntityShape.MATERIALS);
                    Objects.requireNonNull(shapeBe, "Shape tile entity was null when resolving block texture.");
                    return getTextureForState(metadata.textureIndex() == 0 ? shapeBe.base() : shapeBe.secondary());
                }
        ).itemTextureResolver(
                (stack, metadata) -> {
                    // Pull the shape and material states from the item stack, no secondary states are on the items. They get applied with cladding instead.
                    var shape = stack.getItem() instanceof ItemCladding
                            ? EnumShape.CLADDING_SHEET : ItemShape.getShapeFromStack(stack);
                    var material = ItemShape.getStateFromStack(stack);
                    Objects.requireNonNull(shape, "Shape was null when resolving item texture.");
                    return getTextureForState(material);
                }
        ).tintIndexResolver(
                d -> -1
        ).build();
    }

    private final BakedQuadContainerProviderMesh<String, PolygonData> mesh;
    private final EnumShape shape;

    public ModelResolverShapeGeneric(EnumShape shape) {
        this.shape = shape;
        this.mesh = new BakedQuadContainerProviderMeshCached<>(ShapeMeshes.getMesh(shape));
    }

    private static Material.Baked getTextureForState(BlockState state) {
        return TEXTURE_CACHE.computeIfAbsent(state, ModelResolverShapeGeneric::calculateTextureForState);
    }

    private static Material.Baked calculateTextureForState(BlockState state) {
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
    public TextureAtlasSprite getDefaultSprite() {
        return ModelResolverShapeGeneric.getTextureForState(Blocks.OAK_PLANKS.defaultBlockState()).sprite();
    }
}
