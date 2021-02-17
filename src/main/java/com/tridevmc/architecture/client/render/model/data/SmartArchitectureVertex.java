package com.tridevmc.architecture.client.render.model.data;


import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;

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
    public float[] getUVs(IBakedQuadProvider bakedQuadProvider, TransformationMatrix transform) {
        if (!this.generateUVs) {
            return super.getUVs(bakedQuadProvider, transform);
        }

        int[][] ranges = bakedQuadProvider.getRanges(transform);

        Vector3f pos = this.getPosition(transform);
        float[] posData = new float[]{pos.getX(), pos.getY(), pos.getZ()};
        IntStream.range(0, 3).filter(i -> ranges[i][0] == ranges[i][1]).forEach(i -> {
            posData[i] = posData[i] - ranges[i][1];
        });
        pos.set(posData);
        pos.apply((v) -> v > 1 ? v % 1 : v);
        Direction face = this.rotate(bakedQuadProvider.getFace(), transform);
        float u = 0, v = 0;
        switch (face) {
            case DOWN:
                u = pos.getX() * 16F;
                v = -16F * (pos.getZ() - 1F);
                break;
            case UP:
                u = pos.getX() * 16F;
                v = pos.getZ() * 16F;
                break;
            case NORTH:
                u = -16F * (pos.getX() - 1F);
                v = -16F * (pos.getY() - 1F);
                break;
            case SOUTH:
                u = 16F * pos.getX();
                v = -16F * (pos.getY() - 1F);
                break;
            case WEST:
                u = 16F * pos.getZ();
                v = -16F * (pos.getY() - 1F);
                break;
            case EAST:
                u = -16F * (pos.getZ() - 1F);
                v = -16F * (pos.getY() - 1F);
                break;
        }
        return new float[]{u, v};
    }
}
