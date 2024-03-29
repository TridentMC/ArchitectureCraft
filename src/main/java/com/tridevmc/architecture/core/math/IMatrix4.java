package com.tridevmc.architecture.core.math;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable 4x4 matrix, primarily used for transformations.
 * <p>
 * Can be instantiated using the {@link Matrix4#ofImmutable(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)} method.
 */
public interface IMatrix4 {

    IMatrix4Immutable IDENTITY = IMatrix4.ofImmutable(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    );

    ImmutableList<IMatrix4Immutable> TURN_ROTATIONS = ImmutableList.of(
            rotY(0).asImmutable(),
            rotY(90).asImmutable(),
            rotY(180).asImmutable(),
            rotY(270).asImmutable()
    );

    ImmutableList<IMatrix4> SIDE_ROTATIONS = ImmutableList.of(
            /*0, -Y, DOWN */ IDENTITY,
            /*1, +Y, UP   */ rotX(180),
            /*2, -Z, NORTH*/ rotX(90),
            /*3, +Z, SOUTH*/ rotX(-90).rotateY(180).asImmutable(),
            /*4, -X, WEST */ rotZ(-90).rotateY(90).asImmutable(),
            /*5, +X, EAST */ rotZ(90).rotateY(-90).asImmutable()
    );

    // Ideally we could use a static initializer here, but they're not allowed in interfaces.
    ImmutableList<IMatrix4Immutable> SIDE_TURN_ROTATIONS = ImmutableList.of(
            SIDE_ROTATIONS.get(0).asMutable().mul(TURN_ROTATIONS.get(0)).asImmutable(),
            SIDE_ROTATIONS.get(0).asMutable().mul(TURN_ROTATIONS.get(1)).asImmutable(),
            SIDE_ROTATIONS.get(0).asMutable().mul(TURN_ROTATIONS.get(2)).asImmutable(),
            SIDE_ROTATIONS.get(0).asMutable().mul(TURN_ROTATIONS.get(3)).asImmutable(),
            SIDE_ROTATIONS.get(1).asMutable().mul(TURN_ROTATIONS.get(0)).asImmutable(),
            SIDE_ROTATIONS.get(1).asMutable().mul(TURN_ROTATIONS.get(1)).asImmutable(),
            SIDE_ROTATIONS.get(1).asMutable().mul(TURN_ROTATIONS.get(2)).asImmutable(),
            SIDE_ROTATIONS.get(1).asMutable().mul(TURN_ROTATIONS.get(3)).asImmutable(),
            SIDE_ROTATIONS.get(2).asMutable().mul(TURN_ROTATIONS.get(0)).asImmutable(),
            SIDE_ROTATIONS.get(2).asMutable().mul(TURN_ROTATIONS.get(1)).asImmutable(),
            SIDE_ROTATIONS.get(2).asMutable().mul(TURN_ROTATIONS.get(2)).asImmutable(),
            SIDE_ROTATIONS.get(2).asMutable().mul(TURN_ROTATIONS.get(3)).asImmutable(),
            SIDE_ROTATIONS.get(3).asMutable().mul(TURN_ROTATIONS.get(0)).asImmutable(),
            SIDE_ROTATIONS.get(3).asMutable().mul(TURN_ROTATIONS.get(1)).asImmutable(),
            SIDE_ROTATIONS.get(3).asMutable().mul(TURN_ROTATIONS.get(2)).asImmutable(),
            SIDE_ROTATIONS.get(3).asMutable().mul(TURN_ROTATIONS.get(3)).asImmutable(),
            SIDE_ROTATIONS.get(4).asMutable().mul(TURN_ROTATIONS.get(0)).asImmutable(),
            SIDE_ROTATIONS.get(4).asMutable().mul(TURN_ROTATIONS.get(1)).asImmutable(),
            SIDE_ROTATIONS.get(4).asMutable().mul(TURN_ROTATIONS.get(2)).asImmutable(),
            SIDE_ROTATIONS.get(4).asMutable().mul(TURN_ROTATIONS.get(3)).asImmutable(),
            SIDE_ROTATIONS.get(5).asMutable().mul(TURN_ROTATIONS.get(0)).asImmutable(),
            SIDE_ROTATIONS.get(5).asMutable().mul(TURN_ROTATIONS.get(1)).asImmutable(),
            SIDE_ROTATIONS.get(5).asMutable().mul(TURN_ROTATIONS.get(2)).asImmutable(),
            SIDE_ROTATIONS.get(5).asMutable().mul(TURN_ROTATIONS.get(3)).asImmutable()
    );

    @NotNull
    static IMatrix4 getSideTurnRotation(int side, int turn) {
        return SIDE_TURN_ROTATIONS.get(side * 4 + turn);
    }

    @NotNull
    private static IMatrix4Mutable rotX(double deg) {
        return IMatrix4Mutable.ofIdentity().rotateXDirect(deg);
    }

    @NotNull
    private static IMatrix4Mutable rotY(double deg) {
        return IMatrix4Mutable.ofIdentity().rotateYDirect(deg);
    }

    @NotNull
    private static IMatrix4Mutable rotZ(double deg) {
        return IMatrix4Mutable.ofIdentity().rotateZDirect(deg);
    }

    /**
     * Creates a new immutable matrix with the given values.
     * <p>
     * See {@link IMatrix4Immutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
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
    static IMatrix4Immutable ofImmutable(double m00, double m01, double m02, double m03,
                                         double m10, double m11, double m12, double m13,
                                         double m20, double m21, double m22, double m23,
                                         double m30, double m31, double m32, double m33) {
        return IMatrix4Immutable.of(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    /**
     * Creates a new immutable matrix using the given matrix.
     * <p>
     * Please note that this method will always return a new instance, even if the given matrix is already immutable.
     * This has performance implications, it's recommended to use {@link IMatrix4#asImmutable()} instead.
     * <p>
     * See: {@link IMatrix4Immutable#of(IMatrix4)}
     *
     * @param matrix The matrix to copy.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofImmutable(@NotNull IMatrix4 matrix) {
        return IMatrix4Immutable.of(matrix);
    }

    /**
     * Creates a new mutable matrix with the given values.
     * <p>
     * See {@link IMatrix4Mutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)}.
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
    static IMatrix4Mutable ofMutable(double m00, double m01, double m02, double m03,
                                     double m10, double m11, double m12, double m13,
                                     double m20, double m21, double m22, double m23,
                                     double m30, double m31, double m32, double m33) {
        return IMatrix4Mutable.of(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    /**
     * Creates a new mutable matrix using the given matrix.
     * <p>
     * See: {@link IMatrix4Mutable#of(IMatrix4)}
     *
     * @param matrix The matrix to copy.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Mutable ofMutable(@NotNull IMatrix4 matrix) {
        return IMatrix4Mutable.of(matrix);
    }

    /**
     * Gets the value at the [0, 0] position.
     *
     * @return The value at the [0, 0] position.
     */
    double m00();

    /**
     * Gets the value at the [0, 1] position.
     *
     * @return The value at the [0, 1] position.
     */
    double m01();

    /**
     * Gets the value at the [0, 2] position.
     *
     * @return The value at the [0, 2] position.
     */
    double m02();

    /**
     * Gets the value at the [0, 3] position.
     *
     * @return The value at the [0, 3] position.
     */
    double m03();

    /**
     * Gets the value at the [1, 0] position.
     *
     * @return The value at the [1, 0] position.
     */
    double m10();

    /**
     * Gets the value at the [1, 1] position.
     *
     * @return The value at the [1, 1] position.
     */
    double m11();

    /**
     * Gets the value at the [1, 2] position.
     *
     * @return The value at the [1, 2] position.
     */
    double m12();

    /**
     * Gets the value at the [1, 3] position.
     *
     * @return The value at the [1, 3] position.
     */
    double m13();

    /**
     * Gets the value at the [2, 0] position.
     *
     * @return The value at the [2, 0] position.
     */
    double m20();

    /**
     * Gets the value at the [2, 1] position.
     *
     * @return The value at the [2, 1] position.
     */
    double m21();

    /**
     * Gets the value at the [2, 2] position.
     *
     * @return The value at the [2, 2] position.
     */
    double m22();

    /**
     * Gets the value at the [2, 3] position.
     *
     * @return The value at the [2, 3] position.
     */
    double m23();

    /**
     * Gets the value at the [3, 0] position.
     *
     * @return The value at the [3, 0] position.
     */
    double m30();

    /**
     * Gets the value at the [3, 1] position.
     *
     * @return The value at the [3, 1] position.
     */
    double m31();

    /**
     * Gets the value at the [3, 2] position.
     *
     * @return The value at the [3, 2] position.
     */
    double m32();

    /**
     * Gets the value at the [3, 3] position.
     *
     * @return The value at the [3, 3] position.
     */
    double m33();

    /**
     * Determines if this matrix is immutable or not.
     *
     * @return True if this matrix is immutable, false otherwise.
     */
    boolean isImmutable();

    /**
     * Determines if this matrix is mutable or not.
     *
     * @return True if this matrix is mutable, false otherwise.
     */
    boolean isMutable();

    /**
     * Creates an immutable copy of this matrix.
     * <p>
     * If this matrix is already immutable, this method will return this matrix.
     *
     * @return An immutable copy of this matrix.
     */
    @NotNull
    IMatrix4Immutable asImmutable();

    /**
     * Creates a mutable copy of this matrix.
     *
     * @return A mutable copy of this matrix.
     */
    @NotNull
    IMatrix4Mutable asMutable();

    /**
     * Gets the value at the given row and column.
     *
     * @param i the row index. Must be between 0 and 3, inclusive.
     * @param j the column index. Must be between 0 and 3, inclusive.
     * @return The value at the given row and column.
     */
    default double get(int i, int j) {
        return switch (i) {
            case 0 -> switch (j) {
                case 0 -> this.m00();
                case 1 -> this.m01();
                case 2 -> this.m02();
                case 3 -> this.m03();
                default -> throw new IndexOutOfBoundsException(
                        String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                );
            };
            case 1 -> switch (j) {
                case 0 -> this.m10();
                case 1 -> this.m11();
                case 2 -> this.m12();
                case 3 -> this.m13();
                default -> throw new IndexOutOfBoundsException(
                        String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                );
            };
            case 2 -> switch (j) {
                case 0 -> this.m20();
                case 1 -> this.m21();
                case 2 -> this.m22();
                case 3 -> this.m23();
                default -> throw new IndexOutOfBoundsException(
                        String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                );
            };
            case 3 -> switch (j) {
                case 0 -> this.m30();
                case 1 -> this.m31();
                case 2 -> this.m32();
                case 3 -> this.m33();
                default -> throw new IndexOutOfBoundsException(
                        String.format("j index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
                );
            };
            default -> throw new IndexOutOfBoundsException(
                    String.format("i index is out of bounds (i=%d, j=%d) - valid values are: 0, 1, 2, 3", i, j)
            );
        };
    }

    /**
     * Checks if this matrix is equivalent to the identity matrix.
     *
     * @return True if this matrix is equivalent to the identity matrix, false otherwise.
     */
    default boolean isIdentity() {
        return this.m00() == 1 && this.m01() == 0 && this.m02() == 0 && this.m03() == 0 &&
                this.m10() == 0 && this.m11() == 1 && this.m12() == 0 && this.m13() == 0 &&
                this.m20() == 0 && this.m21() == 0 && this.m22() == 1 && this.m23() == 0 &&
                this.m30() == 0 && this.m31() == 0 && this.m32() == 0 && this.m33() == 1;
    }

}
