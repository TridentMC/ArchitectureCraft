package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.client.render.model.OBJSONModel;
import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import java.util.function.Function;

public class SawbenchModel extends OBJSONModel {

    private static TextureAtlasSprite[] textures;
    private static Integer[] colours;
    private static boolean needsInit = true;

    public SawbenchModel() {
        super(OBJSON.fromResource(new ResourceLocation(ArchitectureMod.MOD_ID, "block/sawbench.objson")), true, false);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        doInit();
        return textures[0];
    }

    @Override
    public TextureAtlasSprite[] getTextures(ILightReader world, BlockPos pos) {
        doInit();
        return textures;
    }

    @Override
    public Integer[] getColours(ILightReader world, BlockPos pos) {
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
            colours = new Integer[]{-1, -1};
            needsInit = false;
        }
    }

}
