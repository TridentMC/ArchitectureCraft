package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.legacy.client.render.model.data.FunctionalQuadMetadataResolver;
import com.tridevmc.architecture.legacy.client.render.model.objson.OBJSONQuadMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SawbenchModelResolver implements IModelResolver<OBJSONQuadMetadata> {

    private static TextureAtlasSprite[] textures;
    private static int[] colours;
    private static final IQuadMetadataResolver<OBJSONQuadMetadata> resolver = FunctionalQuadMetadataResolver.of(
            m -> {
                doInit();
                return textures[Math.min(m.texture(), textures.length - 1)];
            },
            m -> {
                doInit();
                return colours[Math.min(m.tintIndex(), colours.length - 1)];
            }
    );
    private static boolean needsInit = true;

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
    public IQuadMetadataResolver<OBJSONQuadMetadata> getMetadataResolver(ItemStack stack) {
        return resolver;
    }

    @Override
    public IQuadMetadataResolver<OBJSONQuadMetadata> getMetadataResolver(LevelAccessor level, BlockPos pos, BlockState state) {
        return resolver;
    }

    @Override
    public IBakedQuadContainer getQuads(IQuadMetadataResolver<OBJSONQuadMetadata> resolver, ITrans3 transform) {
        return null;
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        doInit();
        return textures[0];
    }

}
