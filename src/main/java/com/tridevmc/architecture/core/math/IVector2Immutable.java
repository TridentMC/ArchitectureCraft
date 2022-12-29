package com.tridevmc.architecture.core.math;

/**
 * An extension of {@link IVector2} that is expected to be immutable.
 * <p>
 * Construct using {@link IVector2Immutable#of(double, double)}.
 * <p>
 * See also {@link IVector2}, and {@link IVector2Mutable}.
 */
public interface IVector2Immutable extends IVector2 {

    /**
     * Creates a new immutable vector from the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2Immutable of(double x, double y) {
        return new Vector2(x, y);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector2#asImmutable()} instead.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector2Immutable of(IVector2 vec) {
        return of(vec.x(), vec.y());
    }

    @Override
    default boolean isImmutable() {
        // We are immutable, so just return ourselves. It's not like we can be any more immutable.
        return true;
    }

    @Override
    default boolean isMutable() {
        return false;
    }

    @Override
    default IVector2 asImmutable() {
        return this;
    }

    @Override
    default IVector2Mutable asMutable() {
        return IVector2Mutable.of(this);
    }
}
