package com.tridevmc.architecture.client.render.model.piped;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.IVector3;
import net.minecraft.core.Direction;

import java.util.Objects;

public class PipedBakedQuad<Q extends PipedBakedQuad<Q, V, D>, V extends IPipedVertex<V, Q, D>, D> implements IPipedBakedQuad<Q, V, D> {

    private final ImmutableList<V> vertices;
    private final float nX, nY, nZ;
    private final float minX, minY, minZ;
    private final float maxX, maxY, maxZ;
    private final Direction face;
    private final boolean shouldCull;
    private final D metadata;

    public PipedBakedQuad(
            ImmutableList<V> vertices,
            float nX, float nY, float nZ,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            Direction face,
            boolean shouldCull,
            D metadata
    ) {
        this.vertices = vertices;
        this.nX = nX;
        this.nY = nY;
        this.nZ = nZ;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.face = face;
        this.shouldCull = shouldCull;
        this.metadata = metadata;
    }

    public PipedBakedQuad(ImmutableList<V> vertices, IVector3 normal, IVector3 min, IVector3 max, Direction face, boolean shouldCull, D metadata) {
        this(vertices, (float) normal.x(), (float) normal.y(), (float) normal.z(),
                (float) min.x(), (float) min.y(), (float) min.z(),
                (float) max.x(), (float) max.y(), (float) max.z(),
                face, shouldCull, metadata);
    }

    @Override
    public ImmutableList<V> vertices() {
        return this.vertices;
    }

    @Override
    public float nX() {
        return this.nX;
    }

    @Override
    public float nY() {
        return this.nY;
    }

    @Override
    public float nZ() {
        return this.nZ;
    }

    @Override
    public float minX() {
        return this.minX;
    }

    @Override
    public float minY() {
        return this.minY;
    }

    @Override
    public float minZ() {
        return this.minZ;
    }

    @Override
    public float maxX() {
        return this.maxX;
    }

    @Override
    public float maxY() {
        return this.maxY;
    }

    @Override
    public float maxZ() {
        return this.maxZ;
    }

    @Override
    public Direction face() {
        return this.face;
    }

    @Override
    public boolean shouldCull() {
        return this.shouldCull;
    }

    @Override
    public D metadata() {
        return this.metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PipedBakedQuad) obj;
        return Objects.equals(this.vertices, that.vertices) &&
                Float.floatToIntBits(this.nX) == Float.floatToIntBits(that.nX) &&
                Float.floatToIntBits(this.nY) == Float.floatToIntBits(that.nY) &&
                Float.floatToIntBits(this.nZ) == Float.floatToIntBits(that.nZ) &&
                Float.floatToIntBits(this.minX) == Float.floatToIntBits(that.minX) &&
                Float.floatToIntBits(this.minY) == Float.floatToIntBits(that.minY) &&
                Float.floatToIntBits(this.minZ) == Float.floatToIntBits(that.minZ) &&
                Float.floatToIntBits(this.maxX) == Float.floatToIntBits(that.maxX) &&
                Float.floatToIntBits(this.maxY) == Float.floatToIntBits(that.maxY) &&
                Float.floatToIntBits(this.maxZ) == Float.floatToIntBits(that.maxZ) &&
                Objects.equals(this.face, that.face) &&
                this.shouldCull == that.shouldCull &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vertices, this.nX, this.nY, this.nZ, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.face, this.shouldCull, this.metadata);
    }

    @Override
    public String toString() {
        return "PipedBakedQuad[" +
                "vertices=" + this.vertices + ", " +
                "nX=" + this.nX + ", " +
                "nY=" + this.nY + ", " +
                "nZ=" + this.nZ + ", " +
                "minX=" + this.minX + ", " +
                "minY=" + this.minY + ", " +
                "minZ=" + this.minZ + ", " +
                "maxX=" + this.maxX + ", " +
                "maxY=" + this.maxY + ", " +
                "maxZ=" + this.maxZ + ", " +
                "face=" + this.face + ", " +
                "shouldCull=" + this.shouldCull + ", " +
                "metadata=" + this.metadata + ']';
    }


}
