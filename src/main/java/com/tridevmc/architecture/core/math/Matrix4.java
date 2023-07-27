package com.tridevmc.architecture.core.math;

import com.google.common.base.MoreObjects;

import java.util.Objects;

record Matrix4(
        double m00, double m01, double m02, double m03,
        double m10, double m11, double m12, double m13,
        double m20, double m21, double m22, double m23,
        double m30, double m31, double m32, double m33
) implements IMatrix4Immutable {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nm00", this.m00())
                .add("m01", this.m01())
                .add("m02", this.m02())
                .add("m03", this.m03())
                .add("\nm10", this.m10())
                .add("m11", this.m11())
                .add("m12", this.m12())
                .add("m13", this.m13())
                .add("\nm20", this.m20())
                .add("m21", this.m21())
                .add("m22", this.m22())
                .add("m23", this.m23())
                .add("\nm30", this.m30())
                .add("m31", this.m31())
                .add("m32", this.m32())
                .add("m33", this.m33())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IMatrix4 matrix4)) return false;
        return Double.compare(matrix4.m00(), this.m00()) == 0 &&
                Double.compare(matrix4.m01(), this.m01()) == 0 &&
                Double.compare(matrix4.m02(), this.m02()) == 0 &&
                Double.compare(matrix4.m03(), this.m03()) == 0 &&
                Double.compare(matrix4.m10(), this.m10()) == 0 &&
                Double.compare(matrix4.m11(), this.m11()) == 0 &&
                Double.compare(matrix4.m12(), this.m12()) == 0 &&
                Double.compare(matrix4.m13(), this.m13()) == 0 &&
                Double.compare(matrix4.m20(), this.m20()) == 0 &&
                Double.compare(matrix4.m21(), this.m21()) == 0 &&
                Double.compare(matrix4.m22(), this.m22()) == 0 &&
                Double.compare(matrix4.m23(), this.m23()) == 0 &&
                Double.compare(matrix4.m30(), this.m30()) == 0 &&
                Double.compare(matrix4.m31(), this.m31()) == 0 &&
                Double.compare(matrix4.m32(), this.m32()) == 0 &&
                Double.compare(matrix4.m33(), this.m33()) == 0;
    }

    @Override
    public int hashCode() {
        var out = 1;
        out = out * 31 + Double.hashCode(this.m00());
        out = out * 31 + Double.hashCode(this.m01());
        out = out * 31 + Double.hashCode(this.m02());
        out = out * 31 + Double.hashCode(this.m03());
        out = out * 31 + Double.hashCode(this.m10());
        out = out * 31 + Double.hashCode(this.m11());
        out = out * 31 + Double.hashCode(this.m12());
        out = out * 31 + Double.hashCode(this.m13());
        out = out * 31 + Double.hashCode(this.m20());
        out = out * 31 + Double.hashCode(this.m21());
        out = out * 31 + Double.hashCode(this.m22());
        out = out * 31 + Double.hashCode(this.m23());
        out = out * 31 + Double.hashCode(this.m30());
        out = out * 31 + Double.hashCode(this.m31());
        out = out * 31 + Double.hashCode(this.m32());
        out = out * 31 + Double.hashCode(this.m33());
        return out;
    }

    static class Mutable implements IMatrix4Mutable {

        private double m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33;

        Mutable(
                double m00, double m01, double m02, double m03,
                double m10, double m11, double m12, double m13,
                double m20, double m21, double m22, double m23,
                double m30, double m31, double m32, double m33
        ) {
            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

        @Override
        public double m00() {
            return this.m00;
        }

        @Override
        public double m01() {
            return this.m01;
        }

        @Override
        public double m02() {
            return this.m02;
        }

        @Override
        public double m03() {
            return this.m03;
        }

        @Override
        public double m10() {
            return this.m10;
        }

        @Override
        public double m11() {
            return this.m11;
        }

        @Override
        public double m12() {
            return this.m12;
        }

        @Override
        public double m13() {
            return this.m13;
        }

        @Override
        public double m20() {
            return this.m20;
        }

        @Override
        public double m21() {
            return this.m21;
        }

        @Override
        public double m22() {
            return this.m22;
        }

        @Override
        public double m23() {
            return this.m23;
        }

        @Override
        public double m30() {
            return this.m30;
        }

        @Override
        public double m31() {
            return this.m31;
        }

        @Override
        public double m32() {
            return this.m32;
        }

        @Override
        public double m33() {
            return this.m33;
        }

        @Override
        public Mutable m00(double m00) {
            this.m00 = m00;
            return this;
        }

        @Override
        public Mutable m01(double m01) {
            this.m01 = m01;
            return this;
        }

        @Override
        public Mutable m02(double m02) {
            this.m02 = m02;
            return this;
        }

        @Override
        public Mutable m03(double m03) {
            this.m03 = m03;
            return this;
        }

        @Override
        public Mutable m10(double m10) {
            this.m10 = m10;
            return this;
        }

        @Override
        public Mutable m11(double m11) {
            this.m11 = m11;
            return this;
        }

        @Override
        public Mutable m12(double m12) {
            this.m12 = m12;
            return this;
        }

        @Override
        public Mutable m13(double m13) {
            this.m13 = m13;
            return this;
        }

        @Override
        public Mutable m20(double m20) {
            this.m20 = m20;
            return this;
        }

        @Override
        public Mutable m21(double m21) {
            this.m21 = m21;
            return this;
        }

        @Override
        public Mutable m22(double m22) {
            this.m22 = m22;
            return this;
        }

        @Override
        public Mutable m23(double m23) {
            this.m23 = m23;
            return this;
        }

        @Override
        public Mutable m30(double m30) {
            this.m30 = m30;
            return this;
        }

        @Override
        public Mutable m31(double m31) {
            this.m31 = m31;
            return this;
        }

        @Override
        public Mutable m32(double m32) {
            this.m32 = m32;
            return this;
        }

        @Override
        public Mutable m33(double m33) {
            this.m33 = m33;
            return this;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\nm00", this.m00())
                    .add("m01", this.m01())
                    .add("m02", this.m02())
                    .add("m03", this.m03())
                    .add("\nm10", this.m10())
                    .add("m11", this.m11())
                    .add("m12", this.m12())
                    .add("m13", this.m13())
                    .add("\nm20", this.m20())
                    .add("m21", this.m21())
                    .add("m22", this.m22())
                    .add("m23", this.m23())
                    .add("\nm30", this.m30())
                    .add("m31", this.m31())
                    .add("m32", this.m32())
                    .add("m33", this.m33())
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IMatrix4 matrix4)) return false;
            return Double.compare(matrix4.m00(), this.m00()) == 0 &&
                    Double.compare(matrix4.m01(), this.m01()) == 0 &&
                    Double.compare(matrix4.m02(), this.m02()) == 0 &&
                    Double.compare(matrix4.m03(), this.m03()) == 0 &&
                    Double.compare(matrix4.m10(), this.m10()) == 0 &&
                    Double.compare(matrix4.m11(), this.m11()) == 0 &&
                    Double.compare(matrix4.m12(), this.m12()) == 0 &&
                    Double.compare(matrix4.m13(), this.m13()) == 0 &&
                    Double.compare(matrix4.m20(), this.m20()) == 0 &&
                    Double.compare(matrix4.m21(), this.m21()) == 0 &&
                    Double.compare(matrix4.m22(), this.m22()) == 0 &&
                    Double.compare(matrix4.m23(), this.m23()) == 0 &&
                    Double.compare(matrix4.m30(), this.m30()) == 0 &&
                    Double.compare(matrix4.m31(), this.m31()) == 0 &&
                    Double.compare(matrix4.m32(), this.m32()) == 0 &&
                    Double.compare(matrix4.m33(), this.m33()) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.m00(), this.m01(), this.m02(), this.m03(),
                    this.m10(), this.m11(), this.m12(), this.m13(),
                    this.m20(), this.m21(), this.m22(), this.m23(),
                    this.m30(), this.m31(), this.m32(), this.m33());
        }

    }

}
