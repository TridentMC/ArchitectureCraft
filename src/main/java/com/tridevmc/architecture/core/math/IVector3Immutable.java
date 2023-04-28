package com.tridevmc.architecture.core.math;

/**
 * An extension of {@link IVector3} that is expected to be immutable.
 * <p>
 * Construct using {@link IVector3Immutable#of(double, double, double)}.
 * <p>
 * See also {@link IVector3}, and {@link IVector3Mutable}.
 */
public interface IVector3Immutable extends IVector3 {

    /**
     * Creates a new immutable vector from the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3Immutable of(double x, double y, double z) {
        return new Vector3(x, y, z);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector3#asImmutable()} instead.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector3Immutable of(IVector3 vec) {
        return of(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by adding the given values to this vector.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to add.
     * @param y The Y coordinate to add.
     * @param z The Z coordinate to add.
     * @return The new vector.
     */
    default IVector3Immutable add(double x, double y, double z) {
        return of(this.x() + x, this.y() + y, this.z() + z);
    }

    /**
     * Creates a new immutable vector by adding the given values to this vector.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to add.
     * @return The new vector.
     */
    default IVector3Immutable add(IVector3 vec) {
        return this.add(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by subtracting the given values from this vector.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to subtract.
     * @param y The Y coordinate to subtract.
     * @param z The Z coordinate to subtract.
     * @return The new vector.
     */
    default IVector3Immutable sub(double x, double y, double z) {
        return of(this.x() - x, this.y() - y, this.z() - z);
    }

    /**
     * Creates a new immutable vector by subtracting the given values from this vector.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to subtract.
     * @return The new vector.
     */
    default IVector3Immutable sub(IVector3 vec) {
        return this.sub(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by multiplying this vector by the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to multiply by.
     * @param y The Y coordinate to multiply by.
     * @param z The Z coordinate to multiply by.
     * @return The new vector.
     */
    default IVector3Immutable mul(double x, double y, double z) {
        return of(this.x() * x, this.y() * y, this.z() * z);
    }

    /**
     * Creates a new immutable vector by multiplying this vector by the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to multiply by.
     * @return The new vector.
     */
    default IVector3Immutable mul(IVector3 vec) {
        return this.mul(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by multiplying this vector by the given value.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param value The value to multiply by.
     * @return The new vector.
     */
    default IVector3Immutable mul(double value) {
        return this.mul(value, value, value);
    }

    /**
     * Creates a new immutable vector by dividing this vector by the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to divide by.
     * @param y The Y coordinate to divide by.
     * @param z The Z coordinate to divide by.
     * @return The new vector.
     */
    default IVector3Immutable div(double x, double y, double z) {
        return of(this.x() / x, this.y() / y, this.z() / z);
    }

    /**
     * Creates a new immutable vector by dividing this vector by the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to divide by.
     * @return The new vector.
     */
    default IVector3Immutable div(IVector3 vec) {
        return this.div(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by dividing this vector by the given value.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param value The value to divide by.
     * @return The new vector.
     */
    default IVector3Immutable div(double value) {
        return this.div(value, value, value);
    }

    /**
     * Creates a new immutable vector by calculating the cross product of this vector and the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to cross with.
     * @param y The Y coordinate to cross with.
     * @param z The Z coordinate to cross with.
     * @return The new vector.
     */
    default IVector3Immutable cross(double x, double y, double z) {
        return of(this.y() * z - this.z() * y, this.z() * x - this.x() * z, this.x() * y - this.y() * x);
    }

    /**
     * Creates a new immutable vector by calculating the cross product of this vector and the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to cross with.
     * @return The new vector.
     */
    default IVector3Immutable cross(IVector3 vec) {
        return this.cross(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by selecting the minimum values between this vector and the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @param z The Z coordinate to compare.
     * @return The new vector.
     */
    default IVector3Immutable min(double x, double y, double z) {
        return of(Math.min(this.x(), x), Math.min(this.y(), y), Math.min(this.z(), z));
    }

    /**
     * Creates a new immutable vector by selecting the minimum values between this vector and the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to compare.
     * @return The new vector.
     */
    default IVector3Immutable min(IVector3 vec) {
        return this.min(vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a new immutable vector by selecting the maximum values between this vector and the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @param z The Z coordinate to compare.
     * @return The new vector.
     */
    default IVector3Immutable max(double x, double y, double z) {
        return of(Math.max(this.x(), x), Math.max(this.y(), y), Math.max(this.z(), z));
    }

    /**
     * Creates a new immutable vector by selecting the maximum values between this vector and the given values.
     * <p>
     * This method always returns a new instance, if you plan to make multiple modifications to a vector, it's recommended to use {@link IVector3#asMutable()} instead.
     *
     * @param vec The vector to compare.
     * @return The new vector.
     */
    default IVector3Immutable max(IVector3 vec) {
        return this.max(vec.x(), vec.y(), vec.z());
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
    default IVector3Immutable asImmutable() {
        // We are immutable, so just return ourselves. It's not like we can be any more immutable.
        return this;
    }

    @Override
    default IVector3Mutable asMutable() {
        return IVector3Mutable.of(this);
    }

}
