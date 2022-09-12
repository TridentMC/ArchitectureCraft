package com.tridevmc.architecture.client.render.model;

import com.google.common.collect.Lists;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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
        this.objson = objson;
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
        //for (AxisAlignedBB bb : objson.getVoxelized()) {
        //    this.makeCuboid(convertedModelData, bb);
        //}
        this.convertedModelData.resetState();
    }

    private void makeCuboid(ArchitectureModelData data, AABB bb){
        float minX = (float) bb.minX;
        float minY = (float) bb.minY;
        float minZ = (float) bb.minZ;
        float maxX = (float) bb.maxX;
        float maxY = (float) bb.maxY;
        float maxZ = (float) bb.maxZ;
        data.addQuadInstruction(null, minX, minY, minZ);
        data.addQuadInstruction(null, minX, maxY, minZ);
        data.addQuadInstruction(null, maxX, maxY, minZ);
        data.addQuadInstruction(null, maxX, minY, minZ);

        data.addQuadInstruction(null, minX, minY, maxZ);
        data.addQuadInstruction(null, maxX, minY, maxZ);
        data.addQuadInstruction(null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, minX, maxY, maxZ);

        data.addQuadInstruction(null, minX, minY, minZ);
        data.addQuadInstruction(null, minX, maxY, minZ);
        data.addQuadInstruction(null, minX, maxY, maxZ);
        data.addQuadInstruction(null, minX, minY, maxZ);

        data.addQuadInstruction(null, maxX, minY, minZ);
        data.addQuadInstruction(null, maxX, minY, maxZ);
        data.addQuadInstruction(null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, maxX, maxY, minZ);

        data.addQuadInstruction(null, minX, maxY, minZ);
        data.addQuadInstruction(null, minX, maxY, maxZ);
        data.addQuadInstruction(null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, maxX, maxY, minZ);

        data.addQuadInstruction(null, minX, maxY, minZ);
        data.addQuadInstruction(null, maxX, maxY, minZ);
        data.addQuadInstruction(null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, minX, maxY, maxZ);
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
    public ArchitectureModelData.ModelDataQuads getQuads(BlockState state, LevelAccessor world, BlockPos pos) {
        this.convertedModelData.setState(state);
        TextureAtlasSprite[] textures = this.getTextures(world, pos);
        int[] colours = this.getColours(world, pos);
        for (int i = 0; i < this.textureQuadMap.length; i++) {
            int[] quads = this.textureQuadMap[i];
            TextureAtlasSprite texture = textures[Math.min(i, textures.length - 1)];
            int colour = colours[i];

            for (int quad : quads) {
                this.convertedModelData.setFaceData(quad, null, texture, colour);
            }
        }

        return this.convertedModelData.buildModel();
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
    }

    @Override
    public List<BakedQuad> getDefaultModel() {
        return Collections.emptyList();
    }

    public abstract TextureAtlasSprite[] getTextures(LevelAccessor world, BlockPos pos);

    public abstract int[] getColours(LevelAccessor world, BlockPos pos);
}
