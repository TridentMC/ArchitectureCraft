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

/**
 * A simple representation of the data required to bake a quad.
 */
public class ArchitectureQuad implements IBakedQuadProvider {

    private Direction face;
    private ArchitectureVertex[] vertices = new ArchitectureVertex[4];
    private Vector3f normals;
    private Map<TransformationMatrix, PrebuiltData> prebuiltQuads = Maps.newHashMap();

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

    public ArchitectureQuad(Direction face) {
        this.face = face;
    }

    public ArchitectureQuad(Direction face, Vector3f normals) {
        this.face = face;
        this.normals = normals;
    }

    /**
     * Converts the quad into a baked quad with the given data then caches it for later use.
     *
     * @param transform the transform to apply to the quad.
     * @param facing    the face the quad will occupy on a model.
     * @param sprite    the sprite to apply to the quad.
     * @param tintIndex the tint index to apply to the quad.
     * @return a baked quad matching all the data provided.
     */
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
            this.normals = new Vector3f(0, 0, 0);

            for (int i = 0; i < this.vertices.length; i++) {
                ArchitectureVertex currentVertex = this.vertices[i];
                ArchitectureVertex neighbourVertex = this.vertices[(i + 1) % this.vertices.length];

                this.normals.setX(this.normals.getX() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ())));
                this.normals.setY(this.normals.getY() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX())));
                this.normals.setZ(this.normals.getZ() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY())));
            }

            this.normals.normalize();
        }
        return this.normals;
    }

    @Override
    public void assignNormals() {
        for (int i = 0; i < this.vertices.length; i++) {
            ArchitectureVertex currentVertex = this.vertices[i];
            if (!currentVertex.assignNormals())
                continue;

            Vector3f normals = currentVertex.getNormals();
            ArchitectureVertex neighbourVertex = this.vertices[(i + 1) % this.vertices.length];
            normals.setX(normals.getX() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ())));
            normals.setY(normals.getY() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX())));
            normals.setZ(normals.getZ() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY())));
        }
    }

    /**
     * Determines the next empty slot that a vertex can be added to.
     *
     * @return the index of the next empty slot a vertex can be added to.
     * @throws IllegalArgumentException if no slots are available for a vertex to be added to.
     */
    @Override
    public int getNextVertex() {
        for (int i = 0; i < this.vertices.length; i++) {
            if (this.vertices[i] == null)
                return i;
        }
        throw new IllegalArgumentException("Unable to determine next empty vertex on the given quad.");
    }

    /**
     * Determines the default face this quad occupies when no transform is applied.
     *
     * @return the default face for this quad.
     */
    @Override
    public Direction getFace() {
        return this.face;
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
    public void setVertex(int index, ArchitectureVertex vertex) {
        this.vertices[index] = vertex;
    }

    /**
     * Determines if the quad contains all the vertices required to be baked.
     *
     * @return true if the quad has all the required vertices, false otherwise.
     */
    @Override
    public boolean isComplete() {
        return Arrays.stream(this.vertices).allMatch(Objects::nonNull);
    }

    /**
     * Sets the normals for this quad to the data specified.
     *
     * @param normals the new normals to use for this quad.
     */
    public void setNormals(Vector3f normals) {
        this.normals = normals;
    }

    /**
     * Gets an array of all the vertices that make up this quad.
     *
     * @return an array of all the vertices that make up this quad.
     */
    public ArchitectureVertex[] getVertices() {
        return this.vertices;
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

}
