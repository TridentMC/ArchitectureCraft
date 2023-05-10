package com.tridevmc.architecture.legacy.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;
import com.tridevmc.architecture.legacy.client.render.model.builder.BakedQuadBuilderVertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

/**
 * An implementation of {@link IPipedBakedQuad} that allows for the creation of quads from the stored quad data.
 *
 * @param <T> The type of metadata this provider uses.
 */
public class LegacyArchitectureQuad<T> extends BakedQuadProvider<T> {

    private Direction face;
    private final LegacyArchitectureVertex[] vertices = new LegacyArchitectureVertex[4];
    private Vector3f normals;
    private final Map<Transformation, PrebuiltData> prebuiltQuads = Maps.newHashMap();

    @Override
    public ImmutableList vertices() {
        return null;
    }

    @Override
    public float nX() {
        return 0;
    }

    @Override
    public float nY() {
        return 0;
    }

    @Override
    public float nZ() {
        return 0;
    }

    @Override
    public @NotNull Direction face() {
        return null;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public float minX() {
        return 0;
    }

    @Override
    public float minY() {
        return 0;
    }

    @Override
    public float minZ() {
        return 0;
    }

    @Override
    public float maxX() {
        return 0;
    }

    @Override
    public float maxY() {
        return 0;
    }

    @Override
    public float maxZ() {
        return 0;
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
            if (quadOut == null) {
                //quadOut = new BakedQuadRetextured(this.baseQuad, sprite);
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

    public LegacyArchitectureQuad(T metadata, Direction face) {
        super(metadata);
        this.face = face;
    }

    public LegacyArchitectureQuad(T metadata, Direction face, Vector3f normals) {
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
     * @param colour    the tintIndex index to apply to the quad.
     * @return a baked quad matching all the data provided.
     */
    public BakedQuad bake(Transformation transform, Direction facing, TextureAtlasSprite sprite, int colour) {
        PrebuiltData prebuiltData = this.prebuiltQuads.get(transform);
        this.recalculateFace();
        if (prebuiltData == null) {
            if (facing == null) facing = this.recalculateFace();
            var builder = new BakedQuadBuilderVertexConsumer()
                    .setSprite(sprite)
                    .setTintIndex(colour)
                    .setShade(true)
                    .setHasAmbientOcclusion(true)
                    .setDirection(facing);
            int[] vertexIndices = new int[]{0, 1, 2, 3};
            for (int i = 0; i < 4; i++) {
                LegacyArchitectureVertex vertex = this.vertices[vertexIndices[i]];
                vertex.pipe(builder, this, Optional.of(transform), sprite, colour);
            }
            PrebuiltData baseQuad = new PrebuiltData(builder.getBakedQuad());
            this.prebuiltQuads.put(transform, baseQuad);
            return baseQuad.baseQuad;
        } else {
            return prebuiltData.getQuad(sprite, colour);
        }
    }

    public Vector3f getFaceNormal() {
        if (this.normals == null) {
            this.normals = new Vector3f(0, 0, 0);

            for (int i = 0; i < this.vertices.length; i++) {
                LegacyArchitectureVertex currentVertex = this.vertices[i];
                LegacyArchitectureVertex neighbourVertex = this.vertices[(i + 1) % this.vertices.length];

                this.normals.set((this.normals.x() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ()))),
                                 (this.normals.y() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX()))),
                                 (this.normals.z() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY()))));
            }

            this.normals.normalize();
        }
        return this.normals;
    }

    public void assignNormals() {
        for (int i = 0; i < this.vertices.length; i++) {
            LegacyArchitectureVertex currentVertex = this.vertices[i];
            if (!currentVertex.assignNormals())
                continue;

            Vector3f normals = currentVertex.getNormals();
            LegacyArchitectureVertex neighbourVertex = this.vertices[(i + 1) % this.vertices.length];
            normals.set((normals.x() + ((currentVertex.getY() - neighbourVertex.getY()) * (currentVertex.getZ() + neighbourVertex.getZ()))),
                        (normals.y() + ((currentVertex.getZ() - neighbourVertex.getZ()) * (currentVertex.getX() + neighbourVertex.getX()))),
                        (normals.z() + ((currentVertex.getX() - neighbourVertex.getX()) * (currentVertex.getY() + neighbourVertex.getY()))));
        }
    }

    /**
     * Determines the next empty slot that a vertex can be added to.
     *
     * @return the index of the next empty slot a vertex can be added to.
     * @throws IllegalArgumentException if no slots are available for a vertex to be added to.
     */
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
    public @NotNull Direction facing() {
        return this.face;
    }

    public int[][] getRanges(Transformation transform) {
        int[][] ranges = new int[3][2];
        Set<Integer> xDimensions = Sets.newHashSet();
        Set<Integer> yDimensions = Sets.newHashSet();
        Set<Integer> zDimensions = Sets.newHashSet();
        Set<Integer>[] dimensions = new Set[]{xDimensions, yDimensions, zDimensions};
        for (int i = 0; i < 3; i++) {
            Set<Integer> targetDimensions = dimensions[i];
            int[] targetRange = ranges[i];
            for (LegacyArchitectureVertex vertex : this.getVertices()) {
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

    public void setVertex(int index, LegacyArchitectureVertex vertex) {
        this.vertices[index] = vertex;
    }

    /**
     * Determines if the quad contains all the vertices required to be baked.
     *
     * @return true if the quad has all the required vertices, false otherwise.
     */
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
    public LegacyArchitectureVertex[] getVertices() {
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
