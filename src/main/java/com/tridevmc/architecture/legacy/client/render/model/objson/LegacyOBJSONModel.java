package com.tridevmc.architecture.legacy.client.render.model.objson;

import com.mojang.math.Transformation;
import com.tridevmc.architecture.legacy.client.render.model.LegacyIArchitectureModel;
import com.tridevmc.architecture.legacy.client.render.model.data.LegacyArchitectureModelData;
import com.tridevmc.architecture.legacy.client.render.model.data.LegacyArchitectureModelDataQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Deprecated
public abstract class LegacyOBJSONModel implements LegacyIArchitectureModel<LegacyOBJSONQuadMetadata> {

    private final LegacyOBJSON objson;
    private final LegacyArchitectureModelData<LegacyOBJSONQuadMetadata> convertedModelData;
    private final boolean generateUVs;
    private final boolean generateNormals;

    public LegacyOBJSONModel(LegacyOBJSON objson, boolean generateUVs, boolean generateNormals) {
        this.objson = objson;
        this.convertedModelData = new LegacyArchitectureModelData<>();
        this.generateUVs = generateUVs;
        this.generateNormals = generateNormals;
        List<Pair<Integer, LegacyOBJSON.Face>> mappedFaces = IntStream.range(0, this.objson.getFaces().length).mapToObj(i -> Pair.of(i, this.objson.getFaces()[i])).collect(Collectors.toList());
        mappedFaces = mappedFaces.stream().sorted(Comparator.comparingInt(o -> o.getSecond().texture)).collect(Collectors.toList());
        int minTexture = mappedFaces.get(0).getSecond().texture;
        mappedFaces.forEach(mF -> mF.getSecond().texture = mF.getSecond().texture - minTexture);

        for (Pair<Integer, LegacyOBJSON.Face> indexedFace : mappedFaces) {
            int faceIndex = indexedFace.getFirst();
            LegacyOBJSON.Face face = indexedFace.getSecond();
            for (LegacyOBJSON.Triangle tri : face.triangles) {
                this.addTri(this.convertedModelData, faceIndex, face.texture, 0, tri, face.vertices);
            }
        }

        //for (AABB bb : OBJSONVoxelizer.voxelize(objson, 16)) {
        //    this.makeCuboid(convertedModelData, bb);
        //}
    }

    private void makeCuboid(LegacyArchitectureModelData data, AABB bb) {
        var meta = new LegacyOBJSONQuadMetadata(0, -1);
        float minX = (float) bb.minX;
        float minY = (float) bb.minY;
        float minZ = (float) bb.minZ;
        float maxX = (float) bb.maxX;
        float maxY = (float) bb.maxY;
        float maxZ = (float) bb.maxZ;

        data.addQuadInstruction(meta, Direction.NORTH, minX, minY, maxZ);
        data.addQuadInstruction(meta, Direction.NORTH, minX, maxY, maxZ);
        data.addQuadInstruction(meta, Direction.NORTH, minX, maxY, minZ);
        data.addQuadInstruction(meta, Direction.NORTH, minX, minY, minZ);

        data.addQuadInstruction(meta, Direction.SOUTH, maxX, minY, minZ);
        data.addQuadInstruction(meta, Direction.SOUTH, maxX, maxY, minZ);
        data.addQuadInstruction(meta, Direction.SOUTH, maxX, maxY, maxZ);
        data.addQuadInstruction(meta, Direction.SOUTH, maxX, minY, maxZ);

        data.addQuadInstruction(meta, Direction.WEST, minX, minY, minZ);
        data.addQuadInstruction(meta, Direction.WEST, minX, maxY, minZ);
        data.addQuadInstruction(meta, Direction.WEST, maxX, maxY, minZ);
        data.addQuadInstruction(meta, Direction.WEST, maxX, minY, minZ);

        data.addQuadInstruction(meta, Direction.EAST, maxX, minY, maxZ);
        data.addQuadInstruction(meta, Direction.EAST, maxX, maxY, maxZ);
        data.addQuadInstruction(meta, Direction.EAST, minX, maxY, maxZ);
        data.addQuadInstruction(meta, Direction.EAST, minX, minY, maxZ);

        data.addQuadInstruction(meta, Direction.DOWN, maxX, minY, minZ);
        data.addQuadInstruction(meta, Direction.DOWN, maxX, minY, maxZ);
        data.addQuadInstruction(meta, Direction.DOWN, minX, minY, maxZ);
        data.addQuadInstruction(meta, Direction.DOWN, minX, minY, minZ);

        data.addQuadInstruction(meta, Direction.UP, minX, maxY, minZ);
        data.addQuadInstruction(meta, Direction.UP, minX, maxY, maxZ);
        data.addQuadInstruction(meta, Direction.UP, maxX, maxY, maxZ);
        data.addQuadInstruction(meta, Direction.UP, maxX, maxY, minZ);
    }

    private void addTri(LegacyArchitectureModelData modelData, int face, int texture, int colour, LegacyOBJSON.Triangle tri, LegacyOBJSON.Vertex[] vertices) {
        for (int i = 0; i < 3; i++) {
            int vertexIndex = tri.vertices[i];
            LegacyOBJSON.Vertex vertex = vertices[vertexIndex];
            var metadata = new LegacyOBJSONQuadMetadata(texture, colour);
            if (this.generateUVs && this.generateNormals) {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z());
            } else if (this.generateUVs) {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z(), vertex.getNormal().x(), vertex.getNormal().y(), vertex.getNormal().z());
            } else if (this.generateNormals) {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z(), vertex.getU() * 16, vertex.getV() * 16);
            } else {
                modelData.addTriInstruction(metadata, face, null, vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z(), vertex.getU() * 16, vertex.getV() * 16, vertex.getNormal().x(), vertex.getNormal().y(), vertex.getNormal().z());
            }
        }
    }

    @Override
    public LegacyArchitectureModelDataQuads getQuads(LevelAccessor level, BlockPos pos, BlockState state, Transformation transform) {
        return this.convertedModelData.getQuadsFor(this.getMetadataResolver(level, pos, state), transform);
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).missingSprite();
    }

    @Override
    public List<BakedQuad> getDefaultModel() {
        return Collections.emptyList();
    }

}
