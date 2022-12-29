package com.tridevmc.architecture.core.math;

/**
 * An immutable 3D vector, primarily used for positions and normals.
 *
 * @param x The X coordinate of the vector.
 * @param y The Y coordinate of the vector.
 * @param z The Z coordinate of the vector.
 */
record Vector3(double x, double y, double z) implements IVector3Immutable {

    static class Mutable implements IVector3Mutable {

        private double x, y, z;

        Mutable(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
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
        public IVector3Mutable set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Override
        public IVector3Mutable setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public IVector3Mutable setY(double y) {
            this.y = y;
            return this;
        }

        @Override
        public IVector3Mutable setZ(double z) {
            this.z = z;
            return this;
        }
    }

}
