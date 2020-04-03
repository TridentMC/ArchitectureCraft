package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.baked.BakedQuadRetextured;
import com.tridevmc.architecture.client.render.model.builder.QuadBuilder;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple representation of the data required to bake a quad.
 */
public class ArchitectureQuad implements IBakedQuadProvider {

    private Direction face;
    private ArchitectureVertex[] vertices = new ArchitectureVertex[4];
    private Vector3f normals = null;
    private Map<TransformationMatrix, PrebuiltData> prebuiltQuads = Maps.newHashMap();

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
        if (prebuiltData == null) {
            if (facing == null) facing = this.recalculateFace();
            QuadBuilder builder = new QuadBuilder(transform, facing);
            for (ArchitectureVertex vertex : this.getVertices()) {
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
     * Sets the vertex at the given index to the data provided.
     *
     * @param index the index the vertex data should be added to.
     * @param data  the vertex data to add to the specified index.
     */
    @Override
    public void setVertex(int index, float[] data) {
        this.vertices[index] = new SmartArchitectureVertex(this, data);
    }

    /**
     * Sets the vertex at the given index to the data provided.
     *
     * @param index the index the vertex data should be added to.
     * @param data  the vertex data to add to the specified index.
     * @param uvs   the uv data to add to the specified index.
     */
    @Override
    public void setVertex(int index, float[] data, float[] uvs) {
        this.vertices[index] = new ArchitectureVertex(this, data, uvs);
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

    /**
     * Determines the normals for this quad then caches the result for later use.
     *
     * @return the normals for this quad.
     */
    @Override
    public Vector3f getNormals() {
        if (this.normals == null) {
            // Generate normals
            ArchitectureVertex[] vertices = this.getVertices();

            Vector3f vPrev = vertices[3].getPosition();
            Vector3f vCur = vertices[0].getPosition();
            Vector3f vNext = vertices[1].getPosition();

            if (vPrev.equals(vCur)) {
                vPrev = vertices[2].getPosition();
            }
            if (vNext.equals(vCur)) {
                vNext = vertices[2].getPosition();
            }

            vPrev.sub(vCur);
            vNext.sub(vCur);

            this.normals = vNext.copy();
            vNext.cross(vPrev);

        }
        return this.normals;
    }

    @Override
    public int[][] getRanges(TransformationMatrix transform) {
        int[][] ranges = new int[3][];
        List<Vector3f> vertices = Arrays.stream(this.getVertices()).map(v -> v.getPosition(transform)).collect(Collectors.toList());
        List<Integer> xDimensions = vertices.stream().map((v) -> (int) v.getX()).collect(Collectors.toList());
        ranges[0] = new int[]{xDimensions.stream().min(Comparator.comparingInt(Integer::intValue)).get(),
                xDimensions.stream().max(Comparator.comparingInt(Integer::intValue)).get()};

        List<Integer> yDimensions = vertices.stream().map((v) -> (int) v.getY()).collect(Collectors.toList());
        ranges[1] = new int[]{yDimensions.stream().min(Comparator.comparingInt(Integer::intValue)).get(),
                yDimensions.stream().max(Comparator.comparingInt(Integer::intValue)).get()};

        List<Integer> zDimensions = vertices.stream().map((v) -> (int) v.getZ()).collect(Collectors.toList());
        ranges[2] = new int[]{zDimensions.stream().min(Comparator.comparingInt(Integer::intValue)).get(),
                zDimensions.stream().max(Comparator.comparingInt(Integer::intValue)).get()};

        return ranges;
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
        Vector3f normals = this.getNormals();
        this.face = Direction.getFacingFromVector(normals.getX(), normals.getY(), normals.getZ());
        return this.face;
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
}
