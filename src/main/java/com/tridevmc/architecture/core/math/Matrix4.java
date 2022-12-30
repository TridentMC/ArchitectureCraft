package com.tridevmc.architecture.core.math;

record Matrix4(
        double m00, double m01, double m02, double m03,
        double m10, double m11, double m12, double m13,
        double m20, double m21, double m22, double m23,
        double m30, double m31, double m32, double m33
) implements IMatrix4Immutable {

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
        public IMatrix4Mutable setM00(double m00) {
            this.m00 = m00;
            return this;
        }

        @Override
        public IMatrix4Mutable setM01(double m01) {
            this.m01 = m01;
            return this;
        }

        @Override
        public IMatrix4Mutable setM02(double m02) {
            this.m02 = m02;
            return this;
        }

        @Override
        public IMatrix4Mutable setM03(double m03) {
            this.m03 = m03;
            return this;
        }

        @Override
        public IMatrix4Mutable setM10(double m10) {
            this.m10 = m10;
            return this;
        }

        @Override
        public IMatrix4Mutable setM11(double m11) {
            this.m11 = m11;
            return this;
        }

        @Override
        public IMatrix4Mutable setM12(double m12) {
            this.m12 = m12;
            return this;
        }

        @Override
        public IMatrix4Mutable setM13(double m13) {
            this.m13 = m13;
            return this;
        }

        @Override
        public IMatrix4Mutable setM20(double m20) {
            this.m20 = m20;
            return this;
        }

        @Override
        public IMatrix4Mutable setM21(double m21) {
            this.m21 = m21;
            return this;
        }

        @Override
        public IMatrix4Mutable setM22(double m22) {
            this.m22 = m22;
            return this;
        }

        @Override
        public IMatrix4Mutable setM23(double m23) {
            this.m23 = m23;
            return this;
        }

        @Override
        public IMatrix4Mutable setM30(double m30) {
            this.m30 = m30;
            return this;
        }

        @Override
        public IMatrix4Mutable setM31(double m31) {
            this.m31 = m31;
            return this;
        }

        @Override
        public IMatrix4Mutable setM32(double m32) {
            this.m32 = m32;
            return this;
        }

        @Override
        public IMatrix4Mutable setM33(double m33) {
            this.m33 = m33;
            return this;
        }
    }
}
