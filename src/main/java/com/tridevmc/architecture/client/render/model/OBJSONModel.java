package com.tridevmc.architecture.client.render.model;

import com.google.common.collect.Lists;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import java.util.*;
import java.util.stream.Collectors;

public abstract class OBJSONModel implements IArchitectureModel {

    private final OBJSON objson;
    private final ArchitectureModelData convertedModelData;
    protected ArrayList<Integer>[] textureQuadMap;

    public OBJSONModel(OBJSON objson) {
        this.objson = objson;
        this.convertedModelData = new ArchitectureModelData();
        ArrayList<ArrayList<Integer>> textureQuads = Lists.newArrayList();

        int quadNumber = 0;
        for (OBJSON.Face face : Arrays.stream(this.objson.faces).sorted(Comparator.comparingInt(o -> o.texture)).collect(Collectors.toList())) {
            ArrayList<Integer> quadList = this.addOrGet(textureQuads, face.texture, Lists.newArrayList());
            for (int[] tri : face.triangles) {
                for (int i = 0; i < 3; i++) {
                    int j = tri[i];
                    double[] c = face.vertices[j];
                    this.convertedModelData.addTriInstruction(null, c[0], c[1], c[2], c[6] * 16, c[7] * 16, c[3], c[4], c[5]);
                }
                quadList.add(quadNumber);
                quadNumber++;
            }
        }
        this.textureQuadMap = new ArrayList[textureQuads.size()];
        for (int i = 0; i < textureQuads.size(); i++) {
            this.textureQuadMap[i] = textureQuads.get(i);
        }
    }

    private <T> T addOrGet(ArrayList<T> list, int index, T element) {
        if (index >= list.size()) {
            list.add(index, element);
        }
        return list.get(index);
    }

    @Override
    public ArchitectureModelData.ModelDataQuads getQuads(BlockState state, ILightReader world, BlockPos pos) {
        this.convertedModelData.setState(state);
        TextureAtlasSprite[] textures = this.getTextures(world, pos);
        Integer[] colours = this.getColours(world, pos);
        for (int i = 0; i < this.textureQuadMap.length; i++) {
            ArrayList<Integer> quads = this.textureQuadMap[i];
            TextureAtlasSprite texture = textures[i];
            Integer colour = colours[i];

            for (Integer quad : quads) {
                this.convertedModelData.setFaceData(quad, null, texture, colour);
            }
        }

        return this.convertedModelData.buildModel();
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
    }

    @Override
    public List<BakedQuad> getDefaultModel() {
        return Collections.emptyList();
    }

    public abstract TextureAtlasSprite[] getTextures(ILightReader world, BlockPos pos);

    public abstract Integer[] getColours(ILightReader world, BlockPos pos);
}
