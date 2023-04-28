package com.tridevmc.architecture.client.render.model.piped;

import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

public record PipedVertex<Q extends IPipedBakedQuad<Q, PipedVertex<Q, D>, D>, D>(
        double x, double y, double z,
        float nX, float nY, float nZ,
        float u, float v
) implements IPipedVertex<PipedVertex<Q, D>, Q, D> {

    /**
     * A builder for {@link PipedVertex} instances.
     *
     * @param <Q> The quad type.
     * @param <D> The metadata type.
     */
    public static class Builder<Q extends IPipedBakedQuad<Q, PipedVertex<Q, D>, D>, D> {

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
        public Builder<Q, D> pos(double x, double y, double z) {
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
        public Builder<Q, D> normal(float nX, float nY, float nZ) {
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
        public Builder<Q, D> uvs(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        /**
         * Builds the vertex.
         *
         * @return The vertex.
         */
        public PipedVertex<Q, D> build() {
            return new PipedVertex<>(this.x, this.y, this.z, this.nX, this.nY, this.nZ, this.u, this.v);
        }
    }

    @Override
    public PipedVertex<Q, D> transform(@NotNull Q quadProvider, @NotNull ITrans3 trans) {
        if (trans.isIdentity()) {
            return this;
        } else {
            var fromFace = quadProvider.face();
            var toFace = quadProvider.face(trans);
            var pos = this.pos(trans);
            var normal = this.normal(trans);
            var uvs = this.uvs(trans, fromFace, toFace);
            return new PipedVertex<>(
                    (float) pos.x(), (float) pos.y(), (float) pos.z(),
                    normal.x(), normal.y(), normal.z(),
                    (float) uvs.u(), (float) uvs.v()
            );
        }
    }

}
