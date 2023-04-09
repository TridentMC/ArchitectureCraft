package com.tridevmc.architecture.legacy.client.render.model.objson;

import com.mojang.math.Transformation;
import com.tridevmc.architecture.legacy.client.render.model.IArchitectureModel;
import com.tridevmc.architecture.legacy.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.legacy.client.render.model.data.ArchitectureModelDataQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class OBJSONModel implements IArchitectureModel<OBJSONQuadMetadata> {

    private final LegacyOBJSON objson;
    private final ArchitectureModelData<OBJSONQuadMetadata> convertedModelData;
    private final boolean generateUVs;
    private final boolean generateNormals;

    public OBJSONModel(LegacyOBJSON objson, boolean generateUVs, boolean generateNormals) {
        this.objson = objson;
        this.convertedModelData = new ArchitectureModelData<>();
        this.generateUVs = generateUVs;
        this.generateNormals = generateNormals;
        List<Tuple<Integer, LegacyOBJSON.Face>> mappedFaces = IntStream.range(0, this.objson.getFaces().length).mapToObj(i -> new Tuple<>(i, this.objson.getFaces()[i])).collect(Collectors.toList());
        mappedFaces = mappedFaces.stream().sorted(Comparator.comparingInt(o -> o.getB().texture)).collect(Collectors.toList());
        int minTexture = mappedFaces.get(0).getB().texture;
        mappedFaces.forEach(mF -> mF.getB().texture = mF.getB().texture - minTexture);

        for (Tuple<Integer, LegacyOBJSON.Face> indexedFace : mappedFaces) {
            int faceIndex = indexedFace.getA();
            LegacyOBJSON.Face face = indexedFace.getB();
            for (LegacyOBJSON.Triangle tri : face.triangles) {
                this.addTri(this.convertedModelData, faceIndex, face.texture, 0, tri, face.vertices);
            }
        }

        //for (AABB bb : OBJSONVoxelizer.voxelize(objson, 16)) {
        //    this.makeCuboid(convertedModelData, bb);
        //}
    }

    private void makeCuboid(ArchitectureModelData data, AABB bb) {
        var meta = new OBJSONQuadMetadata(0, -1);
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

    private void addTri(ArchitectureModelData modelData, int face, int texture, int colour, LegacyOBJSON.Triangle tri, LegacyOBJSON.Vertex[] vertices) {
        for (int i = 0; i < 3; i++) {
            int vertexIndex = tri.vertices[i];
            LegacyOBJSON.Vertex vertex = vertices[vertexIndex];
            var metadata = new OBJSONQuadMetadata(texture, colour);
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
    public ArchitectureModelDataQuads getQuads(LevelAccessor level, BlockPos pos, BlockState state, Transformation transform) {
        return this.convertedModelData.getQuadsFor(this.getMetadataResolver(level, pos, state), transform);
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
