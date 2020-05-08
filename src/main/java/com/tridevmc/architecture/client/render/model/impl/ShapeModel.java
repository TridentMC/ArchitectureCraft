package com.tridevmc.architecture.client.render.model.impl;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.client.render.model.OBJSONModel;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.tile.TileShape;
import com.tridevmc.architecture.common.utils.DumbLightReader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import java.util.Map;

public class ShapeModel extends OBJSONModel {

    private static final Map<BlockState, TextureAtlasSprite> SPRITE_CACHE = Maps.newHashMap();

    public ShapeModel(EnumShape shape) {
        super(OBJSON.fromResource(new ResourceLocation(ArchitectureMod.MOD_ID, "objson/shape/" + shape.getName() + ".objson")), true, false);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return getSpriteForState(Blocks.OAK_PLANKS.getDefaultState());
    }

    @Override
    public TextureAtlasSprite[] getTextures(ILightReader world, BlockPos pos) {
        TileShape shape = TileShape.get(world, pos);
        if (shape != null) {
            TextureAtlasSprite baseSprite = getSpriteForState(shape.getBaseBlockState());
            TextureAtlasSprite secondarySprite = shape.hasSecondaryMaterial() ? getSpriteForState(shape.getSecondaryBlockState()) : baseSprite;
            return new TextureAtlasSprite[]{baseSprite, secondarySprite};
        }
        return new TextureAtlasSprite[]{getSpriteForState(Blocks.OAK_PLANKS.getDefaultState())};
    }

    @Override
    public Integer[] getColours(ILightReader world, BlockPos pos) {
        TileShape shape = TileShape.get(world, pos);
        if (shape != null) {
            int baseColour = getColourForState(world, pos, shape.getBaseBlockState());
            int secondaryColour = shape.hasSecondaryMaterial() ? getColourForState(world, pos, shape.getSecondaryBlockState()) : baseColour;
            return new Integer[]{baseColour, secondaryColour};
        }
        return new Integer[]{-1, -1};
    }

    private static TextureAtlasSprite getSpriteForState(BlockState state) {
        if (!SPRITE_CACHE.containsKey(state)) {
            SPRITE_CACHE.put(state, Utils.getSpriteForBlockState(state));
        }
        return SPRITE_CACHE.get(state);
    }

    //TODO: Use model decomposition from CarpentryCubes to get more accurate textures and colours?
    private static int getColourForState(ILightReader world, BlockPos pos, BlockState state) {
        BlockColors colours = Minecraft.getInstance().getBlockColors();
        return colours.getColor(state, new DumbLightReader(world, state), pos, 0);
    }

}
