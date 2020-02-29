package com.tridevmc.architecture.client.render.model;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;

public class ArchitectureVertex {

    private ArchitectureQuad parent;
    private float[] data;

    protected ArchitectureVertex(ArchitectureQuad parent, float[] data) {
        this.parent = parent;
        this.data = data;
    }

    public Vector3f getNormals() {
        return this.parent.getNormals();
    }

    public Vector3f getPosition() {
        return new Vector3f(this.data);
    }

    public Vector3f getPosition(TransformationMatrix transform) {
        Vector3f position = this.getPosition();
        Vector4f transformedPosition = new Vector4f(position.getX(), position.getY(), position.getZ(), 1);
        transform.transformPosition(transformedPosition);
        return new Vector3f(transformedPosition.getX(), transformedPosition.getY(), transformedPosition.getZ());
    }

    private Direction rotate(Direction direction, TransformationMatrix transform) {
        Vec3i dir = direction.getDirectionVec();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
        transform.transformPosition(vec);
        return Direction.getFacingFromVector(vec.getX(), vec.getY(), vec.getZ());
    }

    public float[] getUVs(TransformationMatrix transform) {
        Vector3f pos = this.getPosition(transform);
        Direction face = this.rotate(this.parent.getFace(), transform);
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

    public float getX() {
        return this.data[0];
    }

    public float getY() {
        return this.data[1];
    }

    public float getZ() {
        return this.data[2];
    }

    public float getNormalX() {
        return this.getNormals().getX();
    }

    public float getNormalY() {
        return this.getNormals().getY();
    }

    public float getNormalZ() {
        return this.getNormals().getZ();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArchitectureVertex) {
            return Arrays.equals(((ArchitectureVertex) obj).data, this.data);
        }

        return super.equals(obj);
    }
}
