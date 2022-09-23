package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadRetextured;
import com.tridevmc.architecture.client.render.model.builder.BakedQuadBuilderVertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.*;

/**
 * A simple representation of the data required to bake a quad.
 */
public class ArchitectureQuad<T> extends BakedQuadProvider<T> {

    private Direction face;
    private ArchitectureVertex[] vertices = new ArchitectureVertex[4];
    private Vector3f normals;
    private Map<Transformation, PrebuiltData> prebuiltQuads = Maps.newHashMap();

    private static class PrebuiltData {
        BakedQuad baseQuad;
        Map<TextureAtlasSprite, BakedQuad> quadsForSprite = Maps.newHashMap();

        private PrebuiltData(BakedQuad baseQuad) {
            this.baseQuad = baseQuad;
            this.quadsForSprite.put(baseQuad.getSprite(), baseQuad);
        }

        private BakedQuad getQuad(TextureAtlasSprite sprite, int tint) {
            BakedQuad quadOut = this.quadsForSprite.get(sprite);
            if (quadOut == null) {
                quadOut = new BakedQuadRetextured(this.baseQuad, sprite);
                this.quadsForSprite.put(sprite, quadOut);
            }
            if (tint != -1) {
                quadOut = this.reTintQuad(quadOut, tint);
            }
            return quadOut;
        }

        private BakedQuad reTintQuad(BakedQuad quad, int newTint) {
            return new BakedQuad(quad.getVertices(), newTint, quad.getDirection(), quad.getSprite(),
                    quad.isShade());
        }
    }

    public ArchitectureQuad(T metadata, Direction face) {
        super(metadata);
        this.face = face;
    }

    public ArchitectureQuad(T metadata, Direction face, Vector3f normals) {
        super(metadata);
        this.face = face;
        this.normals = normals;
    }

    /**
     * Converts the quad into a baked quad with the given data then caches it for later use.
     *
     * @param transform the transform to apply to the quad.
     * @param facing    the face the quad will occupy on a model.
     * @param sprite    the sprite to apply to the quad.
     * @param colour the tint index to apply to the quad.
     * @return a baked quad matching all the data provided.
     */
    @Override
    public BakedQuad bake(Transformation transform, Direction facing, TextureAtlasSprite sprite, int colour) {
        PrebuiltData prebuiltData = this.prebuiltQuads.get(transform);
        this.recalculateFace();
        if (prebuiltData == null) {
            if (facing == null) facing = this.recalculateFace();
            var builder = new BakedQuadBuilderVertexConsumer();
            builder.setSprite(sprite);
            builder.setTintIndex(colour);
            builder.setShade(true);
            builder.setDirection(facing);
            int[] vertexIndices = new int[]{0, 0, 1, 2};
            for (int i = 0; i < 4; i++) {
                ArchitectureVertex vertex = this.vertices[vertexIndices[i]];
                vertex.pipe(builder, this, Optional.of(transform), sprite, colour);
            }
            PrebuiltData baseQuad = new PrebuiltData(builder.getBakedQuad());
            this.prebuiltQuads.put(transform, baseQuad);
            return baseQuad.baseQuad;
        } else {
            return prebuiltData.getQuad(sprite, colour);
        }
    }

    @Override
    public Vector3f getFaceNormal() {
        if (this.normals == null) {
            this.normals = new Vector3f(0, 0, 0);

            for (int i = 0; i < this.vertices.length; i++) {
                ArchitectureVertex currentVertex = this.vertices[i];
                ArchitectureVertex neighbourVertex = this.vertices[(i + 1) % this.vertices.length];

                this.normals.setX(this.normals.x() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ())));
                this.normals.setY(this.normals.y() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX())));
                this.normals.setZ(this.normals.z() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY())));
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
            normals.setX(normals.x() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ())));
            normals.setY(normals.y() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX())));
            normals.setZ(normals.z() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY())));
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
    public int[][] getRanges(Transformation transform) {
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
                float[] positionArray = new float[]{position.x(), position.y(), position.z()};
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
        this.face = Direction.getNearest(normals.x(), normals.y(), normals.z());
        return this.face;
    }

}
