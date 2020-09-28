package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadRetextured;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import java.util.*;
import java.util.stream.IntStream;

public class ArchitectureTri implements IBakedQuadProvider {

    private Direction face;
    private ArchitectureVertex[] vertices = new ArchitectureVertex[3];
    private Vector3f normals = null;
    private Map<TransformationMatrix, PrebuiltData> prebuiltQuads = Maps.newHashMap();

    public ArchitectureTri(Direction face) {
        this.face = face;
    }

    public ArchitectureTri(Direction face, Vector3f normals) {
        this.face = face;
        this.normals = normals;
    }

    private static class PrebuiltData {
        BakedQuad baseQuad;
        Map<TextureAtlasSprite, BakedQuad> quadsForSprite = Maps.newHashMap();

        private PrebuiltData(BakedQuad baseQuad) {
            this.baseQuad = baseQuad;
            this.quadsForSprite.put(baseQuad.getSprite(), baseQuad);
        }

        private BakedQuad getQuad(TextureAtlasSprite sprite, int tint) {
            BakedQuad quadOut = this.quadsForSprite.get(sprite);
            if (quadOut != null) {
                if (tint != -1) {
                    quadOut = this.reTintQuad(quadOut, tint);
                }
            } else {
                quadOut = new BakedQuadRetextured(this.baseQuad, sprite);
                this.quadsForSprite.put(sprite, quadOut);
                if (tint != -1) {
                    quadOut = this.reTintQuad(quadOut, tint);
                }
            }
            return quadOut;
        }

        private BakedQuad reTintQuad(BakedQuad quad, int newTint) {
            return new BakedQuad(quad.getVertexData(), newTint, quad.getFace(), quad.getSprite(),
                    quad.applyDiffuseLighting());
        }
    }

    @Override
    public BakedQuad bake(TransformationMatrix transform, Direction facing, TextureAtlasSprite sprite, int tintIndex) {
        PrebuiltData prebuiltData = this.prebuiltQuads.get(transform);
        this.recalculateFace();
        if (prebuiltData == null) {
            if (facing == null) facing = this.recalculateFace();
            BakedQuadBuilder builder = new BakedQuadBuilder();
            builder.setTexture(sprite);
            builder.setQuadTint(tintIndex);
            builder.setApplyDiffuseLighting(true);
            builder.setContractUVs(true);
            builder.setQuadOrientation(facing);

            int[] vertexIndices = new int[]{0, 0, 1, 2};
            for (int i = 0; i < 4; i++) {
                ArchitectureVertex vertex = this.vertices[vertexIndices[i]];
                vertex.pipe(builder, this, sprite, Optional.of(transform));
            }
            PrebuiltData baseQuad = new PrebuiltData(builder.build());
            this.prebuiltQuads.put(transform, baseQuad);
            return baseQuad.baseQuad;
        } else {
            return prebuiltData.getQuad(sprite, tintIndex);
        }
    }

    @Override
    public Vector3f getFaceNormal() {
        if (this.normals == null) {
            ArchitectureVertex vert0 = this.vertices[2];
            ArchitectureVertex vert1 = this.vertices[1];
            ArchitectureVertex vert2 = this.vertices[0];
            Vector3f p0 = vert0.getPosition();

            p0.sub(vert1.getPosition());
            Vector3f p1 = vert2.getPosition();
            p1.sub(vert1.getPosition());
            p0.cross(p1);

            this.normals = p0.copy();
            this.normals.normalize();
        }
        return this.normals;
    }

    @Override
    public Direction getFace() {
        if (this.face == null) {
            this.recalculateFace();
        }
        return this.face;
    }

    /**
     * Calculates the face of this quad based on its normals.
     *
     * @return the face the quad faces.
     */
    public Direction recalculateFace() {
        Vector3f normals = this.getFaceNormal();
        this.face = Direction.getFacingFromVector(normals.getX(), normals.getY(), normals.getZ());
        return this.face;
    }

    public ArchitectureVertex[] getVertices() {
        return this.vertices;
    }

    @Override
    public boolean isComplete() {
        return Arrays.stream(this.vertices).allMatch(Objects::nonNull);
    }

    @Override
    public void setVertex(int index, ArchitectureVertex vertex) {
        this.vertices[index] = vertex;
    }

    @Override
    public void assignNormals() {
        ArchitectureVertex vert0 = this.vertices[2];
        ArchitectureVertex vert1 = this.vertices[1];
        ArchitectureVertex vert2 = this.vertices[0];

        Vector3f p0 = vert0.getPosition();
        p0.sub(vert1.getPosition());
        Vector3f p1 = vert2.getPosition();
        p1.sub(vert1.getPosition());

        p0.cross(p1);
        Vector3f normals;
        if (vert0.assignNormals()) {
            normals = p0.copy();
            normals.add(vert0.getNormals());
            vert0.setNormals(normals);
        }
        if (vert1.assignNormals()) {
            normals = p0.copy();
            normals.add(vert1.getNormals());
            vert1.setNormals(normals);
        }
        if (vert2.assignNormals()) {
            normals = p0.copy();
            normals.add(vert2.getNormals());
            vert2.setNormals(normals);
        }
    }

    @Override
    public int[][] getRanges(TransformationMatrix transform) {
        int[][] ranges = new int[3][2];

        Set<Integer> xDimensions = Sets.newHashSet();
        Set<Integer> yDimensions = Sets.newHashSet();
        Set<Integer> zDimensions = Sets.newHashSet();
        Set<Integer>[] dimensions = new Set[]{xDimensions, yDimensions, zDimensions};

        for (int i = 0; i < 3; i++) {
            Set<Integer> targetDimensions = dimensions[i];
            int[] targetRange = ranges[i];
            for (ArchitectureVertex vertex : this.getVertices()) {
                Vector3f position = vertex.getPosition(transform);
                float[] positionArray = new float[]{position.getX(), position.getY(), position.getZ()};
                float coord = positionArray[i];
                int dimension = (int) coord;
                if (Math.abs(dimension - coord) > 0) {
                    targetDimensions.add(dimension);
                }
            }
            if (targetDimensions.isEmpty()) {
                targetDimensions.add(0);
            }

            targetRange[0] = targetDimensions.stream().min(Comparator.comparingInt(Integer::intValue)).get();
            targetRange[1] = targetDimensions.stream().max(Comparator.comparingInt(Integer::intValue)).get();
        }

        return ranges;
    }

    @Override
    public int getNextVertex() {
        return IntStream.range(0, 3).filter((i) -> this.vertices[i] == null).min().orElse(-1);
    }
}
