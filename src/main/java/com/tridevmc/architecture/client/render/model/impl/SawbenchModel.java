package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.client.render.model.OBJSONModel;
import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import java.util.function.Function;

public class SawbenchModel extends OBJSONModel {

    private static TextureAtlasSprite[] textures;
    private static int[] colours;
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
    public TextureAtlasSprite[] getTextures(IBlockDisplayReader world, BlockPos pos) {
        doInit();
        return textures;
    }

    @Override
    public int[] getColours(IBlockDisplayReader world, BlockPos pos) {
        doInit();
        return colours;
    }

    private static void doInit() {
        if (needsInit) {
            Function<ResourceLocation, TextureAtlasSprite> textureGetter = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            textures = new TextureAtlasSprite[]{
                    textureGetter.apply(new ResourceLocation("block/oak_planks")),
                    textureGetter.apply(new ResourceLocation("block/iron_block"))
            };
            colours = new int[]{-1, -1};
            needsInit = false;
        }
    }

}
