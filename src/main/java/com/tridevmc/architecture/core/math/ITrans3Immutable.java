package com.tridevmc.architecture.core.math;

/**
 * An extension of {@link ITrans3} that prevents modification of the transform.
 * <p>
 * Construct using {@link ITrans3Immutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
 * <p>
 * See also: {@link ITrans3Mutable}
 */
public interface ITrans3Immutable extends ITrans3 {

    /**
     * Creates a new immutable transform from the given matrix values.
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
    static ITrans3Immutable of(
            double m00, double m01, double m02, double m03,
            double m10, double m11, double m12, double m13,
            double m20, double m21, double m22, double m23,
            double m30, double m31, double m32, double m33) {
        return new Trans3(IMatrix4.ofImmutable(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33)
        );
    }

    /**
     * Creates a new immutable transform from the values in the given matrix.
     *
     * @param matrix The matrix to copy values from.
     * @return The new transform.
     */
    static ITrans3Immutable of(IMatrix4 matrix) {
        return of(
                matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03(),
                matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13(),
                matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23(),
                matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()
        );
    }


    /**
     * Creates a new immutable transform that offsets any point in space by the given values.
     *
     * @param transX The x offset to apply.
     * @param transY The y offset to apply.
     * @param transZ The z offset to apply.
     * @return the new transform.
     */
    static ITrans3Immutable ofTranslation(double transX, double transY, double transZ) {
        return of(
                1, 0, 0, transX,
                0, 1, 0, transY,
                0, 0, 1, transZ,
                0, 0, 0, 1
        );
    }

    /**
     * Creates a new immutable transform that offsets any point in space by the given values.
     *
     * @param transPos The offset to apply.
     * @return the new transform.
     */
    static ITrans3Immutable ofTranslation(IVector3 transPos) {
        return ofTranslation(transPos.x(), transPos.y(), transPos.z());
    }

    /**
     * Creates a new immutable transform that scales any point in space by the given values.
     *
     * @param scaleX The x scale to apply.
     * @param scaleY The y scale to apply.
     * @param scaleZ The z scale to apply.
     * @return the new transform.
     */
    static ITrans3Immutable ofScale(double scaleX, double scaleY, double scaleZ) {
        return of(
                scaleX, 0, 0, 0,
                0, scaleY, 0, 0,
                0, 0, scaleZ, 0,
                0, 0, 0, 1
        );
    }

    /**
     * Creates a new immutable transform that scales any point in space by the given values.
     *
     * @param scale The scale to apply.
     * @return the new transform.
     */
    static ITrans3Immutable ofScale(IVector3 scale) {
        return ofScale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Creates a new immutable transform from the values in the given transform.
     * <p>
     * Please note that this method will always return a new instance, even if the given transform is already immutable.
     * This has performance implications, it's recommended to use {@link ITrans3#asImmutable()} instead.
     *
     * @param trans The transform to copy values from.
     * @return The new transform.
     */
    static ITrans3Immutable of(ITrans3 trans) {
        return of(trans.matrix());
    }


    /**
     * Gets the underlying matrix of this transform, note that this matrix is immutable as the transform is immutable.
     *
     * @return The underlying immutable matrix.
     */
    IMatrix4Immutable matrix();

    @Override
    default boolean isImmutable() {
        return true;
    }

    @Override
    default boolean isMutable() {
        return false;
    }

    @Override
    default ITrans3Immutable asImmutable() {
        return this;
    }

    @Override
    default ITrans3Mutable asMutable() {
        return ITrans3Mutable.of(this);
    }
}
