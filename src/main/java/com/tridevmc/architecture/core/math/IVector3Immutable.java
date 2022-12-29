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
