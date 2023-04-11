package com.tridevmc.architecture.core.math.floating;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * An extension of {@link IVector3F} that allows for mutation of the vector.
 * <p>
 * Construct using {@link IVector3FMutable#ofImmutable(float, float, float)}.
 *
 * @see IVector3F
 */
public interface IVector3FMutable extends IVector3F {

    /**
     * Creates a new mutable vector with the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3FMutable of(float x, float y, float z) {
        return new Vector3F.Mutable(x, y, z);
    }

    /**
     * Creates a new mutable vector with the coordinates of the given vector.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector3FMutable of(IVector3F vec) {
        return of(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets the X component of this vector to the given value.
     *
     * @param x The new value of the X component.
     * @return This vector.
     */
    IVector3FMutable setX(float x);

    /**
     * Sets the Y component of this vector to the given value.
     *
     * @param y The new value of the Y component.
     * @return This vector.
     */
    IVector3FMutable setY(float y);

    /**
     * Sets the Z component of this vector to the given value.
     *
     * @param z The new value of the Z component.
     * @return This vector.
     */
    IVector3FMutable setZ(float z);

    /**
     * Sets the X, Y and Z components of this vector to the given values.
     *
     * @param x The new value of the X component.
     * @param y The new value of the Y component.
     * @param z The new value of the Z component.
     * @return This vector.
     */
    IVector3FMutable set(float x, float y, float z);

    /**
     * Sets the X, Y and Z components of this vector to values from the given vector.
     *
     * @param vec The vector to copy the values from.
     * @return This vector.
     */
    default IVector3FMutable set(IVector3F vec) {
        return this.set(vec.x(), vec.y(), vec.z());
    }

    /**
     * Adds the given values to the X, Y and Z components of this vector.
     *
     * @param x The value to add to the X component.
     * @param y The value to add to the Y component.
     * @param z The value to add to the Z component.
     * @return This vector.
     */
    default IVector3FMutable add(float x, float y, float z) {
        return this.set(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    /**
     * Adds the given values to the X, Y and Z components of this vector.
     *
     * @param vec The vector to add to this vector.
     * @return This vector.
     */
    default IVector3FMutable add(IVector3F vec) {
        return this.add(vec.x(), vec.y(), vec.z());
    }

    /**
     * Subtracts the given values from the X, Y and Z components of this vector.
     *
     * @param x The value to subtract from the X component.
     * @param y The value to subtract from the Y component.
     * @param z The value to subtract from the Z component.
     * @return This vector.
     */
    default IVector3FMutable sub(float x, float y, float z) {
        return this.set(this.getX() - x, this.getY() - y, this.getZ() - z);
    }

    /**
     * Subtracts the given values from the X, Y and Z components of this vector.
     *
     * @param vec The vector to subtract from this vector.
     * @return This vector.
     */
    default IVector3FMutable sub(IVector3F vec) {
        return this.sub(vec.x(), vec.y(), vec.z());
    }

    /**
     * Multiplies the X, Y and Z components of this vector by the given values.
     *
     * @param x The value to multiply the X component by.
     * @param y The value to multiply the Y component by.
     * @param z The value to multiply the Z component by.
     * @return This vector.
     */
    default IVector3FMutable mul(float x, float y, float z) {
        return this.set(this.getX() * x, this.getY() * y, this.getZ() * z);
    }

    /**
     * Multiplies the X, Y and Z components of this vector by the given values.
     *
     * @param vec The vector to multiply this vector by.
     * @return This vector.
     */
    default IVector3FMutable mul(IVector3F vec) {
        return this.mul(vec.x(), vec.y(), vec.z());
    }

    /**
     * Multiplies the X, Y and Z components of this vector by the given scalar value.
     *
     * @param scalar The scalar value to multiply this vector by.
     * @return This vector.
     */
    default IVector3FMutable mul(float scalar) {
        return this.mul(scalar, scalar, scalar);
    }

    /**
     * Divides the X, Y and Z components of this vector by the given values.
     *
     * @param x The value to divide the X component by.
     * @param y The value to divide the Y component by.
     * @param z The value to divide the Z component by.
     * @return This vector.
     */
    default IVector3FMutable div(float x, float y, float z) {
        return this.set(this.getX() / x, this.getY() / y, this.getZ() / z);
    }

    /**
     * Divides the X, Y and Z components of this vector by the given values.
     *
     * @param vec The vector to divide this vector by.
     * @return This vector.
     */
    default IVector3FMutable div(IVector3F vec) {
        return this.div(vec.x(), vec.y(), vec.z());
    }

    /**
     * Divides the X, Y and Z components of this vector by the given scalar value.
     *
     * @param scalar The scalar value to divide this vector by.
     * @return This vector.
     */
    default IVector3FMutable div(float scalar) {
        return this.div(scalar, scalar, scalar);
    }

    /**
     * Calculates the cross product of this vector and the given vector, storing the result in this vector.
     *
     * @param x The X component of the other vector.
     * @param y The Y component of the other vector.
     * @param z The Z component of the other vector.
     * @return This vector.
     */
    default IVector3FMutable cross(float x, float y, float z) {
        return this.set(this.getY() * z - this.getZ() * y, this.getZ() * x - this.getX() * z, this.getX() * y - this.getY() * x);
    }

    /**
     * Calculates the cross product of this vector and the given vector, storing the result in this vector.
     *
     * @param vec The other vector.
     * @return This vector.
     */
    default IVector3FMutable cross(IVector3F vec) {
        return this.cross(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets the values of this vector to the minimum of this vector and the given values.
     *
     * @param x The X component to compare.
     * @param y The Y component to compare.
     * @param z The Z component to compare.
     * @return This vector.
     */
    default IVector3FMutable min(float x, float y, float z) {
        return this.set(Math.min(this.getX(), x), Math.min(this.getY(), y), Math.min(this.getZ(), z));
    }

    /**
     * Sets the values of this vector to the minimum of this vector and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector3FMutable min(IVector3F vec) {
        return this.min(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets the values of this vector to the maximum of this vector and the given values.
     *
     * @param x The X component to compare.
     * @param y The Y component to compare.
     * @param z The Z component to compare.
     * @return This vector.
     */
    default IVector3FMutable max(float x, float y, float z) {
        return this.set(Math.max(this.getX(), x), Math.max(this.getY(), y), Math.max(this.getZ(), z));
    }

    /**
     * Sets the values of this vector to the maximum of this vector and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector3FMutable max(IVector3F vec) {
        return this.max(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets the values of this vector to the absolute values of this vector.
     *
     * @return This vector.
     */
    default IVector3FMutable abs() {
        return this.set(Math.abs(this.getX()), Math.abs(this.getY()), Math.abs(this.getZ()));
    }

    /**
     * Sets the values of this vector to the floor of this vector.
     *
     * @return This vector.
     */
    default IVector3FMutable floor() {
        return this.set((float) Math.floor(this.getX()), (float) Math.floor(this.getY()), (float) Math.floor(this.getZ()));
    }

    /**
     * Sets the values of this vector to the ceiling of this vector.
     *
     * @return This vector.
     */
    default IVector3FMutable ceil() {
        return this.set((float) Math.ceil(this.getX()), (float) Math.ceil(this.getY()), (float) Math.ceil(this.getZ()));
    }

    /**
     * Sets the values of this vector to the rounded values of this vector.
     *
     * @return This vector.
     */
    default IVector3FMutable round() {
        return this.set(Math.round(this.getX()), Math.round(this.getY()), Math.round(this.getZ()));
    }

    /**
     * Normalizes this vector.
     *
     * @return This vector.
     */
    default IVector3FMutable normalize() {
        float length = this.length();
        if (length == 0) {
            return this;
        }
        return this.div(length);
    }

    /**
     * Sets the values of this vector to the average of this vector and the given values.
     *
     * @param x The X component to average.
     * @param y The Y component to average.
     * @param z The Z component to average.
     * @return This vector.
     */
    default IVector3FMutable avg(float x, float y, float z) {
        return this.set((this.getX() + x) / 2, (this.getY() + y) / 2, (this.getZ() + z) / 2);
    }

    /**
     * Sets the values of this vector to the average of this vector and the given vector.
     *
     * @param vec The vector to average.
     * @return This vector.
     */
    default IVector3FMutable avg(IVector3F vec) {
        return this.avg(vec.x(), vec.y(), vec.z());
    }

    /**
     * Sets the values of this vector to the average of this vector and the given array of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector3FMutable avg(IVector3F... vecs) {
        float x = this.getX();
        float y = this.getY();
        float z = this.getZ();
        for (IVector3F vec : vecs) {
            x += vec.x();
            y += vec.y();
            z += vec.z();
        }
        return this.set(x / (vecs.length + 1), y / (vecs.length + 1), z / (vecs.length + 1));
    }

    /**
     * Sets the values of this vector to the average of this vector and the given collection of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector3FMutable avg(Collection<? extends IVector3F> vecs) {
        var x = this.getX();
        var y = this.getY();
        var z = this.getZ();
        for (IVector3F vec : vecs) {
            x += vec.x();
            y += vec.y();
            z += vec.z();
        }
        return this.set(x / (vecs.size() + 1), y / (vecs.size() + 1), z / (vecs.size() + 1));
    }

    /**
     * Sets the values of this vector to the average of this vector and the given stream of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector3FMutable avg(Stream<? extends IVector3F> vecs) {
        return this.avg(vecs.toList());
    }

    /**
     * Sets the value of the specified component of this vector to the given value.
     *
     * @param component The component to set, 0 for X, 1 for Y, 2 for Z.
     * @param value     The value to set the component to.
     * @return This vector.
     * @throws IndexOutOfBoundsException If the component is not 0, 1, or 2.
     */
    default IVector3FMutable setComponent(int component, float value) {
        return switch (component) {
            case 0 -> this.setX(value);
            case 1 -> this.setY(value);
            case 2 -> this.setZ(value);
            default -> throw new IllegalArgumentException("Invalid component: " + component);
        };
    }

    @Override
    default boolean isMutable() {
        return true;
    }

    @Override
    default boolean isImmutable() {
        return false;
    }

    @Override
    default IVector3FImmutable asImmutable() {
        return IVector3FImmutable.of(this);
    }

    @Override
    default IVector3FMutable asMutable() {
        return IVector3FMutable.of(this);
    }

}
