package com.tridevmc.architecture.core.math.integer;

/**
 * An extension of {@link IVector3i} that allows for mutation.
 * <p>
 * Construct using {@link IVector3iMutable#of(int, int, int)}.
 * <p>
 * See also: {@link IVector3iImmutable}
 */
public interface IVector3iMutable extends IVector3i {

    /**
     * Creates a new mutable vector from the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3iMutable of(int x, int y, int z) {
        return new Vector3i.Mutable(x, y, z);
    }

    /**
     * Creates a new mutable vector from the given vector.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector3iMutable of(IVector3i vec) {
        return of(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets the X coordinate of this vector.
     *
     * @param x The new X coordinate.
     * @return This vector.
     */
    IVector3iMutable setX(int x);

    /**
     * Sets the Y coordinate of this vector.
     *
     * @param y The new Y coordinate.
     * @return This vector.
     */
    IVector3iMutable setY(int y);

    /**
     * Sets the Z coordinate of this vector.
     *
     * @param z The new Z coordinate.
     * @return This vector.
     */
    IVector3iMutable setZ(int z);

    /**
     * Sets the coordinates of this vector.
     *
     * @param x The new X coordinate.
     * @param y The new Y coordinate.
     * @param z The new Z coordinate.
     * @return This vector.
     */
    IVector3iMutable set(int x, int y, int z);

    /**
     * Sets the coordinates of this vector.
     *
     * @param vec The vector to copy.
     * @return This vector.
     */
    default IVector3iMutable set(IVector3i vec) {
        return this.set(vec.x(), vec.y(), vec.z());
    }

    /**
     * Adds the given coordinates to this vector.
     *
     * @param x The X coordinate to add.
     * @param y The Y coordinate to add.
     * @param z The Z coordinate to add.
     * @return This vector.
     */
    default IVector3iMutable add(int x, int y, int z) {
        return this.set(this.x() + x, this.y() + y, this.z() + z);
    }

    /**
     * Adds the given vector to this vector.
     *
     * @param vec The vector to add.
     * @return This vector.
     */
    default IVector3iMutable add(IVector3i vec) {
        return this.add(vec.x(), vec.y(), vec.z());
    }

    /**
     * Subtracts the given coordinates from this vector.
     *
     * @param x The X coordinate to subtract.
     * @param y The Y coordinate to subtract.
     * @param z The Z coordinate to subtract.
     * @return This vector.
     */
    default IVector3iMutable sub(int x, int y, int z) {
        return this.set(this.x() - x, this.y() - y, this.z() - z);
    }

    /**
     * Subtracts the given vector from this vector.
     *
     * @param vec The vector to subtract.
     * @return This vector.
     */
    default IVector3iMutable sub(IVector3i vec) {
        return this.sub(vec.x(), vec.y(), vec.z());
    }

    /**
     * Multiplies this vector by the given coordinates.
     *
     * @param x The X coordinate to multiply by.
     * @param y The Y coordinate to multiply by.
     * @param z The Z coordinate to multiply by.
     * @return This vector.
     */
    default IVector3iMutable mul(int x, int y, int z) {
        return this.set(this.x() * x, this.y() * y, this.z() * z);
    }

    /**
     * Multiplies this vector by the given vector.
     *
     * @param vec The vector to multiply by.
     * @return This vector.
     */
    default IVector3iMutable mul(IVector3i vec) {
        return this.mul(vec.x(), vec.y(), vec.z());
    }

    /**
     * Multiplies this vector by the given scalar value.
     *
     * @param scalar The scalar value to multiply by.
     * @return This vector.
     */
    default IVector3iMutable mul(int scalar) {
        return this.mul(scalar, scalar, scalar);
    }

    /**
     * Divides this vector by the given coordinates.
     *
     * @param x The X coordinate to divide by.
     * @param y The Y coordinate to divide by.
     * @param z The Z coordinate to divide by.
     * @return This vector.
     */
    default IVector3iMutable div(int x, int y, int z) {
        return this.set(this.x() / x, this.y() / y, this.z() / z);
    }

    /**
     * Divides this vector by the given vector.
     *
     * @param vec The vector to divide by.
     * @return This vector.
     */
    default IVector3iMutable div(IVector3i vec) {
        return this.div(vec.x(), vec.y(), vec.z());
    }

    /**
     * Divides this vector by the given scalar value.
     *
     * @param scalar The scalar value to divide by.
     * @return This vector.
     */
    default IVector3iMutable div(int scalar) {
        return this.div(scalar, scalar, scalar);
    }

    /**
     * Calculates the cross product of this vector and the given vector, storing the result in this vector.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @param z The Z coordinate of the other vector.
     * @return This vector.
     */
    default IVector3iMutable cross(int x, int y, int z) {
        return this.set(this.y() * z - this.z() * y, this.z() * x - this.x() * z, this.x() * y - this.y() * x);
    }

    /**
     * Calculates the cross product of this vector and the given vector, storing the result in this vector.
     *
     * @param vec The other vector.
     * @return This vector.
     */
    default IVector3iMutable cross(IVector3i vec) {
        return this.cross(vec.x(), vec.y(), vec.z());
    }

    /**
     * Negates this vector.
     *
     * @return This vector.
     */
    default IVector3iMutable negate() {
        return this.set(-this.x(), -this.y(), -this.z());
    }

    /**
     * Sets this vector to the minimum of itself and the given coordinates.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @param z The Z coordinate to compare.
     * @return This vector.
     */
    default IVector3iMutable min(int x, int y, int z) {
        return this.set(Math.min(this.x(), x), Math.min(this.y(), y), Math.min(this.z(), z));
    }

    /**
     * Sets this vector to the minimum of itself and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector3iMutable min(IVector3i vec) {
        return this.min(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets this vector to the maximum of itself and the given coordinates.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @param z The Z coordinate to compare.
     * @return This vector.
     */
    default IVector3iMutable max(int x, int y, int z) {
        return this.set(Math.max(this.x(), x), Math.max(this.y(), y), Math.max(this.z(), z));
    }

    /**
     * Sets this vector to the maximum of itself and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector3iMutable max(IVector3i vec) {
        return this.max(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets this vector to the absolute value of itself.
     *
     * @return This vector.
     */
    default IVector3iMutable abs() {
        return this.set(Math.abs(this.x()), Math.abs(this.y()), Math.abs(this.z()));
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
    default IVector3iImmutable asImmutable() {
        return IVector3iImmutable.of(this);
    }

    @Override
    default IVector3iMutable asMutable() {
        return IVector3iMutable.of(this);
    }

}
