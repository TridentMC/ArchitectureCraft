package com.tridevmc.architecture.client.render.model.data;


import com.mojang.math.Transformation;

import java.util.stream.IntStream;

/**
 * Stores information about a vertex that can be piped into a vertex consumer.
 * <p>
 * Capable of generating UVs, and allows the parent model data to assign normal values.
 */
public class SmartArchitectureVertex extends ArchitectureVertex {

    private final boolean generateUVs;
    private final boolean assignNormals;

    private SmartArchitectureVertex(int face, float[] data, float[] uvs, float[] normals, boolean generateUVs, boolean assignNormals) {
        super(face, data, uvs, normals);
        this.generateUVs = generateUVs;
        this.assignNormals = assignNormals;
    }

    public static SmartArchitectureVertex fromPosition(int face, float[] data) {
        return new SmartArchitectureVertex(face, data, new float[]{0F, 0F}, new float[]{0F, 0F, 0F}, true, true);
    }

    public static SmartArchitectureVertex fromPositionWithUV(int face, float[] data, float[] uvs) {
        return new SmartArchitectureVertex(face, data, uvs, new float[]{0F, 0F, 0F}, false, true);
    }

    public static SmartArchitectureVertex fromPositionWithNormal(int face, float[] data, float[] normals) {
        return new SmartArchitectureVertex(face, data, new float[]{0F, 0F}, normals, true, false);
    }

    @Override
    public boolean assignNormals() {
        return this.assignNormals;
    }

    @Override
    public float[] getUVs(IBakedQuadProvider bakedQuadProvider, Transformation transform) {
        if (!this.generateUVs) {
            return super.getUVs(bakedQuadProvider, transform);
        }

        var ranges = bakedQuadProvider.getRanges(transform);

        var pos = this.getPosition(transform);
        var posData = new float[]{pos.x(), pos.y(), pos.z()};
        IntStream.range(0, 3).filter(i -> ranges[i][0] == ranges[i][1]).forEach(i -> {
            posData[i] = posData[i] - ranges[i][1];
        });
        pos.set(posData);
        pos.map((v) -> v > 1 ? v % 1 : v);
        var face = this.rotate(bakedQuadProvider.getFace(), transform);
        float u = 0, v = 0;
        switch (face) {
            case DOWN -> {
                u = pos.x() * 16F;
                v = -16F * (pos.z() - 1F);
            }
            case UP -> {
                u = pos.x() * 16F;
                v = pos.z() * 16F;
            }
            case NORTH -> {
                u = -16F * (pos.x() - 1F);
                v = -16F * (pos.y() - 1F);
            }
            case SOUTH -> {
                u = 16F * pos.x();
                v = -16F * (pos.y() - 1F);
            }
            case WEST -> {
                u = 16F * pos.z();
                v = -16F * (pos.y() - 1F);
            }
            case EAST -> {
                u = -16F * (pos.z() - 1F);
                v = -16F * (pos.y() - 1F);
            }
        }
        return new float[]{u, v};
    }
}
