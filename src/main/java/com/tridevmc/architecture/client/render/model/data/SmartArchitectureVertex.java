package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

import java.util.stream.IntStream;

public class SmartArchitectureVertex extends ArchitectureVertex {
    public SmartArchitectureVertex(IBakedQuadProvider parent, float[] data) {
        super(parent, data, new float[]{0F, 0F});
    }

    @Override
    public float[] getUVs(TransformationMatrix transform) {
        int[][] ranges = this.getParent().getRanges(transform);

        Vector3f pos = this.getPosition(transform);
        float[] posData = new float[]{pos.getX(), pos.getY(), pos.getZ()};
        IntStream.range(0, 3).filter(i -> ranges[i][0] == ranges[i][1]).forEach(i -> {
            posData[i] = posData[i] - ranges[i][1];
        });
        pos.set(posData);
        pos.apply((v) -> v > 1 ? v % 1 : v);
        Direction face = this.rotate(this.getParent().getFace(), transform);
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
