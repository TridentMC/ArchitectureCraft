package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.Transform;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of {@link IVertex}.
 *
 * @param pos    The position of this vertex.
 * @param normal The normal of this vertex.
 * @param u      The U coordinate of the vertex.
 * @param v      The V coordinate of the vertex.
 */
public record Vertex(@NotNull LegacyVector3 pos,
                     @NotNull LegacyVector3 normal,
                     double u, double v) implements IVertex {

    public Vertex(double x, double y, double z, double nX, double nY, double nZ, double u, double v) {
        this(new LegacyVector3(x, y, z), new LegacyVector3(nX, nY, nZ), u, v);
    }

    @Override
    @NotNull
    public LegacyVector3 getPos() {
        return this.pos;
    }

    @Override
    @NotNull
    public LegacyVector3 getNormal() {
        return this.normal;
    }

    @Override
    public @NotNull IVertex transform(@NotNull Transform trans, boolean transformUVs) {
        var builder = new Builder();

        return this;
    }

    @Override
    public double getU() {
        return this.u;
    }

    @Override
    public double getV() {
        return this.v;
    }

    /**
     * Builder for {@link Vertex} instances.
     */
    public static class Builder {
        private double x, y, z;
        private double nX, nY, nZ;
        private double u, v;

        /**
         * Sets the position of the vertex.
         *
         * @param x The X coordinate of the vertex.
         * @param y The Y coordinate of the vertex.
         * @param z The Z coordinate of the vertex.
         * @return This builder.
         */
        public Builder setPos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        /**
         * Sets the normal of the vertex.
         *
         * @param nX The X component of the vertex normal.
         * @param nY The Y component of the vertex normal.
         * @param nZ The Z component of the vertex normal.
         * @return This builder.
         */
        public Builder setNormal(double nX, double nY, double nZ) {
            this.nX = nX;
            this.nY = nY;
            this.nZ = nZ;
            return this;
        }

        /**
         * Sets the texture coordinates of the vertex.
         *
         * @param u The U coordinate of the vertex.
         * @param v The V coordinate of the vertex.
         * @return This builder.
         */
        public Builder setUV(double u, double v) {
            this.u = u;
            this.v = v;
            return this;
        }

        /**
         * Builds a new {@link Vertex} instance.
         *
         * @return The new vertex.
         */
        public Vertex build() {
            return new Vertex(this.x, this.y, this.z, this.nX, this.nY, this.nZ, this.u, this.v);
        }
    }
}
