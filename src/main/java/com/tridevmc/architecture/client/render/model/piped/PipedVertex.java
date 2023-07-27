package com.tridevmc.architecture.client.render.model.piped;

import java.util.Objects;

public class PipedVertex<V extends PipedVertex<V, Q, D>, Q extends IPipedBakedQuad<Q, V, D>, D> implements IPipedVertex<V, Q, D> {

    private final double x, y, z;
    private final float nX, nY, nZ;
    private final float u, v;

    public PipedVertex(
            double x, double y, double z,
            float nX, float nY, float nZ,
            float u, float v
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.nX = nX;
        this.nY = nY;
        this.nZ = nZ;
        this.u = u;
        this.v = v;
    }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    @Override
    public double z() {
        return this.z;
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
    public float u() {
        return this.u;
    }

    @Override
    public float v() {
        return this.v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PipedVertex) obj;
        return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
                Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
                Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z) &&
                Float.floatToIntBits(this.nX) == Float.floatToIntBits(that.nX) &&
                Float.floatToIntBits(this.nY) == Float.floatToIntBits(that.nY) &&
                Float.floatToIntBits(this.nZ) == Float.floatToIntBits(that.nZ) &&
                Float.floatToIntBits(this.u) == Float.floatToIntBits(that.u) &&
                Float.floatToIntBits(this.v) == Float.floatToIntBits(that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z, this.nX, this.nY, this.nZ, this.u, this.v);
    }

    @Override
    public String toString() {
        return "PipedVertex[" +
                "x=" + this.x + ", " +
                "y=" + this.y + ", " +
                "z=" + this.z + ", " +
                "nX=" + this.nX + ", " +
                "nY=" + this.nY + ", " +
                "nZ=" + this.nZ + ", " +
                "u=" + this.u + ", " +
                "v=" + this.v + ']';
    }

    /**
     * A builder for {@link PipedVertex} instances.
     *
     * @param <V> The vertex type.
     * @param <Q> The quad type.
     * @param <D> The metadata type.
     */
    public static class Builder<V extends PipedVertex<V, Q, D>, Q extends IPipedBakedQuad<Q, V, D>, D> {

        private double x, y, z;
        private float nX, nY, nZ;
        private float u, v;

        /**
         * Sets the position of the vertex.
         *
         * @param x The x coordinate.
         * @param y The y coordinate.
         * @param z The z coordinate.
         * @return The builder.
         */
        public Builder<V, Q, D> pos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        /**
         * Sets the normal of the vertex.
         *
         * @param nX The x component of the normal.
         * @param nY The y component of the normal.
         * @param nZ The z component of the normal.
         * @return The builder.
         */
        public Builder<V, Q, D> normal(float nX, float nY, float nZ) {
            this.nX = nX;
            this.nY = nY;
            this.nZ = nZ;
            return this;
        }

        /**
         * Sets the UV coordinates of the vertex.
         *
         * @param u The u coordinate.
         * @param v The v coordinate.
         * @return The builder.
         */
        public Builder<V, Q, D> uvs(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        /**
         * Builds the vertex.
         *
         * @return The vertex.
         */
        public PipedVertex<V, Q, D> build() {
            return new PipedVertex<>(this.x, this.y, this.z, this.nX, this.nY, this.nZ, this.u, this.v);
        }

    }


}
