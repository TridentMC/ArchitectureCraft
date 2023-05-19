package com.tridevmc.architecture.legacy.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;
import com.tridevmc.architecture.legacy.client.render.model.builder.LegacyBakedQuadBuilderVertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.IntStream;

/**
 * An implementation of {@link IPipedBakedQuad} that allows for the creation of quads from the stored triangle data.
 *
 * @param <T> The type of metadata this provider uses.
 */
@Deprecated
public class LegacyArchitectureTri<T> extends LegacyBakedQuadProvider<T> {

    private Direction face;
    private final LegacyArchitectureVertex[] vertices = new LegacyArchitectureVertex[3];
    private Vector3f normals = null;

    public LegacyArchitectureTri(T metadata, Direction face) {
        super(metadata);
        this.face = face;
    }

    public LegacyArchitectureTri(T metadata, Direction face, Vector3f normals) {
        super(metadata);
        this.face = face;
        this.normals = normals;
    }

    public BakedQuad bake(Transformation transform, Direction facing, TextureAtlasSprite sprite, int colour) {
        if (facing == null) facing = this.facing();
        var builder = new LegacyBakedQuadBuilderVertexConsumer()
                .setSprite(sprite)
                .setTintIndex(colour)
                .setShade(true)
                .setDirection(facing);
        int[] vertexIndices = new int[]{0, 0, 1, 2};
        for (int i = 0; i < 4; i++) {
            var vertex = this.vertices[vertexIndices[i]];
            vertex.pipe(builder, this, Optional.of(transform), sprite, colour);
        }
        return builder.getBakedQuad();
    }

    public Vector3f getFaceNormal() {
        if (this.normals == null) {
            var vert0 = this.vertices[2];
            var vert1 = this.vertices[1];
            var vert2 = this.vertices[0];
            Vector3f p0 = vert0.getPosition();

            p0.sub(vert1.getPosition());
            Vector3f p1 = vert2.getPosition();
            p1.sub(vert1.getPosition());
            p0.cross(p1);

            this.normals = new Vector3f(p0);
            this.normals.normalize();
        }
        return this.normals;
    }

    public @NotNull Direction facing() {
        if (this.face == null) {
            Vector3f normals = this.getFaceNormal();
            this.face = Direction.getNearest(normals.x(), normals.y(), normals.z());
        }
        return this.face;
    }

    public LegacyArchitectureVertex[] getVertices() {
        return this.vertices;
    }

    public boolean isComplete() {
        return Arrays.stream(this.vertices).allMatch(Objects::nonNull);
    }

    public void setVertex(int index, LegacyArchitectureVertex vertex) {
        this.vertices[index] = vertex;
    }

    public void assignNormals() {
        var vert0 = this.vertices[2];
        var vert1 = this.vertices[1];
        var vert2 = this.vertices[0];

        Vector3f p0 = vert0.getPosition();
        p0.sub(vert1.getPosition());
        Vector3f p1 = vert2.getPosition();
        p1.sub(vert1.getPosition());

        p0.cross(p1);
        Vector3f normals;
        if (vert0.assignNormals()) {
            normals = new Vector3f(p0);
            normals.add(vert0.getNormals());
            vert0.setNormals(normals);
        }
        if (vert1.assignNormals()) {
            normals = new Vector3f(p0);
            normals.add(vert1.getNormals());
            vert1.setNormals(normals);
        }
        if (vert2.assignNormals()) {
            normals = new Vector3f(p0);
            normals.add(vert2.getNormals());
            vert2.setNormals(normals);
        }
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

    public int getNextVertex() {
        return IntStream.range(0, 3).filter((i) -> this.vertices[i] == null).min().orElse(-1);
    }

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

}
