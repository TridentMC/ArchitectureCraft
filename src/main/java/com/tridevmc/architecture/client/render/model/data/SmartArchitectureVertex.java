package com.tridevmc.architecture.client.render.model.data;

import com.tridevmc.architecture.client.render.model.data.ArchitectureQuad;
import com.tridevmc.architecture.client.render.model.data.ArchitectureVertex;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

public class SmartArchitectureVertex extends ArchitectureVertex {
    public SmartArchitectureVertex(ArchitectureQuad parent, float[] data) {
        super(parent, data, new float[]{0F, 0F});
    }

    public float[] getUVs(TransformationMatrix transform) {
        Vector3f pos = this.getPosition(transform);
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
