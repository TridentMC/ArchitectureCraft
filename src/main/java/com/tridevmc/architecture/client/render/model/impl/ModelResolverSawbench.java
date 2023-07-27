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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

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

    private static void doInit() {
        if (needsInit) {
            var textureGetter = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
            textures = new TextureAtlasSprite[]{
                    textureGetter.apply(new ResourceLocation("block/oak_planks")),
                    textureGetter.apply(new ResourceLocation("block/iron_block"))
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
    public IBakedQuadContainer getQuads(LevelAccessor level, BlockPos pos, BlockState state,
                                        IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return MESH.getQuads("root", resolver, transform);
    }

    @Override
    public IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<PolygonData> resolver, ITrans3 transform) {
        return MESH.getQuads("root", resolver, transform);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        doInit();
        return textures[0];
    }

}
