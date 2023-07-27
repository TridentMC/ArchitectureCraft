package com.tridevmc.architecture.core.math.integer;

record Vector3i(int x, int y, int z) implements IVector3iImmutable {

    static class Mutable implements IVector3iMutable {

        private int x, y, z;

        Mutable(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public IVector3iMutable setX(int x) {
            this.x = x;
            return this;
        }

        @Override
        public IVector3iMutable setY(int y) {
            this.y = y;
            return this;
        }

        @Override
        public IVector3iMutable setZ(int z) {
            this.z = z;
            return this;
        }

        @Override
        public IVector3iMutable set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Override
        public int x() {
            return this.x;
        }

        @Override
        public int y() {
            return this.y;
        }

        @Override
        public int z() {
            return this.z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            Mutable mutable = (Mutable) o;

            if (this.x != mutable.x) return false;
            if (this.y != mutable.y) return false;
            return this.z == mutable.z;
        }

        @Override
        public int hashCode() {
            int result = this.x;
            result = 31 * result + this.y;
            result = 31 * result + this.z;
            return result;
        }

        @Override
        public String toString() {
            return "Mutable{" +
                    "x=" + this.x +
                    ", y=" + this.y +
                    ", z=" + this.z +
                    '}';
        }
    }

}
