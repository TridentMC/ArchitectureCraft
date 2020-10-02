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
import net.minecraft.world.IBlockDisplayReader;

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
    protected int[][] textureQuadMap;

    public OBJSONModel(OBJSON objson, boolean generateUVs, boolean generateNormals) {
        this.objson = objson.offset(new Vector3(0.5, 0.5, 0.5));
        this.convertedModelData = new ArchitectureModelData();
        this.generateUVs = generateUVs;
        this.generateNormals = generateNormals;
        List<Tuple<Integer, OBJSON.Face>> mappedFaces = IntStream.range(0, this.objson.getFaces().length).mapToObj(i -> new Tuple<>(i, this.objson.getFaces()[i])).collect(Collectors.toList());
        mappedFaces = mappedFaces.stream().sorted(Comparator.comparingInt(o -> o.getB().texture)).collect(Collectors.toList());
        int minTexture = mappedFaces.get(0).getB().texture;
        mappedFaces.forEach(mF -> mF.getB().texture = mF.getB().texture - minTexture);
        ArrayList<ArrayList<Integer>> textureQuads = Lists.newArrayList();

        int quadNumber = 0;
        for (Tuple<Integer, OBJSON.Face> indexedFace : mappedFaces) {
            int faceIndex = indexedFace.getA();
            OBJSON.Face face = indexedFace.getB();
            ArrayList<Integer> quadList = this.addOrGet(textureQuads, face.texture, Lists.newArrayList());
            for (OBJSON.Triangle tri : face.triangles) {
                quadNumber = this.addTri(this.convertedModelData, quadList, faceIndex, quadNumber, tri, face.vertices);
            }
        }
        this.textureQuadMap = new int[textureQuads.size()][];
        for (int i = 0; i < textureQuads.size(); i++) {
            this.textureQuadMap[i] = textureQuads.get(i).stream().mapToInt(t -> t).toArray();
        }
        this.convertedModelData.resetState();
    }

    private int addTri(ArchitectureModelData modelData, ArrayList<Integer> quadList, int face, int quadNumber, OBJSON.Triangle tri, OBJSON.Vertex[] vertices) {
        for (int i = 0; i < 3; i++) {
            int vertexIndex = tri.vertices[i];
            OBJSON.Vertex vertex = vertices[vertexIndex];
            if (this.generateUVs && this.generateNormals) {
                modelData.addTriInstruction(face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z);
            } else if (this.generateUVs) {
                modelData.addTriInstruction(face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z, vertex.getNormal().x, vertex.getNormal().y, vertex.getNormal().z);
            } else if (this.generateNormals) {
                modelData.addTriInstruction(face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z, vertex.getU() * 16, vertex.getV() * 16);
            } else {
                modelData.addTriInstruction(face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z, vertex.getU() * 16, vertex.getV() * 16, vertex.getNormal().x, vertex.getNormal().y, vertex.getNormal().z);
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

    // TODO: FIXME!!! - Re-evaluate how we assign textures to model datas based on OBJSON. Something is fucked

    @Override
    public ArchitectureModelData.ModelDataQuads getQuads(BlockState state, IBlockDisplayReader world, BlockPos pos) {
        this.convertedModelData.setState(state);
        TextureAtlasSprite[] textures = this.getTextures(world, pos);
        int[] colours = this.getColours(world, pos);
        for (int i = 0; i < this.textureQuadMap.length; i++) {
            int[] quads = this.textureQuadMap[i];
            TextureAtlasSprite texture = textures[i];
            int colour = colours[i];

            for (int quad : quads) {
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

    public abstract TextureAtlasSprite[] getTextures(IBlockDisplayReader world, BlockPos pos);

    public abstract int[] getColours(IBlockDisplayReader world, BlockPos pos);
}
