package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.data.FunctionalQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.data.IQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.objson.OBJSONModel;
import com.tridevmc.architecture.client.render.model.objson.OBJSONQuadMetadata;
import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SawbenchModel extends OBJSONModel {

    private static TextureAtlasSprite[] textures;
    private static int[] tints;
    private static final IQuadMetadataResolver<OBJSONQuadMetadata> resolver = FunctionalQuadMetadataResolver.of(
            m -> {
                doInit();
                return textures[Math.min(m.texture(), textures.length -1)];
            },
            m -> {
                doInit();
                return tints[Math.min(m.texture(), tints.length -1)];
            }
    );
    private static boolean needsInit = true;

    public SawbenchModel() {
        super(ArchitectureMod.PROXY.getCachedOBJSON("block/sawbench_all.objson"), true, false);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        doInit();
        return textures[0];
    }

    @Override
    public IQuadMetadataResolver<OBJSONQuadMetadata> getMetadataResolver(LevelAccessor level, BlockPos pos, BlockState state) {
        return resolver;
    }

    private static void doInit() {
        if (needsInit) {
            var textureGetter = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
            textures = new TextureAtlasSprite[]{
                    textureGetter.apply(new ResourceLocation("block/oak_planks")),
                    textureGetter.apply(new ResourceLocation("block/iron_block"))
            };
            tints = new int[]{-1, -1};
            needsInit = false;
        }
    }

}
