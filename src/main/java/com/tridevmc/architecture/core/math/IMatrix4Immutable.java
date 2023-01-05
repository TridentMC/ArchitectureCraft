package com.tridevmc.architecture.core.math;

import org.jetbrains.annotations.NotNull;

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
    @NotNull
    static IMatrix4Immutable of(@NotNull IMatrix4 matrix) {
        return of(
                matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03(),
                matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13(),
                matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23(),
                matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()
        );
    }

    /**
     * Creates a new immutable matrix that represents the given translation.
     *
     * @param transX The translation along the x-axis.
     * @param transY The translation along the y-axis.
     * @param transZ The translation along the z-axis.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofTranslation(double transX, double transY, double transZ) {
        return of(
                1, 0, 0, transX,
                0, 1, 0, transY,
                0, 0, 1, transZ,
                0, 0, 0, 1
        );
    }

    /**
     * Creates a new immutable matrix that represents the given translation.
     *
     * @param trans The translation.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofTranslation(@NotNull IVector3 trans) {
        return ofTranslation(trans.x(), trans.y(), trans.z());
    }

    /**
     * Creates a new immutable matrix that represents the given scale around the given point.
     *
     * @param aroundX The point to scale around on the x-axis.
     * @param aroundY The point to scale around on the y-axis.
     * @param aroundZ The point to scale around on the z-axis.
     * @param scaleX  The scale along the x-axis.
     * @param scaleY  The scale along the y-axis.
     * @param scaleZ  The scale along the z-axis.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofScale(
            double aroundX, double aroundY, double aroundZ,
            double scaleX, double scaleY, double scaleZ) {
        // Should be equivalent to creating an identity matrix, then translating to the point, scaling, then translating back.
        // So you would start with:
        // 1 0 0 0
        // 0 1 0 0
        // 0 0 1 0
        // 0 0 0 1

        // Then translate to the point:
        // 1 0 0 +aroundX
        // 0 1 0 +aroundY
        // 0 0 1 +aroundZ
        // 0 0 0 1

        // Then scale:
        // scaleX 0      0      0
        // 0      scaleY 0      0
        // 0      0      scaleZ 0
        // 0      0      0      1

        // Then translate back:
        // 1 0 0 -aroundX
        // 0 1 0 -aroundY
        // 0 0 1 -aroundZ
        // 0 0 0 1

        // Which can be simplified to:
        // scaleX 0      0      -aroundX*scaleX + aroundX
        // 0      scaleY 0      -aroundY*scaleY + aroundY
        // 0      0      scaleZ -aroundZ*scaleZ + aroundZ
        // 0      0      0      1

        // Which can be further simplified to:
        // scaleX 0      0      aroundX - aroundX*scaleX
        // 0      scaleY 0      aroundY - aroundY*scaleY
        // 0      0      scaleZ aroundZ - aroundZ*scaleZ
        // 0      0      0      1

        // Which can be further simplified to:
        // scaleX 0      0      aroundX*(1 - scaleX)
        // 0      scaleY 0      aroundY*(1 - scaleY)
        // 0      0      scaleZ aroundZ*(1 - scaleZ)
        // 0      0      0      1

        // Which can be further simplified to:

        return of(
                scaleX, 0, 0, aroundX * (1 - scaleX),
                0, scaleY, 0, aroundY * (1 - scaleY),
                0, 0, scaleZ, aroundZ * (1 - scaleZ),
                0, 0, 0, 1
        );
    }

    /**
     * Creates a new immutable matrix that represents the given scale around the given point.
     *
     * @param origin The point to scale around.
     * @param scale  The scale.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofScale(@NotNull IVector3 origin, @NotNull IVector3 scale) {
        return ofScale(
                origin.x(), origin.y(), origin.z(),
                scale.x(), scale.y(), scale.z()
        );
    }

    /**
     * Creates a new immutable matrix that represents the given scale.
     *
     * @param scaleX The scale along the x-axis.
     * @param scaleY The scale along the y-axis.
     * @param scaleZ The scale along the z-axis.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofScale(double scaleX, double scaleY, double scaleZ) {
        return ofScale(0, 0, 0, scaleX, scaleY, scaleZ);
    }

    /**
     * Creates a new immutable matrix that represents the given scale.
     *
     * @param scale The scale.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofScale(@NotNull IVector3 scale) {
        return ofScale(scale.x(), scale.y(), scale.z());
    }

    /**
     * Creates a new immutable matrix that represents the given rotation around the given point, using the XYZ convention.
     *
     * @param originX The point to rotate around on the x-axis.
     * @param originY The point to rotate around on the y-axis.
     * @param originZ The point to rotate around on the z-axis.
     * @param rotX    The rotation around the x-axis, in degrees.
     * @param rotY    The rotation around the y-axis, in degrees.
     * @param rotZ    The rotation around the z-axis, in degrees.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofRotationXYZ(
            double originX, double originY, double originZ,
            double rotX, double rotY, double rotZ
    ) {
        // Calculate the sin and cos of each rotation.
        final double xRadians = Math.toRadians(rotX);
        final double xSin = Math.sin(xRadians);
        final double xCos = Math.cos(xRadians);

        final double yRadians = Math.toRadians(rotY);
        final double ySin = Math.sin(yRadians);
        final double yCos = Math.cos(yRadians);

        final double zRadians = Math.toRadians(rotZ);
        final double zSin = Math.sin(zRadians);
        final double zCos = Math.cos(zRadians);

        var tMulYR10 = -xSin * -ySin;
        var tMulYR12 = -xSin * yCos;
        var tMulYR20 = xCos * -ySin;
        var tMulYR22 = xCos * yCos;

        var tMulZR00 = yCos * zCos;
        var tMulZR01 = yCos * -zSin;
        var tMulZR10 = tMulYR10 * zCos + xCos * zSin;
        var tMulZR11 = tMulYR10 * -zSin + xCos * zCos;
        var tMulZR20 = tMulYR20 * zCos + xSin * zSin;
        var tMulZR21 = tMulYR20 * -zSin + xSin * zCos;

        var out03 = tMulZR00 * -originX + tMulZR01 * -originY + ySin * -originZ + originX;
        var out13 = tMulZR10 * -originX + tMulZR11 * -originY + tMulYR12 * -originZ + originY;
        var out23 = tMulZR20 * -originX + tMulZR21 * -originY + tMulYR22 * -originZ + originZ;

        // Construct the matrix.
        return IMatrix4Immutable.of(
                tMulZR00, tMulZR01, ySin, out03,
                tMulZR10, tMulZR11, tMulYR12, out13,
                tMulZR20, tMulZR21, tMulYR22, out23,
                0, 0, 0, 1
        );
    }


    /**
     * Creates a new immutable matrix that represents the given rotation around the given point, using the XYZ convention.
     *
     * @param origin The point to rotate around.
     * @param rot    The rotation.
     * @return The new matrix.
     */
    @NotNull
    static IMatrix4Immutable ofRotationXYZ(
            IVector3 origin,
            IVector3 rot) {
        return ofRotationXYZ(
                origin.x(), origin.y(), origin.z(),
                rot.x(), rot.y(), rot.z()
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
    @NotNull
    default IMatrix4Immutable asImmutable() {
        // We are immutable, so just return ourselves. It's not like we can be any more immutable.
        return this;
    }

    @Override
    @NotNull
    default IMatrix4Mutable asMutable() {
        return IMatrix4Mutable.of(this);
    }
}
