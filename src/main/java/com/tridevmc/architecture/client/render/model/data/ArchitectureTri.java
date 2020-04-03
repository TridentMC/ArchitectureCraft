package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadRetextured;
import com.tridevmc.architecture.client.render.model.builder.QuadBuilder;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;

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
            this.quadsForSprite.put(baseQuad.func_187508_a(), baseQuad);
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
            return new BakedQuad(quad.getVertexData(), newTint, quad.getFace(), quad.func_187508_a(),
                    quad.shouldApplyDiffuseLighting());
        }
    }

    @Override
    public BakedQuad bake(TransformationMatrix transform, Direction facing, TextureAtlasSprite sprite, int tintIndex) {
        PrebuiltData prebuiltData = this.prebuiltQuads.get(transform);
        this.recalculateFace();
        prebuiltData = null;
        if (prebuiltData == null) {
            if (facing == null) facing = this.recalculateFace();
            QuadBuilder builder = new QuadBuilder(transform, facing);
            for (int i = 0; i < 4; i++) {
                ArchitectureVertex vertex = this.vertices[i == 0 ? 0 : i - 1];
                float[] UVs = vertex.getUVs(transform);
                float u = sprite.getInterpolatedU(UVs[0]), v = sprite.getInterpolatedV(UVs[1]);
                builder.putVertex(vertex.getX(), vertex.getY(), vertex.getZ(), u, v,
                        vertex.getNormalX(), vertex.getNormalY(), vertex.getNormalZ());
            }
            PrebuiltData baseQuad = new PrebuiltData(builder.build(sprite, tintIndex));
            this.prebuiltQuads.put(transform, baseQuad);
            return baseQuad.baseQuad;
        } else {
            return prebuiltData.getQuad(sprite, tintIndex);
        }
    }

    @Override
    public Vector3f getNormals() {
        if (this.normals == null) {
            this.normals = new Vector3f(0, 0, 0);

            for (int i = 0; i < this.vertices.length; i++) {
                ArchitectureVertex currentVertex = this.vertices[i];
                ArchitectureVertex neighbourVertex = this.vertices[(i + 1) % this.vertices.length];

                this.normals.setX(this.normals.getX() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ())));
                this.normals.setY(this.normals.getY() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX())));
                this.normals.setZ(this.normals.getZ() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY())));
            }

            this.normals.normalize();
            Vec3i directionVec = Direction.getFacingFromVector(this.normals.getX(), this.normals.getY(), this.normals.getZ()).getDirectionVec();
            this.normals = new Vector3f(directionVec.getX(), directionVec.getY(), directionVec.getZ());
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
        Vector3f normals = this.getNormals();
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
    public void setVertex(int index, float[] data) {
        this.vertices[index] = new SmartArchitectureVertex(this, data);
    }

    @Override
    public void setVertex(int index, float[] data, float[] uvs) {
        this.vertices[index] = new ArchitectureVertex(this, data, uvs);
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
