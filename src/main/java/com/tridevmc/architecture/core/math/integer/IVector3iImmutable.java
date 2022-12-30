package com.tridevmc.architecture.core.math.integer;

/**
 * An extension of {@link IVector3i} that is immutable.
 * <p>
 * Construct using {@link IVector3iImmutable#of(int, int, int)}.
 * <p>
 * See also: {@link IVector3iMutable}
 */
public interface IVector3iImmutable extends IVector3i {

    /**
     * Creates a new immutable vector from the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3iImmutable of(int x, int y, int z) {
        return new Vector3i(x, y, z);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector3i#asImmutable()} instead.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector3iImmutable of(IVector3i vec) {
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
    default IVector3iImmutable asImmutable() {
        // We are immutable, so just return ourselves. It's not like we can be any more immutable.
        return this;
    }

    @Override
    default IVector3iMutable asMutable() {
        return IVector3iMutable.of(this);
    }
}
