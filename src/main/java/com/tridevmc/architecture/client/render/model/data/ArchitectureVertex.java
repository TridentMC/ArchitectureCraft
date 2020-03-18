package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;

public class ArchitectureVertex {

    private IBakedQuadProvider parent;
    private float[] data;
    private float[] uvs;

    public ArchitectureVertex(IBakedQuadProvider parent, float[] data, float[] uvs) {
        this.parent = parent;
        this.data = data;
        this.uvs = uvs;
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

    protected Direction rotate(Direction direction, TransformationMatrix transform) {
        Vec3i dir = direction == null ? new Vec3i(0,0,0) : direction.getDirectionVec();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
        transform.transformPosition(vec);
        return Direction.getFacingFromVector(vec.getX(), vec.getY(), vec.getZ());
    }

    public float[] getUVs(TransformationMatrix transform) {
        return this.uvs;
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

    protected IBakedQuadProvider getParent() {
        return this.parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArchitectureVertex) {
            return Arrays.equals(((ArchitectureVertex) obj).data, this.data);
        }

        return super.equals(obj);
    }
}
