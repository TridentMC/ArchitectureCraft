package com.tridevmc.architecture.client.render.model;

import com.google.common.collect.Lists;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class OBJSONModel implements IArchitectureModel {

    private final OBJSON objson;
    private final ArchitectureModelData convertedModelData;
    private final boolean generateUVs;
    private final boolean generateNormals;
    protected ArrayList<Integer>[] textureQuadMap;

    private List<List<Vector3>> knownTris;

    public OBJSONModel(OBJSON objson, boolean generateUVs, boolean generateNormals) {
        this.objson = objson.offset(new Vector3(0.5, 0.5, 0.5));
        this.convertedModelData = new ArchitectureModelData();
        this.generateUVs = generateUVs;
        this.generateNormals = generateNormals;
        this.knownTris = Lists.newArrayList();
        List<Tuple<Integer, OBJSON.Face>> mappedFaces = IntStream.range(0, this.objson.faces.length).mapToObj(i -> new Tuple<>(i, this.objson.faces[i])).collect(Collectors.toList());
        mappedFaces = mappedFaces.stream().sorted(Comparator.comparingInt(o -> o.getB().texture)).collect(Collectors.toList());
        int minTexture = mappedFaces.get(0).getB().texture;
        mappedFaces.forEach(mF -> mF.getB().texture = mF.getB().texture - minTexture);
        ArrayList<ArrayList<Integer>> textureQuads = Lists.newArrayList();

        int quadNumber = 0;
        for (Tuple<Integer, OBJSON.Face> indexedFace : mappedFaces) {
            int faceIndex = indexedFace.getA();
            OBJSON.Face face = indexedFace.getB();
            ArrayList<Integer> quadList = this.addOrGet(textureQuads, face.texture, Lists.newArrayList());
            for (int[] tri : face.triangles) {
                quadNumber = this.addTri(this.convertedModelData, quadList, faceIndex, quadNumber, tri, face.vertices);
            }
        }
        this.textureQuadMap = new ArrayList[textureQuads.size()];
        for (int i = 0; i < textureQuads.size(); i++) {
            this.textureQuadMap[i] = textureQuads.get(i);
        }
        this.convertedModelData.resetState();
    }

    private int addTri(ArchitectureModelData modelData, ArrayList<Integer> quadList, int face, int quadNumber, int[] tri, double[][] vertices) {
        for (int i = 0; i < 3; i++) {
            int vertexIndex = tri[i];
            double[] vertex = vertices[vertexIndex];
            if (this.generateUVs && this.generateNormals) {
                modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2]);
            } else if (this.generateUVs) {
                modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2], vertex[3], vertex[4], vertex[5]);
            } else if (this.generateNormals) {
                modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2], vertex[6] * 16, vertex[7] * 16);
            } else {
                modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2], vertex[6] * 16, vertex[7] * 16, vertex[3], vertex[4], vertex[5]);
            }
        }
        quadList.add(quadNumber);
        quadNumber++;

        return quadNumber;
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
