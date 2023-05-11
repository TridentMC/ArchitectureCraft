package com.tridevmc.architecture.core.math.floating;

record Vector2F(double x, double y) implements IVector2FImmutable {

    static class Mutable implements IVector2FMutable {

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
        public IVector2FMutable set(double x, double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        @Override
        public IVector2FMutable setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public IVector2FMutable setY(double y) {
            this.y = y;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;

            Mutable mutable = (Mutable) o;

            if (Double.compare(mutable.x, this.x) != 0) return false;
            return Double.compare(mutable.y, this.y) == 0;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(this.x);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(this.y);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

    }

}
