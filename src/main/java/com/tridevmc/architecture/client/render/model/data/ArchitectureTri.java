package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Sets;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.tridevmc.architecture.client.render.model.builder.BakedQuadBuilderVertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.*;
import java.util.stream.IntStream;

public class ArchitectureTri<T> extends BakedQuadProvider<T> {

    private Direction face;
    private final ArchitectureVertex[] vertices = new ArchitectureVertex[3];
    private Vector3f normals = null;

    public ArchitectureTri(T metadata, Direction face) {
        super(metadata);
        this.face = face;
    }

    public ArchitectureTri(T metadata, Direction face, Vector3f normals) {
        super(metadata);
        this.face = face;
        this.normals = normals;
    }

    @Override
    public BakedQuad bake(Transformation transform, Direction facing, TextureAtlasSprite sprite, int colour) {
        if (facing == null) facing = this.getFace();
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
        return builder.getBakedQuad();
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
            Vector3f normals = this.getFaceNormal();
            this.face = Direction.getNearest(normals.x(), normals.y(), normals.z());
        }
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
    public int getNextVertex() {
        return IntStream.range(0, 3).filter((i) -> this.vertices[i] == null).min().orElse(-1);
    }
}
