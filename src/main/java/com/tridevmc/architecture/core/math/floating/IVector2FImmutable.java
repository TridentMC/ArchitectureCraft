package com.tridevmc.architecture.core.math.floating;

/**
 * An extension of {@link IVector2F} that is expected to be immutable.
 * <p>
 * Construct using {@link IVector2FImmutable#of(double, double)}.
 * <p>
 * See also {@link IVector2F}, and {@link IVector2FMutable}.
 */
public interface IVector2FImmutable extends IVector2F {

    /**
     * Creates a new immutable vector from the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2FImmutable of(double x, double y) {
        return new Vector2F(x, y);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector2F#asImmutable()} instead.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector2FImmutable of(IVector2F vec) {
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
    default IVector2FImmutable asImmutable() {
        return this;
    }

    @Override
    default IVector2FMutable asMutable() {
        return IVector2FMutable.of(this);
    }

}
