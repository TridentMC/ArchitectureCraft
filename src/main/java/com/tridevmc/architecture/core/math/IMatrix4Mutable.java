package com.tridevmc.architecture.core.math;

/**
 * An extension of {@link IMatrix4} that allows for mutation of the matrix.
 * <p>
 * Construct using {@link IMatrix4Mutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
 *
 * @see IMatrix4
 */
public interface IMatrix4Mutable extends IMatrix4 {

    /**
     * Creates a new mutable matrix with the given values.
     *
     * @param m00 The value at row 0, column 0.
     * @param m01 The value at row 0, column 1.
     * @param m02 The value at row 0, column 2.
     * @param m03 The value at row 0, column 3.
     * @param m10 The value at row 1, column 0.
     * @param m11 The value at row 1, column 1.
     * @param m12 The value at row 1, column 2.
     * @param m13 The value at row 1, column 3.
     * @param m20 The value at row 2, column 0.
     * @param m21 The value at row 2, column 1.
     * @param m22 The value at row 2, column 2.
     * @param m23 The value at row 2, column 3.
     * @param m30 The value at row 3, column 0.
     * @param m31 The value at row 3, column 1.
     * @param m32 The value at row 3, column 2.
     * @param m33 The value at row 3, column 3.
     * @return The new matrix.
     */
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
    static IMatrix4Mutable of(IMatrix4 matrix) {
        return of(
                matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03(),
                matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13(),
                matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23(),
                matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()
        );
    }

    /**
     * Creates a new mutable matrix with the values of the identity matrix.
     *
     * @return The new matrix.
     */
    static IMatrix4Mutable ofIdentity() {
        return of(
                IMatrix4.IDENTITY
        );
    }

