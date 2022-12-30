package com.tridevmc.architecture.core.math;

import net.minecraft.core.Direction;

public interface ITrans3 {

    /**
     * Gets the underlying matrix of this transform.
     *
     * @return The matrix.
     */
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
    ITrans3Immutable asImmutable();

    /**
     * Creates a mutable copy of this transform.
     *
     * @return A mutable copy of this transform.
     */
    ITrans3Mutable asMutable();


    /**
     * Gets the underlying matrix of this transform.
     *
     * @return The matrix.
     */
    default IMatrix4 getMatrix() {
        return this.matrix();
    }

    /**
     * Transforms a point in 3D space and stores the result in an immutable vector.
     *
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @param z The z coordinate of the point.
     * @return The transformed point.
     */
    default IVector3Immutable transformPosImmutable(double x, double y, double z) {
        return IVector3.ofImmutable(
                x * this.matrix().m00() + y * this.matrix().m10() + z * this.matrix().m20() + this.matrix().m30(),
                x * this.matrix().m01() + y * this.matrix().m11() + z * this.matrix().m21() + this.matrix().m31(),
                x * this.matrix().m02() + y * this.matrix().m12() + z * this.matrix().m22() + this.matrix().m32()
        );
    }

    /**
     * Transforms a point in 3D space and stores the result in an immutable vector.
     * <p>
     * If multiple transforms are being applied to the same point, it is more efficient to create a mutable vector and use {@link #transformPos(IVector3Mutable)}.
     *
     * @param vec The point.
     * @return The transformed point.
     */
    default IVector3Immutable transformPosImmutable(IVector3Immutable vec) {
        return this.transformPosImmutable(vec.x(), vec.y(), vec.z());
    }

    /**
     * Transforms the given vector representing a point in 3D space.
     *
     * @param vec The vector to transform.
     * @return The transformed point.
     */
    default IVector3Mutable transformPos(IVector3Mutable vec) {
        return vec.set(
                vec.x() * this.matrix().m00() + vec.y() * this.matrix().m10() + vec.z() * this.matrix().m20() + this.matrix().m30(),
                vec.x() * this.matrix().m01() + vec.y() * this.matrix().m11() + vec.z() * this.matrix().m21() + this.matrix().m31(),
                vec.x() * this.matrix().m02() + vec.y() * this.matrix().m12() + vec.z() * this.matrix().m22() + this.matrix().m32()
        );
    }

    /**
     * Transforms a set of X, Y, and Z coordinates representing the normals of a vertex or face and stores the result in an immutable vector.
     *
     * @param x The x coordinate of the normal.
     * @param y The y coordinate of the normal.
     * @param z The z coordinate of the normal.
     * @return The transformed normal.
     */
    default IVector3Immutable transformNormalImmutable(double x, double y, double z) {
        return IVector3.ofMutable(
                x * this.matrix().m00() + y * this.matrix().m10() + z * this.matrix().m20(),
                x * this.matrix().m01() + y * this.matrix().m11() + z * this.matrix().m21(),
                x * this.matrix().m02() + y * this.matrix().m12() + z * this.matrix().m22()
        ).normalize().asImmutable();
    }

    /**
     * Transforms a vector representing the normals of a vertex or face and stores the result in an immutable vector.
     * <p>
     * If multiple transforms are being applied to the same normal, it is more efficient to create a mutable vector and use {@link #transformNormal(IVector3Mutable)}.
     *
     * @param vec The normal.
     * @return The transformed normal.
     */
    default IVector3Immutable transformNormalImmutable(IVector3Immutable vec) {
        return this.transformNormalImmutable(vec.x(), vec.y(), vec.z());
    }

    /**
     * Transforms the given vector representing the normals of a vertex or face.
     *
     * @param vec The vector to transform.
     * @return The transformed normal.
     */
    default IVector3Mutable transformNormal(IVector3Mutable vec) {
        return vec.set(
                vec.x() * this.matrix().m00() + vec.y() * this.matrix().m10() + vec.z() * this.matrix().m20(),
                vec.x() * this.matrix().m01() + vec.y() * this.matrix().m11() + vec.z() * this.matrix().m21(),
                vec.x() * this.matrix().m02() + vec.y() * this.matrix().m12() + vec.z() * this.matrix().m22()
        ).normalize();
    }

    /**
     * Transforms a set of X, and Y coordinates representing texture coordinates and stores the result in an immutable vector.
     *
     * @param u The u coordinate of the texture.
     * @param v The v coordinate of the texture.
     * @return The transformed texture coordinates.
     */
    default IVector2Immutable transformUVImmutable(double u, double v) {
        return IVector2.ofImmutable(
                u * this.matrix().m00() + v * this.matrix().m10() + this.matrix().m30(),
                u * this.matrix().m01() + v * this.matrix().m11() + this.matrix().m31()
        );
    }

    /**
     * Transforms a vector representing texture coordinates and stores the result in an immutable vector.
     * <p>
     * If multiple transforms are being applied to the same texture coordinates, it is more efficient to create a mutable vector and use {@link #transformUV(IVector2Mutable)}.
     *
     * @param vec The texture coordinates.
     * @return The transformed texture coordinates.
     */
    default IVector2Immutable transformUVImmutable(IVector2Immutable vec) {
        return this.transformUVImmutable(vec.x(), vec.y());
    }

    /**
     * Transforms the given vector representing texture coordinates.
     *
     * @param vec The vector to transform.
     * @return The transformed texture coordinates.
     */
    default IVector2Mutable transformUV(IVector2Mutable vec) {
        return vec.set(
                vec.x() * this.matrix().m00() + vec.y() * this.matrix().m10() + this.matrix().m30(),
                vec.x() * this.matrix().m01() + vec.y() * this.matrix().m11() + this.matrix().m31()
        );
    }

    /**
     * Transforms the given direction.
     *
     * @param direction The direction to transform.
     * @return The transformed direction.
     */
    default Direction transformDirection(Direction direction) {
        // Let's avoid allocating a new vector - we'll just do the math as if we were transforming a vector.
        // We'll also avoid normalizing the vector, since we know the direction vectors are already normalized.
        var normal = direction.getNormal();
        var x = normal.getX() * this.matrix().m00() + normal.getY() * this.matrix().m10() + normal.getZ() * this.matrix().m20();
        var y = normal.getX() * this.matrix().m01() + normal.getY() * this.matrix().m11() + normal.getZ() * this.matrix().m21();
        var z = normal.getX() * this.matrix().m02() + normal.getY() * this.matrix().m12() + normal.getZ() * this.matrix().m22();
        return Direction.fromNormal(
                Math.round((float) x),
                Math.round((float) y),
                Math.round((float) z)
        );
    }


}
