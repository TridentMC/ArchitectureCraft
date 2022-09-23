package com.tridevmc.architecture.client.render.model.objson;

import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.IArchitectureModel;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelDataQuads;
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

public abstract class OBJSONModel implements IArchitectureModel<OBJSONQuadMetadata> {

    private final OBJSON objson;
    private final ArchitectureModelData<OBJSONQuadMetadata> convertedModelData;
    private final boolean generateUVs;
    private final boolean generateNormals;

    public OBJSONModel(OBJSON objson, boolean generateUVs, boolean generateNormals) {
        this.objson = objson;
        this.convertedModelData = new ArchitectureModelData<>();
        this.generateUVs = generateUVs;
        this.generateNormals = generateNormals;
        List<Tuple<Integer, OBJSON.Face>> mappedFaces = IntStream.range(0, this.objson.getFaces().length).mapToObj(i -> new Tuple<>(i, this.objson.getFaces()[i])).collect(Collectors.toList());
        mappedFaces = mappedFaces.stream().sorted(Comparator.comparingInt(o -> o.getB().texture)).collect(Collectors.toList());
        int minTexture = mappedFaces.get(0).getB().texture;
        mappedFaces.forEach(mF -> mF.getB().texture = mF.getB().texture - minTexture);

        for (Tuple<Integer, OBJSON.Face> indexedFace : mappedFaces) {
            int faceIndex = indexedFace.getA();
            OBJSON.Face face = indexedFace.getB();
            for (OBJSON.Triangle tri : face.triangles) {
                this.addTri(this.convertedModelData, faceIndex, face.texture, tri, face.vertices);
            }
        }

        //for (AxisAlignedBB bb : objson.getVoxelized()) {
        //    this.makeCuboid(convertedModelData, bb);
        //}
    }

    private void makeCuboid(ArchitectureModelData data, AABB bb) {
        float minX = (float) bb.minX;
        float minY = (float) bb.minY;
        float minZ = (float) bb.minZ;
        float maxX = (float) bb.maxX;
        float maxY = (float) bb.maxY;
        float maxZ = (float) bb.maxZ;
        data.addQuadInstruction(null, null, minX, minY, minZ);
        data.addQuadInstruction(null, null, minX, maxY, minZ);
        data.addQuadInstruction(null, null, maxX, maxY, minZ);
        data.addQuadInstruction(null, null, maxX, minY, minZ);

        data.addQuadInstruction(null, null, minX, minY, maxZ);
        data.addQuadInstruction(null, null, maxX, minY, maxZ);
        data.addQuadInstruction(null, null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, null, minX, maxY, maxZ);

        data.addQuadInstruction(null, null, minX, minY, minZ);
        data.addQuadInstruction(null, null, minX, maxY, minZ);
        data.addQuadInstruction(null, null, minX, maxY, maxZ);
        data.addQuadInstruction(null, null, minX, minY, maxZ);

        data.addQuadInstruction(null, null, maxX, minY, minZ);
        data.addQuadInstruction(null, null, maxX, minY, maxZ);
        data.addQuadInstruction(null, null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, null, maxX, maxY, minZ);

        data.addQuadInstruction(null, null, minX, maxY, minZ);
        data.addQuadInstruction(null, null, minX, maxY, maxZ);
        data.addQuadInstruction(null, null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, null, maxX, maxY, minZ);

        data.addQuadInstruction(null, null, minX, maxY, minZ);
        data.addQuadInstruction(null, null, maxX, maxY, minZ);
        data.addQuadInstruction(null, null, maxX, maxY, maxZ);
        data.addQuadInstruction(null, null, minX, maxY, maxZ);
    }

    private void addTri(ArchitectureModelData modelData, int face, int texture, OBJSON.Triangle tri, OBJSON.Vertex[] vertices) {
        for (int i = 0; i < 3; i++) {
            int vertexIndex = tri.vertices[i];
            OBJSON.Vertex vertex = vertices[vertexIndex];
            var metadata = new OBJSONQuadMetadata(texture, 0);
            if (this.generateUVs && this.generateNormals) {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z);
            } else if (this.generateUVs) {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z, vertex.getNormal().x, vertex.getNormal().y, vertex.getNormal().z);
            } else if (this.generateNormals) {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z, vertex.getU() * 16, vertex.getV() * 16);
            } else {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x, vertex.getPos().y, vertex.getPos().z, vertex.getU() * 16, vertex.getV() * 16, vertex.getNormal().x, vertex.getNormal().y, vertex.getNormal().z);
            }
        }
    }

    private <T> T addOrGet(ArrayList<T> list, int index, T element) {
        if (index >= list.size()) {
            list.add(index, element);
        }
        return list.get(index);
    }

    // TODO: FIXME!!! - Re-evaluate how we assign textures to model datas based on OBJSON. Something is fucked

    @Override
    public ArchitectureModelDataQuads getQuads(LevelAccessor level, BlockPos pos, BlockState state) {
        return this.convertedModelData.getQuadsFor(this.getMetadataResolver(level, pos, state), Transformation.identity());
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
    }

    @Override
    public List<BakedQuad> getDefaultModel() {
        return Collections.emptyList();
    }

}
