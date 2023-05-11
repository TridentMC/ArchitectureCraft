package com.tridevmc.architecture.core.math.floating;

import com.google.common.base.MoreObjects;

import java.util.Objects;

record Vector3F(float x, float y, float z) implements IVector3FImmutable {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", this.x())
                .add("y", this.y())
                .add("z", this.z())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IVector3F vec)) return false;
        return Float.compare(vec.getX(), this.getX()) == 0 && Float.compare(vec.getY(), this.getY()) == 0 && Float.compare(vec.getZ(), this.getZ()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY(), this.getZ());
    }

    static class Mutable implements IVector3FMutable {

        private float x, y, z;

        Mutable(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public float x() {
            return this.x;
        }

        @Override
        public float y() {
            return this.y;
        }

        @Override
        public float z() {
            return this.z;
        }

        @Override
        public IVector3FMutable set(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Override
        public IVector3FMutable setX(float x) {
            this.x = x;
            return this;
        }

        @Override
        public IVector3FMutable setY(float y) {
            this.y = y;
            return this;
        }

        @Override
        public IVector3FMutable setZ(float z) {
            this.z = z;
            return this;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("x", this.x())
                    .add("y", this.y())
                    .add("z", this.z())
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IVector3F vec)) return false;
            return Float.compare(vec.getX(), this.getX()) == 0 && Float.compare(vec.getY(), this.getY()) == 0 && Float.compare(vec.getZ(), this.getZ()) == 0;
        }

        @Override
        public int hashCode() {
            var out = 1;
            out = 31 * out + Float.hashCode(this.getX());
            out = 31 * out + Float.hashCode(this.getY());
            out = 31 * out + Float.hashCode(this.getZ());
            return out;
        }

    }

}
