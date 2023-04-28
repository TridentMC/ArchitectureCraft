package com.tridevmc.architecture.core.math;

record Vector2(double x, double y) implements IVector2Immutable {

    static class Mutable implements IVector2Mutable {

        private double x, y;

        Mutable(double x, double y) {
            this.x = x;
            this.y = y;
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
        public IVector2Mutable set(double x, double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public IVector2Mutable setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public IVector2Mutable setY(double y) {
            this.y = y;
            return this;
        }

    }

}
