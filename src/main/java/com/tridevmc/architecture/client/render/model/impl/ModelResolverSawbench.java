package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMeshCached;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.resolver.functional.FunctionalQuadMetadataResolver;
import com.tridevmc.architecture.common.block.BlockSawbench;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.PolygonData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class ModelResolverSawbench implements IModelResolver<PolygonData> {

    private static final BakedQuadContainerProviderMesh<String, PolygonData> MESH = new BakedQuadContainerProviderMeshCached<>(BlockSawbench.MODEL.mesh());
    private static TextureAtlasSprite[] textures;
    private static int[] colours;
    private static boolean needsInit = true;
    private static final IQuadMetadataResolver<PolygonData> resolver = FunctionalQuadMetadataResolver.of(
            m -> {
                doInit();
                return textures[Math.min(m.textureIndex(), textures.length - 1)];
            },
            m -> {
                doInit();
                return colours[Math.max(Math.min(m.tintIndex(), colours.length - 1), 0)];
            }
    );

    public static void clearCache() { needsInit = true; }

    private static void doInit() {
        if (needsInit) {
            Function<Identifier, TextureAtlasSprite> textureGetter = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)::getSprite;
            textures = new TextureAtlasSprite[]{
                    textureGetter.apply(Identifier.withDefaultNamespace("block/oak_planks")),
                    textureGetter.apply(Identifier.withDefaultNamespace("block/iron_block"))
            };
            colours = new int[]{-1, -1};
            needsInit = false;
        }
    }

    @Override
    public IQuadMetadataResolver<PolygonData> getMetadataResolver() {
        return resolver;
    }

    @Override
    public IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state,
                                        IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return MESH.getQuads("root", level, pos, state, resolver, transform);
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return MESH.getQuads("root", stack, resolver, transform);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        doInit();
        return textures[0];
    }

}
