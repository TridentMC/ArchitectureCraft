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

    }

}