    /**
     * Sets the value of the matrix at the given row and column.
     *
     * @param i     the row index.
     * @param j     the column index.
     * @param value the value to set.
     */
    default IMatrix4Mutable set(int i, int j, double value) {
        switch (i) {
            case 0 -> {
                switch (j) {
                    case 0 -> this.setM00(value);
                    case 1 -> this.setM01(value);
                    case 2 -> this.setM02(value);
                    case 3 -> this.setM03(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            case 1 -> {
                switch (j) {
                    case 0 -> this.setM10(value);
                    case 1 -> this.setM11(value);
                    case 2 -> this.setM12(value);
                    case 3 -> this.setM13(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            case 2 -> {
                switch (j) {
                    case 0 -> this.setM20(value);
                    case 1 -> this.setM21(value);
                    case 2 -> this.setM22(value);
                    case 3 -> this.setM23(value);
                    default -> throw new IndexOutOfBoundsException(
                            String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                    );
                }
            }
            case 3 -> {
                switch (j) {
                    case 0 -> this.setM30(value);
                    case 1 -> this.setM31(value);
                    case 2 -> this.setM32(value);
                    case 3 -> this.setM33(value);
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
     * @param m00 The value at row 0, column 0.
     * @param m01 The value at row 0, column 1.
     * @param m02 The value at row 0, column 2.
     * @param m03 The value at row 0, column 3.
     * @param m10 The value at row 1, column 0.
     * @param m11 The value at row 1, column 1.
     * @param m12 The value at row 1, column 2.
     * @param m13 The value at row 1, column 3.
     * @param m20 The value at row 2, column 0.
     * @param m21 The value at row 2, column 1.
     * @param m22 The value at row 2, column 2.
     * @param m23 The value at row 2, column 3.
     * @param m30 The value at row 3, column 0.
     * @param m31 The value at row 3, column 1.
     * @param m32 The value at row 3, column 2.
     * @param m33 The value at row 3, column 3.
     * @return The matrix.
     */
    default IMatrix4Mutable set(double m00, double m01, double m02, double m03,
                                double m10, double m11, double m12, double m13,
                                double m20, double m21, double m22, double m23,
                                double m30, double m31, double m32, double m33) {
        return this.setM00(m00).setM01(m01).setM02(m02).setM03(m03)
                .setM10(m10).setM11(m11).setM12(m12).setM13(m13)
                .setM20(m20).setM21(m21).setM22(m22).setM23(m23)
                .setM30(m30).setM31(m31).setM32(m32).setM33(m33);
    }

    /**
     * Sets the value of the matrix at the 0, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM00(double value);

    /**
     * Sets the value of the matrix at the 0, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM01(double value);

    /**
     * Sets the value of the matrix at the 0, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM02(double value);

    /**
     * Sets the value of the matrix at the 0, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM03(double value);

    /**
     * Sets the value of the matrix at the 1, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM10(double value);

    /**
     * Sets the value of the matrix at the 1, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM11(double value);

    /**
     * Sets the value of the matrix at the 1, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM12(double value);

    /**
     * Sets the value of the matrix at the 1, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM13(double value);

    /**
     * Sets the value of the matrix at the 2, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM20(double value);

    /**
     * Sets the value of the matrix at the 2, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM21(double value);

    /**
     * Sets the value of the matrix at the 2, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM22(double value);

    /**
     * Sets the value of the matrix at the 2, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM23(double value);

    /**
     * Sets the value of the matrix at the 3, 0 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM30(double value);

    /**
     * Sets the value of the matrix at the 3, 1 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM31(double value);

    /**
     * Sets the value of the matrix at the 3, 2 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM32(double value);

    /**
     * Sets the value of the matrix at the 3, 3 position.
     *
     * @param value the value to set.
     * @return this matrix.
     */
    IMatrix4Mutable setM33(double value);

    /**
     * Multiplies this matrix by the given matrix, storing the result in this matrix.
     *
     * @param m00 The value at the 0, 0 position of the matrix to multiply by.
     * @param m01 The value at the 0, 1 position of the matrix to multiply by.
     * @param m02 The value at the 0, 2 position of the matrix to multiply by.
     * @param m03 The value at the 0, 3 position of the matrix to multiply by.
     * @param m10 The value at the 1, 0 position of the matrix to multiply by.
     * @param m11 The value at the 1, 1 position of the matrix to multiply by.
     * @param m12 The value at the 1, 2 position of the matrix to multiply by.
     * @param m13 The value at the 1, 3 position of the matrix to multiply by.
     * @param m20 The value at the 2, 0 position of the matrix to multiply by.
     * @param m21 The value at the 2, 1 position of the matrix to multiply by.
     * @param m22 The value at the 2, 2 position of the matrix to multiply by.
     * @param m23 The value at the 2, 3 position of the matrix to multiply by.
     * @param m30 The value at the 3, 0 position of the matrix to multiply by.
     * @param m31 The value at the 3, 1 position of the matrix to multiply by.
     * @param m32 The value at the 3, 2 position of the matrix to multiply by.
     * @param m33 The value at the 3, 3 position of the matrix to multiply by.
     * @return this matrix.
     */
    default IMatrix4Mutable mul(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        return this.set(
                this.m00() * m00 + this.m01() * m10 + this.m02() * m20 + this.m03() * m30,
                this.m00() * m01 + this.m01() * m11 + this.m02() * m21 + this.m03() * m31,
                this.m00() * m02 + this.m01() * m12 + this.m02() * m22 + this.m03() * m32,
                this.m00() * m03 + this.m01() * m13 + this.m02() * m23 + this.m03() * m33,
                this.m10() * m00 + this.m11() * m10 + this.m12() * m20 + this.m13() * m30,
                this.m10() * m01 + this.m11() * m11 + this.m12() * m21 + this.m13() * m31,
                this.m10() * m02 + this.m11() * m12 + this.m12() * m22 + this.m13() * m32,
                this.m10() * m03 + this.m11() * m13 + this.m12() * m23 + this.m13() * m33,
                this.m20() * m00 + this.m21() * m10 + this.m22() * m20 + this.m23() * m30,
                this.m20() * m01 + this.m21() * m11 + this.m22() * m21 + this.m23() * m31,
                this.m20() * m02 + this.m21() * m12 + this.m22() * m22 + this.m23() * m32,
                this.m20() * m03 + this.m21() * m13 + this.m22() * m23 + this.m23() * m33,
                this.m30() * m00 + this.m31() * m10 + this.m32() * m20 + this.m33() * m30,
                this.m30() * m01 + this.m31() * m11 + this.m32() * m21 + this.m33() * m31,
                this.m30() * m02 + this.m31() * m12 + this.m32() * m22 + this.m33() * m32,
                this.m30() * m03 + this.m31() * m13 + this.m32() * m23 + this.m33() * m33
        );
    }

    /**
     * Multiplies this matrix by the specified matrix, storing the result in this matrix.
     *
     * @param other the matrix to multiply by.
     * @return this matrix.
     */
    default IMatrix4Mutable mul(IMatrix4 other) {
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
     * @param m00 The value at the 0, 0 position of the matrix to add.
     * @param m01 The value at the 0, 1 position of the matrix to add.
     * @param m02 The value at the 0, 2 position of the matrix to add.
     * @param m03 The value at the 0, 3 position of the matrix to add.
     * @param m10 The value at the 1, 0 position of the matrix to add.
     * @param m11 The value at the 1, 1 position of the matrix to add.
     * @param m12 The value at the 1, 2 position of the matrix to add.
     * @param m13 The value at the 1, 3 position of the matrix to add.
     * @param m20 The value at the 2, 0 position of the matrix to add.
     * @param m21 The value at the 2, 1 position of the matrix to add.
     * @param m22 The value at the 2, 2 position of the matrix to add.
     * @param m23 The value at the 2, 3 position of the matrix to add.
     * @param m30 The value at the 3, 0 position of the matrix to add.
     * @param m31 The value at the 3, 1 position of the matrix to add.
     * @param m32 The value at the 3, 2 position of the matrix to add.
     * @param m33 The value at the 3, 3 position of the matrix to add.
     * @return this matrix.
     */
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
    default IMatrix4Mutable add(IMatrix4 other) {
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
     * @param m00 The value at the 0, 0 position of the matrix to subtract.
     * @param m01 The value at the 0, 1 position of the matrix to subtract.
     * @param m02 The value at the 0, 2 position of the matrix to subtract.
     * @param m03 The value at the 0, 3 position of the matrix to subtract.
     * @param m10 The value at the 1, 0 position of the matrix to subtract.
     * @param m11 The value at the 1, 1 position of the matrix to subtract.
     * @param m12 The value at the 1, 2 position of the matrix to subtract.
     * @param m13 The value at the 1, 3 position of the matrix to subtract.
     * @param m20 The value at the 2, 0 position of the matrix to subtract.
     * @param m21 The value at the 2, 1 position of the matrix to subtract.
     * @param m22 The value at the 2, 2 position of the matrix to subtract.
     * @param m23 The value at the 2, 3 position of the matrix to subtract.
     * @param m30 The value at the 3, 0 position of the matrix to subtract.
     * @param m31 The value at the 3, 1 position of the matrix to subtract.
     * @param m32 The value at the 3, 2 position of the matrix to subtract.
     * @param m33 The value at the 3, 3 position of the matrix to subtract.
     * @return this matrix.
     */
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
    default IMatrix4Mutable sub(IMatrix4 other) {
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
    default IMatrix4Mutable translate(double x, double y, double z) {
        return this.setM03(this.m03() + x).
                setM13(this.m13() + y).
                setM23(this.m23() + z);
    }

    /**
     * Translates this matrix by the given vector.
     *
     * @param vector the translation vector.
     * @return this matrix.
     */
    default IMatrix4Mutable translate(IVector3 vector) {
        return this.translate(vector.x(), vector.y(), vector.z());
    }

    /**
     * Rotates the matrix by adjusting the values in the given rows and columns, should only really be used with the identity matrix.
     * <p>
     * See {@link IMatrix4Mutable#rotateX(double)}, {@link IMatrix4Mutable#rotateY(double)}, and {@link IMatrix4Mutable#rotateZ(double)} for more general rotation methods.
     *
     * @param degrees the angle of rotation, in degrees.
     * @param i       the first index of the rows and columns to rotate.
     * @param j       the second index of the rows and columns to rotate.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateDirect(int i, int j, double degrees) {
        var radians = Math.toRadians(degrees);
        var cos = Math.cos(radians);
        var sin = Math.sin(radians);

        var mII = this.get(i, i);
        var mIJ = this.get(i, j);
        var mJI = this.get(j, i);
        var mJJ = this.get(j, j);

        // Using the mul() method requires a temporary matrix, which we want to avoid.
        // This is the equivalent of: this.mul(rotationMatrix);
        this.set(i, i, mII * cos + mIJ * -sin);
        this.set(i, j, mII * sin + mIJ * cos);
        this.set(j, i, mJI * cos + mJJ * -sin);
        this.set(j, j, mJI * sin + mJJ * cos);

        return this;
    }

    /**
     * Rotates this matrix around the x-axis, should only really be used with the identity matrix.
     * <p>
     * See {@link #rotateX(double)} for a more general rotation method.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateXDirect(double degrees) {
        return degrees % 360 != 0 ? this.rotateDirect(1, 2, degrees) : this;
    }

    /**
     * Rotates this matrix around the y-axis, should only really be used with the identity matrix.
     * <p>
     * See {@link #rotateY(double)} for a more general rotation method.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateYDirect(double degrees) {
        return degrees % 360 != 0 ? this.rotateDirect(2, 0, degrees) : this;
    }

    /**
     * Rotates this matrix around the z-axis, should only really be used with the identity matrix.
     * <p>
     * See {@link #rotateZ(double)} for a more general rotation method.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateZDirect(double degrees) {
        return degrees % 360 != 0 ? this.rotateDirect(0, 1, degrees) : this;
    }

    /**
     * Rotates this matrix along the X axis by the given angle by multiplying it with a rotation matrix.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @SuppressWarnings("DuplicatedCode")
    default IMatrix4Mutable rotateX(double degrees) {
        if (this.isIdentity()) {
            return this.rotateXDirect(degrees);
        }

        // Using the mul() method requires a temporary matrix, which we want to avoid.
        // The math here is equivalent to: this.mul(IMutableMatrix4.ofIdentity().rotateXDirect(degrees));
        var radians = Math.toRadians(degrees);
        var cos = Math.cos(radians);
        var sin = Math.sin(radians);

        // Our rotateXDirect matrix would look like this:
        // 1  0   0  0
        // 0  cos sin 0
        // 0 -sin cos 0
        // 0  0   0  1

        // We could use the mul method and pass in each value but that would require a lot of extra calculations.
        // Instead, we can just set the values directly.

        var m01 = this.m01() * cos + this.m02() * -sin;
        var m02 = this.m01() * sin + this.m02() * cos;
        var m11 = this.m11() * cos + this.m12() * -sin;
        var m12 = this.m11() * sin + this.m12() * cos;
        var m21 = this.m21() * cos + this.m22() * -sin;
        var m22 = this.m21() * sin + this.m22() * cos;
        var m31 = this.m31() * cos + this.m32() * -sin;
        var m32 = this.m31() * sin + this.m32() * cos;

        return this.setM01(m01).
                setM02(m02).
                setM11(m11).
                setM12(m12).
                setM21(m21).
                setM22(m22).
                setM31(m31).
                setM32(m32);
    }

    /**
     * Rotates this matrix along the Y axis by the given angle by multiplying it with a rotation matrix.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @SuppressWarnings("DuplicatedCode")
    default IMatrix4Mutable rotateY(double degrees) {
        if (this.isIdentity()) {
            return this.rotateYDirect(degrees);
        }

        // Using the mul() method requires a temporary matrix, which we want to avoid.
        // The math here is equivalent to: this.mul(IMutableMatrix4.ofIdentity().rotateYDirect(degrees));
        var radians = Math.toRadians(degrees);
        var cos = Math.cos(radians);
        var sin = Math.sin(radians);

        // Our rotateYDirect matrix would look like this:
        // cos 0 -sin 0
        // 0   1  0   0
        // sin 0  cos 0
        // 0   0  0   1

        // We could use the mul method and pass in each value but that would require a lot of extra calculations.
        // Instead, we can just set the values directly.

        var m00 = this.m00() * cos + this.m02() * sin;
        var m02 = this.m00() * -sin + this.m02() * cos;
        var m10 = this.m10() * cos + this.m12() * sin;
        var m12 = this.m10() * -sin + this.m12() * cos;
        var m20 = this.m20() * cos + this.m22() * sin;
        var m22 = this.m20() * -sin + this.m22() * cos;
        var m30 = this.m30() * cos + this.m32() * sin;
        var m32 = this.m30() * -sin + this.m32() * cos;

        return this.setM00(m00).
                setM02(m02).
                setM10(m10).
                setM12(m12).
                setM20(m20).
                setM22(m22).
                setM30(m30).
                setM32(m32);
    }

    /**
     * Rotates this matrix along the Z axis by the given angle by multiplying it with a rotation matrix.
     *
     * @param degrees the angle of rotation, in degrees.
     * @return this matrix.
     */
    @SuppressWarnings("DuplicatedCode")
    default IMatrix4Mutable rotateZ(double degrees) {
        if (this.isIdentity()) {
            return this.rotateZDirect(degrees);
        }

        // Using the mul() method requires a temporary matrix, which we want to avoid.
        // The math here is equivalent to: this.mul(IMutableMatrix4.ofIdentity().rotateZDirect(degrees));
        var radians = Math.toRadians(degrees);
        var cos = Math.cos(radians);
        var sin = Math.sin(radians);

        // Our rotateZDirect matrix would look like this:
        // cos -sin 0 0
        // sin cos  0 0
        // 0   0    1 0
        // 0   0    0 1

        // We could use the mul method and pass in each value but that would require a lot of extra calculations.
        // Instead, we can just set the values directly.

        var m00 = this.m00() * cos + this.m01() * -sin;
        var m01 = this.m00() * sin + this.m01() * cos;
        var m10 = this.m10() * cos + this.m11() * -sin;
        var m11 = this.m10() * sin + this.m11() * cos;
        var m20 = this.m20() * cos + this.m21() * -sin;
        var m21 = this.m20() * sin + this.m21() * cos;
        var m30 = this.m30() * cos + this.m31() * -sin;
        var m31 = this.m30() * sin + this.m31() * cos;

        return this.setM00(m00).
                setM01(m01).
                setM10(m10).
                setM11(m11).
                setM20(m20).
                setM21(m21).
                setM30(m30).
                setM31(m31);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the XYZ convention.
     *
     * @param xDegrees the angle of rotation, in degrees, around the x-axis.
     * @param yDegrees the angle of rotation, in degrees, around the y-axis.
     * @param zDegrees the angle of rotation, in degrees, around the z-axis.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateXYZ(double xDegrees, double yDegrees, double zDegrees) {
        return this.rotateX(xDegrees).rotateY(yDegrees).rotateZ(zDegrees);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the XYZ convention.
     *
     * @param vec the vector containing the angles of rotation, in degrees, around each axis.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateXYZ(IVector3 vec) {
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
    default IMatrix4Mutable rotateZYX(double xDegrees, double yDegrees, double zDegrees) {
        return this.rotateZ(zDegrees).rotateY(yDegrees).rotateX(xDegrees);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the ZYX convention.
     *
     * @param vec the vector containing the angles of rotation, in degrees, around each axis.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateZYX(IVector3 vec) {
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
    default IMatrix4Mutable rotateYXZ(double xDegrees, double yDegrees, double zDegrees) {
        return this.rotateY(yDegrees).rotateX(xDegrees).rotateZ(zDegrees);
    }

    /**
     * Rotates this matrix by the given degrees along each axis, uses the YXZ convention.
     *
     * @param vec the vector containing the angles of rotation, in degrees, around each axis.
     * @return this matrix.
     */
    default IMatrix4Mutable rotateYXZ(IVector3 vec) {
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
    default IMatrix4Mutable rotateXYZ(IVector3 vec, IVector3 origin) {
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
    default IMatrix4Mutable rotateZYX(IVector3 vec, IVector3 origin) {
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
    default IMatrix4Mutable rotateYXZ(IVector3 vec, IVector3 origin) {
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
    default IMatrix4Mutable scale(double x, double y, double z) {
        return this.setM00(this.m00() * x).
                setM11(this.m11() * y).
                setM22(this.m22() * z);
    }

    /**
     * Scales this matrix by the given vector.
     *
     * @param scale the scale vector.
     * @return this matrix.
     */
    default IMatrix4Mutable scale(IVector3 scale) {
        return this.scale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Scales this matrix by the given factor.
     *
     * @param scale the scale factor.
     * @return this matrix.
     */
    default IMatrix4Mutable scale(double scale) {
        return this.scale(scale, scale, scale);
    }

    /**
     * Scales this matrix by the given factor around the given origin point.
     *
     * @param x       the x component of the scale vector.
     * @param y       the y component of the scale vector.
     * @param z       the z component of the scale vector.
     * @param xOrigin the x component of the origin point to scale around.
     * @param yOrigin the y component of the origin point to scale around.
     * @param zOrigin the z component of the origin point to scale around.
     * @return this matrix.
     */
    default IMatrix4Mutable scale(double x, double y, double z,
                                  double xOrigin, double yOrigin, double zOrigin) {
        return this.translate(xOrigin, yOrigin, zOrigin).
                scale(x, y, z).
                translate(-xOrigin, -yOrigin, -zOrigin);
    }

    /**
     * Scales this matrix by the given factor around the given origin point.
     *
     * @param scale  the scale vector.
     * @param origin the origin point to scale around.
     * @return this matrix.
     */
    default IMatrix4Mutable scale(IVector3 scale, IVector3 origin) {
        return this.scale(scale.x(), scale.y(), scale.z(),
                origin.x(), origin.y(), origin.z());
    }

    /**
     * Scales this matrix by the given factor around the given origin point.
     *
     * @param scale  the scale factor.
     * @param origin the origin point to scale around.
     * @return this matrix.
     */
    default IMatrix4Mutable scale(double scale, IVector3 origin) {
        return this.scale(scale, scale, scale, origin.x(), origin.y(), origin.z());
    }

    @Override
    default boolean isImmutable() {
        return false;
    }

    @Override
    default boolean isMutable() {
        return true;
    }
}
