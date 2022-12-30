package com.tridevmc.architecture.core.math;

/**
 * An extension of {@link IMatrix4} that is immutable.
 * <p>
 * Construct using {@link IMatrix4Immutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
 * <p>
 * See also: {@link IMatrix4Mutable}
 */
public interface IMatrix4Immutable extends IMatrix4 {

    /**
     * Creates a new immutable matrix with the given values.
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
    static IMatrix4Immutable of(double m00, double m01, double m02, double m03,
                                double m10, double m11, double m12, double m13,
                                double m20, double m21, double m22, double m23,
                                double m30, double m31, double m32, double m33) {
        return new Matrix4(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    /**
     * Creates a new immutable matrix using the given matrix.
     * <p>
     * Please note that this method will always return a new instance, even if the given matrix is already immutable.
     * This has performance implications, it's recommended to use {@link IMatrix4#asImmutable()} instead.
     *
     * @param matrix The matrix to copy.
     * @return The new matrix.
     */
    static IMatrix4Immutable of(IMatrix4 matrix) {
        return of(
                matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03(),
                matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13(),
                matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23(),
                matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()
        );
    }

    @Override
    default boolean isImmutable() {
        return true;
    }

    @Override
    default boolean isMutable() {
        return false;
    }

    @Override
    default IMatrix4Immutable asImmutable() {
        // We are immutable, so just return ourselves. It's not like we can be any more immutable.
        return this;
    }

    @Override
    default IMatrix4Mutable asMutable() {
        return IMatrix4Mutable.of(this);
    }
}
