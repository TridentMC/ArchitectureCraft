package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.*;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of {@link IVertex}.
 *
 * @param pos    The position of this vertex.
 * @param normal The normal of this vertex.
 * @param uvs    The UV coordinates of this vertex.
 */
public record Vertex(@NotNull IVector3Immutable pos,
                     @NotNull IVector3Immutable normal,
                     @NotNull IVector2Immutable uvs) implements IVertex {

    public Vertex(@NotNull IVector3 pos, @NotNull IVector3 normal, @NotNull IVector2 uvs) {
        this(pos.asImmutable(), normal.asImmutable(), uvs.asImmutable());
    }

    public Vertex(double x, double y, double z, double nX, double nY, double nZ, double u, double v) {
        this(IVector3.ofImmutable(x, y, z), IVector3.ofImmutable(nX, nY, nZ), IVector2.ofImmutable(u, v));
    }

    @Override
    @NotNull
    public IVector3 getPos() {
        return this.pos;
    }

    @Override
    @NotNull
    public IVector3 getNormal() {
        return this.normal;
    }

    @Override
    public @NotNull IVertex transform(@NotNull ITrans3 trans, boolean transformUVs) {
        var builder = new Builder();

        trans.transformPos(builder.getPos().set(this.pos));
        trans.transformNormal(builder.getNormal().set(this.normal));
        builder.getUV().set(this.uvs);
        if (transformUVs) {
            trans.transformUV(builder.getUV());
        }

        return builder.build();
    }

    @Override
    public double getU() {
        return this.uvs.u();
    }

    @Override
    public double getV() {
        return this.uvs.v();
    }

    /**
     * Builder for {@link Vertex} instances.
     */
    public static class Builder {
        private final IVector3Mutable pos = IVector3.ofMutable(0, 0, 0), normal = IVector3.ofMutable(0, 0, 0);
        private final IVector2Mutable uvs = IVector2.ofMutable(0, 0);

        /**
         * Sets the position of the vertex.
         *
         * @param x The X coordinate of the vertex.
         * @param y The Y coordinate of the vertex.
         * @param z The Z coordinate of the vertex.
         * @return This builder.
         */
        public Builder setPos(double x, double y, double z) {
            this.pos.set(x, y, z);
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
            this.normal.set(nX, nY, nZ);
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
            this.uvs.set(u, v);
            return this;
        }

        /**
         * Gets the mutable position vector of the vertex being built.
         * <p>
         * This vector is mutable, and changes to it will be reflected in the vertex being built.
         *
         * @return The mutable position vector.
         */
        public IVector3Mutable getPos() {
            return this.pos;
        }

        /**
         * Gets the mutable normal vector of the vertex being built.
         * <p>
         * This vector is mutable, and changes to it will be reflected in the vertex being built.
         *
         * @return The mutable normal vector.
         */
        public IVector3Mutable getNormal() {
            return this.normal;
        }

        /**
         * Gets the mutable UV vector of the vertex being built.
         * <p>
         * This vector is mutable, and changes to it will be reflected in the vertex being built.
         *
         * @return The mutable UV vector.
         */
        public IVector2Mutable getUV() {
            return this.uvs;
        }

        /**
         * Builds a new {@link Vertex} instance.
         *
         * @return The new vertex.
         */
        public Vertex build() {
            return new Vertex(this.pos, this.normal, this.uvs);
        }
    }
}
