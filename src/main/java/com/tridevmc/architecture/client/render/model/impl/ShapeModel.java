package com.tridevmc.architecture.client.render.model.impl;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.behaviour.ShapeBehaviourModel;
import com.tridevmc.architecture.legacy.client.render.model.objson.OBJSONModel;
import com.tridevmc.architecture.legacy.client.render.model.objson.OBJSONQuadMetadata;
import com.tridevmc.architecture.legacy.client.render.model.objson.OBJSONQuadMetadataResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ShapeModel extends OBJSONModel {

    private static final Map<BlockState, TextureAtlasSprite> SPRITE_CACHE = Maps.newHashMap();
    private final EnumShape shape;

    public ShapeModel(EnumShape shape, ShapeBehaviourModel shapeBehaviour, boolean generateUVs) {
        super(ArchitectureMod.PROXY.getCachedOBJSON(shapeBehaviour.getModelName()), false, false);
        this.shape = shape;
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return getSpriteForState(Blocks.OAK_PLANKS.defaultBlockState());
    }

    @Override
    public IQuadMetadataResolver<OBJSONQuadMetadata> getMetadataResolver(LevelAccessor level, BlockPos pos, BlockState state) {
        return new OBJSONQuadMetadataResolver(this.getTextures(level, pos), this.getColours(level, pos));
    }

    public TextureAtlasSprite[] getTextures(LevelAccessor world, BlockPos pos) {
        ShapeBlockEntity shape = ShapeBlockEntity.get(world, pos);
        if (shape != null) {
            TextureAtlasSprite baseSprite = getSpriteForState(shape.getBaseBlockState());
            TextureAtlasSprite secondarySprite = shape.hasSecondaryMaterial() ? getSpriteForState(shape.getSecondaryBlockState()) : baseSprite;
            return new TextureAtlasSprite[]{baseSprite, secondarySprite};
        }
        return new TextureAtlasSprite[]{getSpriteForState(Blocks.OAK_PLANKS.defaultBlockState())};
    }

    public int[] getColours(LevelAccessor world, BlockPos pos) {
        ShapeBlockEntity shape = ShapeBlockEntity.get(world, pos);
        if (shape != null) {
            int baseColour = getColourForState(world, pos, shape.getBaseBlockState());
            int secondaryColour = shape.hasSecondaryMaterial() ? getColourForState(world, pos, shape.getSecondaryBlockState()) : baseColour;
            return new int[]{baseColour, secondaryColour};
        }
        return new int[]{-1, -1};
    }

    private static TextureAtlasSprite getSpriteForState(BlockState state) {
        if (!SPRITE_CACHE.containsKey(state)) {
            SPRITE_CACHE.put(state, Utils.getSpriteForBlockState(state));
        }
        return SPRITE_CACHE.get(state);
    }

    //TODO: Use model decomposition from CarpentryCubes to get more accurate textures and colours?
    private static int getColourForState(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockColors colours = Minecraft.getInstance().getBlockColors();
        return colours.getColor(state, world, pos, 0);
    }

}
