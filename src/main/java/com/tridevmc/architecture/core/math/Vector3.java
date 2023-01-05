package com.tridevmc.architecture.core.math;

import com.google.common.base.MoreObjects;

import java.util.Objects;

record Vector3(double x, double y, double z) implements IVector3Immutable {

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
        if (!(o instanceof IVector3 vec)) return false;
        return Double.compare(vec.getX(), this.getX()) == 0 && Double.compare(vec.getY(), this.getY()) == 0 && Double.compare(vec.getZ(), this.getZ()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getX(), this.getY(), this.getZ());
    }

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
            if (!(o instanceof IVector3 vec)) return false;
            return Double.compare(vec.getX(), this.getX()) == 0 && Double.compare(vec.getY(), this.getY()) == 0 && Double.compare(vec.getZ(), this.getZ()) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getX(), this.getY(), this.getZ());
        }
    }

}
