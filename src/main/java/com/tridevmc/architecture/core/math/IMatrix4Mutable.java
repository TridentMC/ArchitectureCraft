package com.tridevmc.architecture.core.math;

import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link IMatrix4} that allows for mutation of the matrix.
 * <p>
 * Construct using {@link IMatrix4Mutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
 * <p>
 * See also: {@link IMatrix4Immutable}
 */
public interface IMatrix4Mutable extends IMatrix4 {

    /**
     * Creates a new mutable matrix with the given values.
     *
     * @param m00 The value at [0, 0].
     * @param m01 The value at [0, 1].
     * @param m02 The value at [0, 2].
     * @param m03 The value at [0, 3].
     * @param m10 The value at [1, 0].
     * @param m11 The value at [1, 1].
     * @param m12 The value at [1, 2].
     * @param m13 The value at [1, 3].
     * @param m20 The value at [2, 0].
     * @param m21 The value at [2, 1].
     * @param m22 The value at [2, 2].
     * @param m23 The value at [2, 3].
     * @param m30 The value at [3, 0].
     * @param m31 The value at [3, 1].
     * @param m32 The value at [3, 2].
     * @param m33 The value at [3, 3].
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable of(double m00, double m01, double m02, double m03,
                              double m10, double m11, double m12, double m13,
                              double m20, double m21, double m22, double m23,
                              double m30, double m31, double m32, double m33) {
        return new Matrix4.Mutable(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    /**
     * Creates a new mutable matrix with the values of the given matrix.
     *
     * @param matrix The matrix to copy.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable of(@NotNull IMatrix4 matrix) {
        return of(
                matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03(),
                matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13(),
                matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23(),
                matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()
        );
    }

    /**
     * Creates a new mutable matrix that represents the given translation.
     *
     * @param transX The translation along the x-axis.
     * @param transY The translation along the y-axis.
     * @param transZ The translation along the z-axis.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofTranslation(double transX, double transY, double transZ) {
        // Should be equivalent to:
        // return ofIdentity().translate(transX, transY, transZ)
        // The translate method is as follows:
        // this.m03(this.m00() * x + this.m01() * y + this.m02() * z + this.m03())
        // .m13(this.m10() * x + this.m11() * y + this.m12() * z + this.m13())
        // .m23(this.m20() * x + this.m21() * y + this.m22() * z + this.m23())
        // .m33(this.m30() * x + this.m31() * y + this.m32() * z + this.m33());

        return of(
                1, 0, 0, transX,
                0, 1, 0, transY,
                0, 0, 1, transZ,
                0, 0, 0, 1
        );
    }

    /**
     * Creates a new mutable matrix that represents the given translation.
     *
     * @param trans The translation.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofTranslation(@NotNull IVector3 trans) {
        return ofTranslation(trans.x(), trans.y(), trans.z());
    }

    /**
     * Creates a new mutable matrix that represents the given scale.
     *
     * @param scaleX The scale along the x-axis.
     * @param scaleY The scale along the y-axis.
     * @param scaleZ The scale along the z-axis.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofScale(double scaleX, double scaleY, double scaleZ) {
        return of(
                scaleX, 0, 0, 0,
                0, scaleY, 0, 0,
                0, 0, scaleZ, 0,
                0, 0, 0, 1
        );
    }

    /**
     * Creates a new mutable matrix that represents the given scale.
     *
     * @param scale The scale.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofScale(@NotNull IVector3 scale) {
        return ofScale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Creates a new mutable matrix that represents the given rotation, using the XYZ convention.
     *
     * @param xAngle The rotation around the x-axis, in degrees.
     * @param yAngle The rotation around the y-axis, in degrees.
     * @param zAngle The rotation around the z-axis, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofRotationXYZ(double xAngle, double yAngle, double zAngle) {
        return IMatrix4Mutable.ofIdentity().rotateXYZ(xAngle, yAngle, zAngle);
    }

    /**
     * Creates a new mutable matrix that represents the given rotation, using the XYZ convention.
     *
     * @param angles The rotation angles, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofRotationXYZ(@NotNull IVector3 angles) {
        return IMatrix4Mutable.ofIdentity().rotateXYZ(angles);
    }

    /**
     * Creates a new mutable matrix that represents the given rotation, using the ZYX convention.
     *
     * @param xAngle The rotation around the x-axis, in degrees.
     * @param yAngle The rotation around the y-axis, in degrees.
     * @param zAngle The rotation around the z-axis, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofRotationZYX(double xAngle, double yAngle, double zAngle) {
        return IMatrix4Mutable.ofIdentity().rotateZYX(xAngle, yAngle, zAngle);
    }

    /**
     * Creates a new mutable matrix that represents the given rotation, using the ZYX convention.
     *
     * @param angles The rotation angles, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofRotationZYX(@NotNull IVector3 angles) {
        return IMatrix4Mutable.ofIdentity().rotateZYX(angles);
    }

    /**
     * Creates a new mutable matrix that represents the given rotation, using the ZYX convention.
     *
     * @param xAngle The rotation around the x-axis, in degrees.
     * @param yAngle The rotation around the y-axis, in degrees.
     * @param zAngle The rotation around the z-axis, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofRotationYXZ(double xAngle, double yAngle, double zAngle) {
        return IMatrix4Mutable.ofIdentity().rotateYXZ(xAngle, yAngle, zAngle);
    }

    /**
     * Creates a new mutable matrix that represents the given rotation, using the ZYX convention.
     *
     * @param angles The rotation angles, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofRotationYXZ(@NotNull IVector3 angles) {
        return IMatrix4Mutable.ofIdentity().rotateYXZ(angles);
    }

    /**
     * Creates a new mutable matrix with the values of the identity matrix.
     *
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofIdentity() {
        return of(
                IMatrix4.IDENTITY
        );
    }

    /**
     * Sets the value of the matrix at the 0, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m00(double value);

    /**
     * Sets the value of the matrix at the 0, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m01(double value);

    /**
     * Sets the value of the matrix at the 0, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m02(double value);

    /**
     * Sets the value of the matrix at the 0, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m03(double value);

    /**
     * Sets the value of the matrix at the 1, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m10(double value);

    /**
     * Sets the value of the matrix at the 1, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m11(double value);

    /**
     * Sets the value of the matrix at the 1, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m12(double value);

    /**
     * Sets the value of the matrix at the 1, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m13(double value);

    /**
     * Sets the value of the matrix at the 2, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m20(double value);

    /**
     * Sets the value of the matrix at the 2, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m21(double value);

    /**
     * Sets the value of the matrix at the 2, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m22(double value);

    /**
     * Sets the value of the matrix at the 2, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m23(double value);

    /**
     * Sets the value of the matrix at the 3, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m30(double value);

    /**
     * Sets the value of the matrix at the 3, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m31(double value);

    /**
     * Sets the value of the matrix at the 3, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m32(double value);

    /**
     * Sets the value of the matrix at the 3, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    IMatrix4Mutable m33(double value);

    /**
     * Sets the value of the matrix at the 0, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM00(double value) {
        return this.m00(value);
    }

    /**
     * Sets the value of the matrix at the 0, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM01(double value) {
        return this.m01(value);
    }

    /**
     * Sets the value of the matrix at the 0, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM02(double value) {
        return this.m02(value);
    }

    /**
     * Sets the value of the matrix at the 0, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM03(double value) {
        return this.m03(value);
    }

    /**
     * Sets the value of the matrix at the 1, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM10(double value) {
        return this.m10(value);
    }

    /**
     * Sets the value of the matrix at the 1, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM11(double value) {
        return this.m11(value);
    }

    /**
     * Sets the value of the matrix at the 1, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM12(double value) {
        return this.m12(value);
    }

    /**
     * Sets the value of the matrix at the 1, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM13(double value) {
        return this.m13(value);
    }

    /**
     * Sets the value of the matrix at the 2, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM20(double value) {
        return this.m20(value);
    }

    /**
     * Sets the value of the matrix at the 2, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM21(double value) {
        return this.m21(value);
    }

    /**
     * Sets the value of the matrix at the 2, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM22(double value) {
        return this.m22(value);
    }

    /**
     * Sets the value of the matrix at the 2, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM23(double value) {
        return this.m23(value);
    }

    /**
     * Sets the value of the matrix at the 3, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM30(double value) {
        return this.m30(value);
    }

    /**
     * Sets the value of the matrix at the 3, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM31(double value) {
        return this.m31(value);
    }

    /**
     * Sets the value of the matrix at the 3, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM32(double value) {
        return this.m32(value);
    }

    /**
     * Sets the value of the matrix at the 3, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable setM33(double value) {
        return this.m33(value);
    }


    /**
     * Sets the value of the matrix at the given position.
     *
     * @param i     [i, j] The position of the value.
     * @param j     [i, j] The position of the value.
     * @param value the value to set.
     */
    @NotNull
    default IMatrix4Mutable set(int i, int j, double value) {
        switch (i) {
            case 0 -> {
                switch (j) {
                    case 0 -> this.m00(value);
                    case 1 -> this.m01(value);
                    case 2 -> this.m02(value);
                    case 3 -> this.m03(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            case 1 -> {
                switch (j) {
                    case 0 -> this.m10(value);
                    case 1 -> this.m11(value);
                    case 2 -> this.m12(value);
                    case 3 -> this.m13(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            case 2 -> {
                switch (j) {
                    case 0 -> this.m20(value);
                    case 1 -> this.m21(value);
                    case 2 -> this.m22(value);
                    case 3 -> this.m23(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            case 3 -> {
                switch (j) {
                    case 0 -> this.m30(value);
                    case 1 -> this.m31(value);
                    case 2 -> this.m32(value);
                    case 3 -> this.m33(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            default -> throw new IndexOutOfBoundsException(
                    String.format("i index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
            );
        }
        return this;
    }

    /**
     * Sets all the values of the matrix to the values provided.
     *
     * @param m00 The value at [0, 0].
     * @param m01 The value at [0, 1].
     * @param m02 The value at [0, 2].
     * @param m03 The value at [0, 3].
     * @param m10 The value at [1, 0].
     * @param m11 The value at [1, 1].
     * @param m12 The value at [1, 2].
     * @param m13 The value at [1, 3].
     * @param m20 The value at [2, 0].
     * @param m21 The value at [2, 1].
     * @param m22 The value at [2, 2].
     * @param m23 The value at [2, 3].
     * @param m30 The value at [3, 0].
     * @param m31 The value at [3, 1].
     * @param m32 The value at [3, 2].
     * @param m33 The value at [3, 3].
     * @return The matrix.
     */
    @NotNull
    default IMatrix4Mutable set(double m00, double m01, double m02, double m03,
                                double m10, double m11, double m12, double m13,
                                double m20, double m21, double m22, double m23,
                                double m30, double m31, double m32, double m33) {
        return this.m00(m00).m01(m01).m02(m02).m03(m03)
                .m10(m10).m11(m11).m12(m12).m13(m13)
                .m20(m20).m21(m21).m22(m22).m23(m23)
                .m30(m30).m31(m31).m32(m32).m33(m33);
    }

    /**
     * Post multiplies this matrix by the given matrix, storing the result in this matrix.
     *
     * @param m00 The value at the [0, 0]. position of the matrix to multiply by.
     * @param m01 The value at the [0, 1]. position of the matrix to multiply by.
     * @param m02 The value at the [0, 2]. position of the matrix to multiply by.
     * @param m03 The value at the [0, 3]. position of the matrix to multiply by.
     * @param m10 The value at the [1, 0]. position of the matrix to multiply by.
     * @param m11 The value at the [1, 1]. position of the matrix to multiply by.
     * @param m12 The value at the [1, 2]. position of the matrix to multiply by.
     * @param m13 The value at the [1, 3]. position of the matrix to multiply by.
     * @param m20 The value at the [2, 0]. position of the matrix to multiply by.
     * @param m21 The value at the [2, 1]. position of the matrix to multiply by.
     * @param m22 The value at the [2, 2]. position of the matrix to multiply by.
     * @param m23 The value at the [2, 3]. position of the matrix to multiply by.
     * @param m30 The value at the [3, 0]. position of the matrix to multiply by.
     * @param m31 The value at the [3, 1]. position of the matrix to multiply by.
     * @param m32 The value at the [3, 2]. position of the matrix to multiply by.
     * @param m33 The value at the [3, 3]. position of the matrix to multiply by.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable mul(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        final double a00 = this.m00();
        final double a01 = this.m01();
        final double a02 = this.m02();
        final double a03 = this.m03();
        final double a10 = this.m10();
        final double a11 = this.m11();
        final double a12 = this.m12();
        final double a13 = this.m13();
        final double a20 = this.m20();
        final double a21 = this.m21();
        final double a22 = this.m22();
        final double a23 = this.m23();
        final double a30 = this.m30();
        final double a31 = this.m31();
        final double a32 = this.m32();
        final double a33 = this.m33();

        final double t00 = a00 * m00 + a01 * m10 + a02 * m20 + a03 * m30;
        final double t01 = a00 * m01 + a01 * m11 + a02 * m21 + a03 * m31;
        final double t02 = a00 * m02 + a01 * m12 + a02 * m22 + a03 * m32;
        final double t03 = a00 * m03 + a01 * m13 + a02 * m23 + a03 * m33;
        final double t10 = a10 * m00 + a11 * m10 + a12 * m20 + a13 * m30;
        final double t11 = a10 * m01 + a11 * m11 + a12 * m21 + a13 * m31;
        final double t12 = a10 * m02 + a11 * m12 + a12 * m22 + a13 * m32;
        final double t13 = a10 * m03 + a11 * m13 + a12 * m23 + a13 * m33;
        final double t20 = a20 * m00 + a21 * m10 + a22 * m20 + a23 * m30;
        final double t21 = a20 * m01 + a21 * m11 + a22 * m21 + a23 * m31;
        final double t22 = a20 * m02 + a21 * m12 + a22 * m22 + a23 * m32;
        final double t23 = a20 * m03 + a21 * m13 + a22 * m23 + a23 * m33;
        final double t30 = a30 * m00 + a31 * m10 + a32 * m20 + a33 * m30;
        final double t31 = a30 * m01 + a31 * m11 + a32 * m21 + a33 * m31;
        final double t32 = a30 * m02 + a31 * m12 + a32 * m22 + a33 * m32;
        final double t33 = a30 * m03 + a31 * m13 + a32 * m23 + a33 * m33;

        return this.set(
                t00, t01, t02, t03,
                t10, t11, t12, t13,
                t20, t21, t22, t23,
                t30, t31, t32, t33
        );
    }

    /**
     * Multiplies this matrix by the specified matrix, storing the result in this matrix.
     *
     * @param other the matrix to multiply by.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable mul(@NotNull IMatrix4 other) {
        return this.mul(
                other.m00(), other.m01(), other.m02(), other.m03(),
                other.m10(), other.m11(), other.m12(), other.m13(),
                other.m20(), other.m21(), other.m22(), other.m23(),
                other.m30(), other.m31(), other.m32(), other.m33()
        );
    }

    /**
     * Multiplies all components of this matrix by the specified scalar value.
     *
     * @param scalar the scalar value to multiply by.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable mul(double scalar) {
        return this.mul(
                scalar, scalar, scalar, scalar,
                scalar, scalar, scalar, scalar,
                scalar, scalar, scalar, scalar,
                scalar, scalar, scalar, scalar
        );
    }

    /**
     * Adds all components of the given matrix to this matrix.
     *
     * @param m00 The value at the [0, 0] position of the matrix to add.
     * @param m01 The value at the [0, 1] position of the matrix to add.
     * @param m02 The value at the [0, 2] position of the matrix to add.
     * @param m03 The value at the [0, 3] position of the matrix to add.
     * @param m10 The value at the [1, 0] position of the matrix to add.
     * @param m11 The value at the [1, 1] position of the matrix to add.
     * @param m12 The value at the [1, 2] position of the matrix to add.
     * @param m13 The value at the [1, 3] position of the matrix to add.
     * @param m20 The value at the [2, 0] position of the matrix to add.
     * @param m21 The value at the [2, 1] position of the matrix to add.
     * @param m22 The value at the [2, 2] position of the matrix to add.
     * @param m23 The value at the [2, 3] position of the matrix to add.
     * @param m30 The value at the [3, 0] position of the matrix to add.
     * @param m31 The value at the [3, 1] position of the matrix to add.
     * @param m32 The value at the [3, 2] position of the matrix to add.
     * @param m33 The value at the [3, 3] position of the matrix to add.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable add(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        return this.set(
                this.m00() + m00, this.m01() + m01, this.m02() + m02, this.m03() + m03,
                this.m10() + m10, this.m11() + m11, this.m12() + m12, this.m13() + m13,
                this.m20() + m20, this.m21() + m21, this.m22() + m22, this.m23() + m23,
                this.m30() + m30, this.m31() + m31, this.m32() + m32, this.m33() + m33
        );
    }

    /**
     * Adds all components of the given matrix to this matrix.
     *
     * @param other the matrix to add.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable add(@NotNull IMatrix4 other) {
        return this.add(
                other.m00(), other.m01(), other.m02(), other.m03(),
                other.m10(), other.m11(), other.m12(), other.m13(),
                other.m20(), other.m21(), other.m22(), other.m23(),
                other.m30(), other.m31(), other.m32(), other.m33()
        );
    }

    /**
     * Subtracts all components of the given matrix from this matrix.
     *
     * @param m00 The value at the [0, 0] position of the matrix to subtract.
     * @param m01 The value at the [0, 1] position of the matrix to subtract.
     * @param m02 The value at the [0, 2] position of the matrix to subtract.
     * @param m03 The value at the [0, 3] position of the matrix to subtract.
     * @param m10 The value at the [1, 0] position of the matrix to subtract.
     * @param m11 The value at the [1, 1] position of the matrix to subtract.
     * @param m12 The value at the [1, 2] position of the matrix to subtract.
     * @param m13 The value at the [1, 3] position of the matrix to subtract.
     * @param m20 The value at the [2, 0] position of the matrix to subtract.
     * @param m21 The value at the [2, 1] position of the matrix to subtract.
     * @param m22 The value at the [2, 2] position of the matrix to subtract.
     * @param m23 The value at the [2, 3] position of the matrix to subtract.
     * @param m30 The value at the [3, 0] position of the matrix to subtract.
     * @param m31 The value at the [3, 1] position of the matrix to subtract.
     * @param m32 The value at the [3, 2] position of the matrix to subtract.
     * @param m33 The value at the [3, 3] position of the matrix to subtract.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable sub(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        return this.set(
                this.m00() - m00, this.m01() - m01, this.m02() - m02, this.m03() - m03,
                this.m10() - m10, this.m11() - m11, this.m12() - m12, this.m13() - m13,
                this.m20() - m20, this.m21() - m21, this.m22() - m22, this.m23() - m23,
                this.m30() - m30, this.m31() - m31, this.m32() - m32, this.m33() - m33
        );
    }

    /**
     * Subtracts all components of the given matrix from this matrix.
     *
     * @param other the matrix to subtract.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable sub(@NotNull IMatrix4 other) {
        return this.sub(
                other.m00(), other.m01(), other.m02(), other.m03(),
                other.m10(), other.m11(), other.m12(), other.m13(),
                other.m20(), other.m21(), other.m22(), other.m23(),
                other.m30(), other.m31(), other.m32(), other.m33()
        );
    }

    /**
     * Translates this matrix by the given vector.
     *
     * @param x the x component of the translation vector.
     * @param y the y component of the translation vector.
     * @param z the z component of the translation vector.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable translate(double x, double y, double z) {
        return this.m03(this.m00() * x + this.m01() * y + this.m02() * z + this.m03())
                .m13(this.m10() * x + this.m11() * y + this.m12() * z + this.m13())
                .m23(this.m20() * x + this.m21() * y + this.m22() * z + this.m23())
                .m33(this.m30() * x + this.m31() * y + this.m32() * z + this.m33());
    }

    /**
     * Translates this matrix by the given vector.
     *
     * @param vector the translation vector.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable translate(@NotNull IVector3 vector) {
        return this.translate(vector.x(), vector.y(), vector.z());
    }

    /**
     * Rotates this matrix around the x-axis, should only really be used with the identity matrix.
     * <p>
     * See {@link #rotateX(double)} for a more general rotation method.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateXDirect(double degrees) {
        // Reduce the angle to a value between 0 and 360, making sure to handle negative angles.
        if ((degrees = (degrees % 360 + 360) % 360) == 0) {
            return this;
        }
        final var radians = Math.toRadians(degrees);
        final var cos = Math.cos(radians);
        final var sin = Math.sin(radians);

        return this.m11(cos).m12(-sin).m21(sin).m22(cos);
    }

    /**
     * Rotates this matrix around the y-axis, should only really be used with the identity matrix.
     * <p>
     * See {@link #rotateY(double)} for a more general rotation method.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateYDirect(double degrees) {
        // Reduce the angle to a value between 0 and 360, making sure to handle negative angles.
        if ((degrees = (degrees % 360 + 360) % 360) == 0) {
            return this;
        }
        final double radians = Math.toRadians(degrees);
        final double cos = Math.cos(radians);
        final double sin = Math.sin(radians);

        return this.m00(cos).m02(sin).m20(-sin).m22(cos);
    }

    /**
     * Rotates this matrix around the z-axis, should only really be used with the identity matrix.
     * <p>
     * See {@link #rotateZ(double)} for a more general rotation method.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateZDirect(double degrees) {
        // Reduce the angle to a value between 0 and 360, making sure to handle negative angles.
        if ((degrees = (degrees % 360 + 360) % 360) == 0) {
            return this;
        }
        final double radians = Math.toRadians(degrees);
        final double cos = Math.cos(radians);
        final double sin = Math.sin(radians);

        return this.m00(cos).m01(-sin).m10(sin).m11(cos);
    }

    /**
     * Rotates this matrix along the X axis by the given angle by multiplying it with a rotation matrix.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @NotNull
    @SuppressWarnings("DuplicatedCode")
    default IMatrix4Mutable rotateX(double degrees) {
        if (this.isIdentity()) {
            return this.rotateXDirect(degrees);
        }

        // Reduce the angle to a value between 0 and 360, making sure to handle negative angles.
        degrees = (degrees % 360 + 360) % 360;
        if (degrees == 0) {
            return this;
        }

        // Gather the values we need to calculate the rotation matrix.
        final var radians = Math.toRadians(degrees);
        final var cos = Math.cos(radians);
        final var sin = Math.sin(radians);

        // The rotation matrix for rotating around the x-axis would be:
        // identity.m11(cos).m12(-sin).m21(sin).m22(cos);
        // Which looks like this:
        // 1  0  0  0
        // 0  cos -sin 0
        // 0  sin cos  0
        // 0  0  0  1

        final double m01 = this.m01() * cos + this.m02() * sin;
        final double m02 = this.m01() * (-sin) + this.m02() * cos;
        final double m11 = this.m11() * cos + this.m12() * sin;
        final double m12 = this.m11() * (-sin) + this.m12() * cos;
        final double m21 = this.m21() * cos + this.m22() * sin;
        final double m22 = this.m21() * (-sin) + this.m22() * cos;
        final double m31 = this.m31() * cos + this.m32() * sin;
        final double m32 = this.m31() * (-sin) + this.m32() * cos;

        return this.m01(m01).m02(m02).m11(m11).m12(m12).m21(m21).m22(m22).m31(m31).m32(m32);
    }

    /**
     * Rotates this matrix along the Y axis by the given angle by multiplying it with a rotation matrix.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @NotNull
    @SuppressWarnings("DuplicatedCode")
    default IMatrix4Mutable rotateY(double degrees) {
        if (this.isIdentity()) {
            return this.rotateYDirect(degrees);
        }

        // Reduce the angle to a value between 0 and 360, making sure to handle negative angles.
        degrees = (degrees % 360 + 360) % 360;
        if (degrees == 0) {
            return this;
        }

        // Gather the values we need to calculate the rotation matrix.
        final var radians = Math.toRadians(degrees);
        final var cos = Math.cos(radians);
        final var sin = Math.sin(radians);

        // The rotation matrix for rotating around the y-axis would be:
        // identity.m00(cos).m02(sin).m20(-sin).m22(cos);
        // Which looks like this:
        // cos  0  sin  0
        // 0    1  0    0
        // -sin 0  cos  0
        // 0    0  0    1

        final double m00 = this.m00() * cos + this.m02() * (-sin);
        final double m02 = this.m00() * sin + this.m02() * cos;
        final double m10 = this.m10() * cos + this.m12() * (-sin);
        final double m12 = this.m10() * sin + this.m12() * cos;
        final double m20 = this.m20() * cos + this.m22() * (-sin);
        final double m22 = this.m20() * sin + this.m22() * cos;
        final double m30 = this.m30() * cos + this.m32() * (-sin);
        final double m32 = this.m30() * sin + this.m32() * cos;

        return this.m00(m00).m02(m02).m10(m10).m12(m12).m20(m20).m22(m22).m30(m30).m32(m32);
    }

    /**
     * Rotates this matrix along the Z axis by the given angle by multiplying it with a rotation matrix.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @NotNull
    @SuppressWarnings("DuplicatedCode")
    default IMatrix4Mutable rotateZ(double degrees) {
        if (this.isIdentity()) {
            return this.rotateZDirect(degrees);
        }

        // Reduce the angle to a value between 0 and 360, making sure to handle negative angles.
        degrees = (degrees % 360 + 360) % 360;
        if (degrees == 0) {
            return this;
        }

        // Gather the values we need to calculate the rotation matrix.
        final var radians = Math.toRadians(degrees);
        final var cos = Math.cos(radians);
        final var sin = Math.sin(radians);

        // The rotation matrix for rotating around the z-axis would be:
        // identity.m00(cos).m01(-sin).m10(sin).m11(cos);
        // Which looks like this:
        // cos  -sin 0  0
        // sin  cos  0  0
        // 0    0    1  0
        // 0    0    0  1

        final double m00 = this.m00() * cos + this.m01() * sin;
        final double m01 = this.m00() * (-sin) + this.m01() * cos;
        final double m10 = this.m10() * cos + this.m11() * sin;
        final double m11 = this.m10() * (-sin) + this.m11() * cos;
        final double m20 = this.m20() * cos + this.m21() * sin;
        final double m21 = this.m20() * (-sin) + this.m21() * cos;
        final double m30 = this.m30() * cos + this.m31() * sin;
        final double m31 = this.m30() * (-sin) + this.m31() * cos;

        return this.m00(m00).m01(m01).m10(m10).m11(m11).m20(m20).m21(m21).m30(m30).m31(m31);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the XYZ convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateXYZ(double xDegrees, double yDegrees, double zDegrees) {
        return this.rotateX(xDegrees).rotateY(yDegrees).rotateZ(zDegrees);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the XYZ convention.
     *
     * @param vec the vector containing the angles of rotation, in degrees, around each axis.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateXYZ(@NotNull IVector3 vec) {
        return this.rotateXYZ(vec.x(), vec.y(), vec.z());
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the ZYX convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateZYX(double xDegrees, double yDegrees, double zDegrees) {
        return this.rotateZ(zDegrees).rotateY(yDegrees).rotateX(xDegrees);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the ZYX convention.
     *
     * @param vec the vector containing the angles of rotation, in degrees, around each axis.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateZYX(@NotNull IVector3 vec) {
        return this.rotateZYX(vec.x(), vec.y(), vec.z());
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the YXZ convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateYXZ(double xDegrees, double yDegrees, double zDegrees) {
        return this.rotateY(yDegrees).rotateX(xDegrees).rotateZ(zDegrees);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the YXZ convention.
     *
     * @param vec the vector containing the angles of rotation, in degrees, around each axis.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateYXZ(@NotNull IVector3 vec) {
        return this.rotateYXZ(vec.x(), vec.y(), vec.z());
    }

    /**
     * Rotates this matrix by the given degrees along each axis, around the given origin point, uses the XYZ convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @param xOrigin  the x component of the origin point.
     * @param yOrigin  the y component of the origin point.
     * @param zOrigin  the z component of the origin point.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateXYZ(double xDegrees, double yDegrees, double zDegrees, double xOrigin, double yOrigin, double zOrigin) {
        return this.translate(xOrigin, yOrigin, zOrigin).
                rotateXYZ(xDegrees, yDegrees, zDegrees).
                translate(-xOrigin, -yOrigin, -zOrigin);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, around the given origin point, uses the XYZ convention.
     *
     * @param vec    the vector containing the angles of rotation, in degrees, around each axis.
     * @param origin the origin point.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateXYZ(@NotNull IVector3 vec, @NotNull IVector3 origin) {
        return this.rotateXYZ(vec.x(), vec.y(), vec.z(), origin.x(), origin.y(), origin.z());
    }

    /**
     * Rotates this matrix by the given degrees along each axis, around the given origin point, uses the ZYX convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @param xOrigin  the x component of the origin point.
     * @param yOrigin  the y component of the origin point.
     * @param zOrigin  the z component of the origin point.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateZYX(double xDegrees, double yDegrees, double zDegrees, double xOrigin, double yOrigin, double zOrigin) {
        return this.translate(xOrigin, yOrigin, zOrigin).
                rotateZYX(xDegrees, yDegrees, zDegrees).
                translate(-xOrigin, -yOrigin, -zOrigin);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, around the given origin point, uses the ZYX convention.
     *
     * @param vec    the vector containing the angles of rotation, in degrees, around each axis.
     * @param origin the origin point.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateZYX(@NotNull IVector3 vec, @NotNull IVector3 origin) {
        return this.rotateZYX(vec.x(), vec.y(), vec.z(), origin.x(), origin.y(), origin.z());
    }

    /**
     * Rotates this matrix by the given degrees along each axis, around the given origin point, uses the YXZ convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @param xOrigin  the x component of the origin point.
     * @param yOrigin  the y component of the origin point.
     * @param zOrigin  the z component of the origin point.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateYXZ(double xDegrees, double yDegrees, double zDegrees, double xOrigin, double yOrigin, double zOrigin) {
        return this.translate(xOrigin, yOrigin, zOrigin).
                rotateYXZ(xDegrees, yDegrees, zDegrees).
                translate(-xOrigin, -yOrigin, -zOrigin);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, around the given origin point, uses the YXZ convention.
     *
     * @param vec    the vector containing the angles of rotation, in degrees, around each axis.
     * @param origin the origin point.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable rotateYXZ(@NotNull IVector3 vec, @NotNull IVector3 origin) {
        return this.rotateYXZ(vec.x(), vec.y(), vec.z(), origin.x(), origin.y(), origin.z());
    }

    /**
     * Scales this matrix by the given vector.
     *
     * @param x the x component of the scale vector.
     * @param y the y component of the scale vector.
     * @param z the z component of the scale vector.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable scale(double x, double y, double z) {
        // Equivalent to: this.mul(IMatrix4Mutable.ofIdentity().scale(x, y, z));
        return this.m00(this.m00() * x).
                m01(this.m01() * y).
                m02(this.m02() * z).
                m10(this.m10() * x).
                m11(this.m11() * y).
                m12(this.m12() * z).
                m20(this.m20() * x).
                m21(this.m21() * y).
                m22(this.m22() * z).
                m30(this.m30() * x).
                m31(this.m31() * y).
                m32(this.m32() * z);
    }

    /**
     * Scales this matrix by the given vector.
     *
     * @param scale the scale vector.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable scale(@NotNull IVector3 scale) {
        return this.scale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Scales this matrix by the given factor.
     *
     * @param scale the scale factor.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable scale(double scale) {
        return this.scale(scale, scale, scale);
    }

    /**
     * Scales this matrix by the given factor around the given point.
     *
     * @param aroundX the x component of the origin point to scale around.
     * @param aroundY the y component of the origin point to scale around.
     * @param aroundZ the z component of the origin point to scale around.
     * @param scaleX  the x component of the scale vector.
     * @param scaleY  the y component of the scale vector.
     * @param scaleZ  the z component of the scale vector.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable scale(double aroundX, double aroundY, double aroundZ,
                                  double scaleX, double scaleY, double scaleZ) {
        return this.translate(aroundX, aroundY, aroundZ).
                scale(scaleX, scaleY, scaleZ).
                translate(-aroundX, -aroundY, -aroundZ);
    }

    /**
     * Scales this matrix by the given factor around the given origin point.
     *
     * @param origin the origin point to scale around.
     * @param scale  the scale vector.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable scale(@NotNull IVector3 origin, @NotNull IVector3 scale) {
        return this.scale(origin.x(), origin.y(), origin.z(), scale.x(), scale.y(), scale.z());
    }

    /**
     * Scales this matrix by the given factor around the given origin point.
     *
     * @param origin the origin point to scale around.
     * @param scale  the scale factor.
     * @return this matrix.
     */
    @NotNull
    default IMatrix4Mutable scale(@NotNull IVector3 origin, double scale) {
        return this.scale(origin.x(), origin.y(), origin.z(), scale, scale, scale);
    }

    @Override
    default boolean isImmutable() {
        return false;
    }

    @Override
    default boolean isMutable() {
        return true;
    }

    @Override
    default IMatrix4Immutable asImmutable() {
        return IMatrix4Immutable.of(this);
    }

    @Override
    default IMatrix4Mutable asMutable() {
        return IMatrix4Mutable.of(this);
    }

}
