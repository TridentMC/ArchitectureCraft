package com.tridevmc.architecture.core.math;

/**
 * An extension of {@link ITrans3} that allows for mutable transforms.
 * <p>
 * Construct using {@link ITrans3Mutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
 * <p>
 * See also: {@link ITrans3Immutable}
 */
public interface ITrans3Mutable extends ITrans3 {

    /**
     * Creates a new mutable transform from the given matrix values.
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
     * @return The new transform.
     */
    static ITrans3Mutable of(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {

        return new Trans3.Mutable(IMatrix4.ofMutable(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33)
        );
    }

    /**
     * Creates a new mutable transform from the values in the given matrix.
     *
     * @param matrix The matrix to copy values from.
     * @return The new transform.
     */
    static ITrans3Mutable of(IMatrix4 matrix) {
        return new Trans3.Mutable(IMatrix4Mutable.of(matrix));
    }

    /**
     * Creates a new mutable transform from the values in the given transform.
     *
     * @param trans The transform to copy values from.
     * @return The new transform.
     */
    static ITrans3Mutable of(ITrans3 trans) {
        return of(trans.matrix());
    }

    /**
     * Gets the underlying matrix of this transform, note that this matrix is mutable as the transform is mutable.
     * <p>
     * Any changes to the matrix will be reflected in the transform.
     *
     * @return The underlying mutable matrix.
     */
    IMatrix4Mutable matrix();

    /**
     * Sets the values of the underlying matrix of this transform.
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
     * @return This transform.
     */
    default ITrans3Mutable setMatrix(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33
    ) {
        this.matrix().set(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
        return this;
    }

    /**
     * Sets the values of the underlying matrix of this transform to the values of the given matrix.
     * <p>
     * Please note that the matrix object being passed is copied, so any changes to the matrix will not be reflected in the transform.
     * If you wish to modify the matrix, use {@link #matrix()}.
     *
     * @param matrix The matrix to set the values of this transform to.
     * @return This transform.
     */
    default ITrans3Mutable setMatrix(IMatrix4 matrix) {
        return this.setMatrix(
                matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03(),
                matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13(),
                matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23(),
                matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()
        );
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
    default ITrans3Immutable asImmutable() {
        return ITrans3Immutable.of(this);
    }

    @Override
    default ITrans3Mutable asMutable() {
        return ITrans3Mutable.of(this);
    }
}
