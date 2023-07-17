package com.tridevmc.architecture.core.math;

import com.tridevmc.architecture.core.math.floating.*;
import com.tridevmc.architecture.core.model.mesh.CullFace;
import com.tridevmc.architecture.core.model.mesh.FaceDirection;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around a transformation matrix that provides methods for transforming points and vectors.
 * <p>
 * Can be instantiated using the {@link ITrans3#ofImmutable(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)} and {@link ITrans3#ofMutable(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)} methods.
 * <p>
 * For mutable transformations, see {@link ITrans3Mutable}, for immutable transformations, see {@link ITrans3Immutable}.
 */
public interface ITrans3 {

    ITrans3Immutable BLOCK_CENTER = ITrans3.ofTranslationImmutable(0.5, 0.5, 0.5);
    ITrans3Immutable BLOCK_CENTER_INVERSE = ITrans3.ofTranslationImmutable(-0.5, -0.5, -0.5);
    ITrans3Immutable IDENTITY = ITrans3.ofImmutable(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    );

    /**
     * Creates a new immutable transformation from the given matrix values.
     * <p>
     * See {@link ITrans3Immutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)} for more information.
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
     * @return The new transformation.
     */
    @NotNull
    static ITrans3Immutable ofImmutable(double m00, double m01, double m02, double m03,
                                        double m10, double m11, double m12, double m13,
                                        double m20, double m21, double m22, double m23,
                                        double m30, double m31, double m32, double m33) {
        return ITrans3Immutable.of(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    /**
     * Creates a new immutable transformation from the values in the given matrix.
     * <p>
     * See {@link ITrans3Immutable#of(IMatrix4)} for more information.
     *
     * @param matrix The matrix to copy values from.
     * @return The new transformation.
     */
    @NotNull
    static ITrans3Immutable ofImmutable(@NotNull IMatrix4 matrix) {
        return ITrans3Immutable.of(matrix);
    }

    /**
     * Creates a new immutable transform that offsets any point in space by the given values.
     *
     * @param transX The x offset to apply.
     * @param transY The y offset to apply.
     * @param transZ The z offset to apply.
     * @return the new transform.
     */
    @NotNull
    static ITrans3Immutable ofTranslationImmutable(double transX, double transY, double transZ) {
        return ITrans3Immutable.ofTranslation(transX, transY, transZ);
    }

    /**
     * Creates a new immutable transform that offsets any point in space by the given values.
     *
     * @param trans The offset to apply.
     * @return the new transform.
     */
    @NotNull
    static ITrans3Immutable ofTranslationImmutable(@NotNull IVector3 trans) {
        return ITrans3Immutable.ofTranslation(trans);
    }

    /**
     * Creates a new immutable transform that scales any point in space by the given values.
     *
     * @param scaleX The x scale to apply.
     * @param scaleY The y scale to apply.
     * @param scaleZ The z scale to apply.
     * @return the new transform.
     */
    @NotNull
    static ITrans3Immutable ofScaleImmutable(double scaleX, double scaleY, double scaleZ) {
        return ITrans3Immutable.ofScale(scaleX, scaleY, scaleZ);
    }

    /**
     * Creates a new immutable transform that scales any point in space by the given values.
     *
     * @param scale The scale to apply.
     * @return the new transform.
     */
    @NotNull
    static ITrans3Immutable ofScaleImmutable(@NotNull IVector3 scale) {
        return ITrans3Immutable.ofScale(scale);
    }


    /**
     * Creates a new immutable transformation from the given transformation.
     * <p>
     * Please note that this method will always return a new instance, even if the given transformation is already immutable.
     * This has performance implications, it's recommended to use {@link ITrans3Immutable#of(ITrans3)} instead.
     * <p>
     * See {@link ITrans3Immutable#of(ITrans3)} for more information.
     *
     * @param trans The transformation to copy values from.
     * @return The new transformation.
     */
    @NotNull
    static ITrans3Immutable ofImmutable(@NotNull ITrans3 trans) {
        return ITrans3Immutable.of(trans);
    }

    /**
     * Creates a new mutable transformation from the given matrix values.
     * <p>
     * See {@link ITrans3Mutable#of(double, double, double, double, double, double, double, double, double, double, double, double, double, double, double, double)} for more information.
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
     * @return The new transformation.
     */
    @NotNull
    static ITrans3Mutable ofMutable(double m00, double m01, double m02, double m03,
                                    double m10, double m11, double m12, double m13,
                                    double m20, double m21, double m22, double m23,
                                    double m30, double m31, double m32, double m33) {
        return ITrans3Mutable.of(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    /**
     * Creates a new mutable transformation from the values in the given matrix.
     * <p>
     * See {@link ITrans3Mutable#of(IMatrix4)} for more information.
     *
     * @param matrix The matrix to copy values from.
     * @return The new transformation.
     */
    @NotNull
    static ITrans3Mutable ofMutable(@NotNull IMatrix4 matrix) {
        return ITrans3Mutable.of(matrix);
    }

    /**
     * Creates a new mutable transformation from the given transformation.
     * <p>
     * See {@link ITrans3Mutable#of(ITrans3)} for more information.
     *
     * @param trans The transformation to copy values from.
     * @return The new transformation.
     */
    @NotNull
    static ITrans3Mutable ofMutable(@NotNull ITrans3 trans) {
        return ITrans3Mutable.of(trans);
    }

    /**
     * Gets the immutable identity transform (a transform that does nothing).
     *
     * @return The identity transform.
     */
    @NotNull
    static ITrans3Immutable ofIdentity() {
        return IDENTITY;
    }

    /**
     * Gets the underlying matrix of this transform.
     *
     * @return The matrix.
     */
    @NotNull
    IMatrix4 matrix();

    /**
     * Determines if this transform is immutable or not.
     *
     * @return True if this transform is immutable, false otherwise.
     */
    boolean isImmutable();

    /**
     * Determines if this transform is mutable or not.
     *
     * @return True if this transform is mutable, false otherwise.
     */
    boolean isMutable();

    /**
     * Creates an immutable copy of this transform.
     * <p>
     * If this transform is already immutable, this method will return itself.
     *
     * @return An immutable copy of this transform.
     */
    @NotNull
    ITrans3Immutable asImmutable();

    /**
     * Creates a mutable copy of this transform.
     *
     * @return A mutable copy of this transform.
     */
    @NotNull
    ITrans3Mutable asMutable();

    /**
     * Determines if this transform is the identity transform.
     *
     * @return True if this transform is the identity transform, false otherwise.
     */
    default boolean isIdentity() {
        return this.matrix().isIdentity();
    }

    /**
     * Gets the underlying matrix of this transform.
     *
     * @return The matrix.
     */
    @NotNull
    default IMatrix4 getMatrix() {
        return this.matrix();
    }

    /**
     * Transforms the given position vector by this transformation, storing the result in the given vector.
     *
     * @param position The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3Mutable transformPos(@NotNull IVector3Mutable position) {
        return position.set(
                this.matrix().m00() * position.x() + this.matrix().m01() * position.y() + this.matrix().m02() * position.z() + this.matrix().m03(),
                this.matrix().m10() * position.x() + this.matrix().m11() * position.y() + this.matrix().m12() * position.z() + this.matrix().m13(),
                this.matrix().m20() * position.x() + this.matrix().m21() * position.y() + this.matrix().m22() * position.z() + this.matrix().m23()
        );
    }

    /**
     * Transforms the given position vector by this transformation, storing the result in a new vector.
     *
     * @param position The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3Immutable transformPosImmutable(@NotNull IVector3 position) {
        return IVector3Immutable.of(
                this.matrix().m00() * position.x() + this.matrix().m01() * position.y() + this.matrix().m02() * position.z() + this.matrix().m03(),
                this.matrix().m10() * position.x() + this.matrix().m11() * position.y() + this.matrix().m12() * position.z() + this.matrix().m13(),
                this.matrix().m20() * position.x() + this.matrix().m21() * position.y() + this.matrix().m22() * position.z() + this.matrix().m23()
        );
    }

    /**
     * Transforms the given position vector by this transformation, storing the result in the given vector.
     *
     * @param position The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3FMutable transformPos(@NotNull IVector3FMutable position) {
        return position.set(
                (float) (this.matrix().m00() * position.x() + this.matrix().m01() * position.y() + this.matrix().m02() * position.z() + this.matrix().m03()),
                (float) (this.matrix().m10() * position.x() + this.matrix().m11() * position.y() + this.matrix().m12() * position.z() + this.matrix().m13()),
                (float) (this.matrix().m20() * position.x() + this.matrix().m21() * position.y() + this.matrix().m22() * position.z() + this.matrix().m23())
        );
    }

    /**
     * Transforms the given position vector by this transformation, storing the result in a new vector.
     *
     * @param position The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3FImmutable transformPosImmutable(@NotNull IVector3F position) {
        return IVector3FImmutable.of(
                (float) (this.matrix().m00() * position.x() + this.matrix().m01() * position.y() + this.matrix().m02() * position.z() + this.matrix().m03()),
                (float) (this.matrix().m10() * position.x() + this.matrix().m11() * position.y() + this.matrix().m12() * position.z() + this.matrix().m13()),
                (float) (this.matrix().m20() * position.x() + this.matrix().m21() * position.y() + this.matrix().m22() * position.z() + this.matrix().m23())
        );
    }

    /**
     * Transforms the given normal vector by this transformation, storing the result in the given vector.
     *
     * @param normal The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3Mutable transformNormal(@NotNull IVector3Mutable normal) {
        return normal.set(
                this.matrix().m00() * normal.x() + this.matrix().m01() * normal.y() + this.matrix().m02() * normal.z(),
                this.matrix().m10() * normal.x() + this.matrix().m11() * normal.y() + this.matrix().m12() * normal.z(),
                this.matrix().m20() * normal.x() + this.matrix().m21() * normal.y() + this.matrix().m22() * normal.z()
        ).normalize();
    }

    /**
     * Transforms the given normal vector by this transformation, storing the result in a new vector.
     *
     * @param normal The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3Immutable transformNormalImmutable(@NotNull IVector3 normal) {
        var x = this.matrix().m00() * normal.x() + this.matrix().m01() * normal.y() + this.matrix().m02() * normal.z();
        var y = this.matrix().m10() * normal.x() + this.matrix().m11() * normal.y() + this.matrix().m12() * normal.z();
        var z = this.matrix().m20() * normal.x() + this.matrix().m21() * normal.y() + this.matrix().m22() * normal.z();
        var length = Math.sqrt(x * x + y * y + z * z);
        return IVector3Immutable.of(x / length, y / length, z / length);
    }

    /**
     * Transforms the given normal vector by this transformation, storing the result in the given vector.
     *
     * @param normal The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3FMutable transformNormal(@NotNull IVector3FMutable normal) {
        return normal.set(
                (float) (this.matrix().m00() * normal.x() + this.matrix().m01() * normal.y() + this.matrix().m02() * normal.z()),
                (float) (this.matrix().m10() * normal.x() + this.matrix().m11() * normal.y() + this.matrix().m12() * normal.z()),
                (float) (this.matrix().m20() * normal.x() + this.matrix().m21() * normal.y() + this.matrix().m22() * normal.z())
        ).normalize();
    }

    /**
     * Transforms the given normal vector by this transformation, storing the result in a new vector.
     *
     * @param normal The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector3FImmutable transformNormalImmutable(@NotNull IVector3F normal) {
        var x = this.matrix().m00() * normal.x() + this.matrix().m01() * normal.y() + this.matrix().m02() * normal.z();
        var y = this.matrix().m10() * normal.x() + this.matrix().m11() * normal.y() + this.matrix().m12() * normal.z();
        var z = this.matrix().m20() * normal.x() + this.matrix().m21() * normal.y() + this.matrix().m22() * normal.z();
        var length = Math.sqrt(x * x + y * y + z * z);
        return IVector3FImmutable.of((float) (x / length), (float) (y / length), (float) (z / length));
    }

    /**
     * Transforms the given texture coordinate vector by this transformation, storing the result in the given vector.
     *
     * @param uvs The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector2Mutable transformUV(@NotNull IVector2Mutable uvs) {
        return uvs.set(
                this.matrix().m00() * uvs.x() + this.matrix().m01() * uvs.y() + this.matrix().m03(),
                this.matrix().m10() * uvs.x() + this.matrix().m11() * uvs.y() + this.matrix().m13()
        );
    }

    /**
     * Transforms the given texture coordinate vector by this transformation, storing the result in a new vector.
     *
     * @param uvs The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector2Immutable transformUVImmutable(@NotNull IVector2 uvs) {
        return IVector2Immutable.of(
                this.matrix().m00() * uvs.x() + this.matrix().m01() * uvs.y() + this.matrix().m03(),
                this.matrix().m10() * uvs.x() + this.matrix().m11() * uvs.y() + this.matrix().m13()
        );
    }

    /**
     * Transforms the given texture coordinate vector by this transformation, storing the result in the given vector.
     *
     * @param uvs The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector2FMutable transformUV(@NotNull IVector2FMutable uvs) {
        return uvs.set(
                this.matrix().m00() * uvs.x() + this.matrix().m01() * uvs.y() + this.matrix().m03(),
                this.matrix().m10() * uvs.x() + this.matrix().m11() * uvs.y() + this.matrix().m13()
        );
    }

    /**
     * Transforms the given texture coordinate vector by this transformation, storing the result in a new vector.
     *
     * @param uvs The vector to transform.
     * @return The given vector.
     */
    @NotNull
    default IVector2FImmutable transformUVImmutable(@NotNull IVector2F uvs) {
        return IVector2FImmutable.of(
                this.matrix().m00() * uvs.x() + this.matrix().m01() * uvs.y() + this.matrix().m03(),
                this.matrix().m10() * uvs.x() + this.matrix().m11() * uvs.y() + this.matrix().m13()
        );
    }

    /**
     * Transforms the minimum and maximum points of the given AABB by this transformation, storing the result in a new AABB.
     *
     * @param aabb The AABB to transform.
     * @return The transformed AABB.
     */
    @NotNull
    default AABB transformAABB(@NotNull AABB aabb) {
        var tMin = this.transformPosImmutable(aabb.min());
        var tMax = this.transformPosImmutable(aabb.max());
        // Select the minimum and maximum coordinates from the transformed points.
        var min = IVector3Immutable.of(
                Math.min(tMin.x(), tMax.x()),
                Math.min(tMin.y(), tMax.y()),
                Math.min(tMin.z(), tMax.z())
        );
        var max = IVector3Immutable.of(
                Math.max(tMin.x(), tMax.x()),
                Math.max(tMin.y(), tMax.y()),
                Math.max(tMin.z(), tMax.z())
        );
        return new AABB(
                min, max
        );
    }

    /**
     * Transforms the given direction by this transformation, returning the new direction.
     *
     * @param direction The direction to transform.
     * @return The transformed direction.
     */
    @NotNull
    default Direction transformDirection(@NotNull Direction direction) {
        var x = direction.getStepX();
        var y = direction.getStepY();
        var z = direction.getStepZ();
        var x2 = this.matrix().m00() * x + this.matrix().m01() * y + this.matrix().m02() * z;
        var y2 = this.matrix().m10() * x + this.matrix().m11() * y + this.matrix().m12() * z;
        var z2 = this.matrix().m20() * x + this.matrix().m21() * y + this.matrix().m22() * z;
        return Direction.getNearest(x2, y2, z2);
    }

    /**
     * Transforms the given cull face by this transformation, returning the new cull face.
     *
     * @param face The cull face to transform.
     * @return The transformed cull face.
     */
    @NotNull
    default CullFace transformCullFace(@NotNull CullFace face) {
        // Cull faces are effectively the same as directions, just with support for NONE.
        if (face == CullFace.NONE) {
            return CullFace.NONE;
        }
        //noinspection DataFlowIssue - the only way this can be null is if face is NONE, which we already checked.
        return CullFace.fromDirection(this.transformDirection(face.toDirection()));
    }

    /**
     * Transforms the given face direction by this transformation, returning the new face direction.
     *
     * @param face The face direction to transform.
     * @return The transformed face direction.
     */
    @NotNull
    default FaceDirection transformFaceDirection(@NotNull FaceDirection face) {
        return FaceDirection.fromDirection(this.transformDirection(face.toDirection()));
    }

}
