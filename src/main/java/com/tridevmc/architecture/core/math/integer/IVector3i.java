package com.tridevmc.architecture.core.math.integer;

import net.minecraft.core.Direction;

/**
 * A 3D vector composed of integers, primarily used for block positions.
 * <p>
 * Can be instantiated using the {@link IVector3i#ofImmutable(int, int, int)} and {@link IVector3i#ofMutable(int, int, int)} methods.
 * <p>
 * For mutable vectors, see {@link IVector3iMutable}, for immutable vectors, see {@link IVector3iImmutable}.
 */
public interface IVector3i {

    static IVector3iImmutable ZERO = ofImmutable(0, 0, 0);
    static IVector3iImmutable ONE = ofImmutable(1, 1, 1);
    static IVector3iImmutable UNIT_X = ofImmutable(1, 0, 0);
    static IVector3iImmutable UNIT_Y = ofImmutable(0, 1, 0);
    static IVector3iImmutable UNIT_Z = ofImmutable(0, 0, 1);

    static IVector3iImmutable UNIT_NX = ofImmutable(-1, 0, 0);
    static IVector3iImmutable UNIT_NY = ofImmutable(0, -1, 0);
    static IVector3iImmutable UNIT_NZ = ofImmutable(0, 0, -1);

    static IVector3iImmutable[] DIRECTION_VEC = {
            ofImmutable(0, -1, 0),
            ofImmutable(0, 1, 0),
            ofImmutable(0, 0, -1),
            ofImmutable(0, 0, 1),
            ofImmutable(-1, 0, 0),
            ofImmutable(1, 0, 0)
    };

    /**
     * Creates a new immutable vector with the given coordinates.
     * <p>
     * See {@link IVector3iImmutable#of(int, int, int)}.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3iImmutable ofImmutable(int x, int y, int z) {
        return IVector3iImmutable.of(x, y, z);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector3i#asImmutable()} instead.
     * <p>
     * See: {@link IVector3iImmutable#of(IVector3i)}
     *
     * @param vector The vector to create an immutable copy of.
     * @return The new immutable vector.
     */
    static IVector3iImmutable ofImmutable(IVector3i vector) {
        return IVector3iImmutable.of(vector);
    }

    /**
     * Creates a new mutable vector with the given coordinates.
     * <p>
     * See: {@link IVector3iMutable#of(int, int, int)}
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3iMutable ofMutable(int x, int y, int z) {
        return IVector3iMutable.of(x, y, z);
    }

    /**
     * Creates a new mutable vector from the given vector.
     * <p>
     * See: {@link IVector3iMutable#of(IVector3i)}
     *
     * @param vector The vector to create a mutable copy of.
     * @return The new mutable vector.
     */
    static IVector3iMutable ofMutable(IVector3i vector) {
        return IVector3iMutable.of(vector);
    }

    /**
     * Gets the corresponding vector for the given direction.
     *
     * @param direction The direction index.
     * @return The vector.
     */
    static IVector3i directionVec(int direction) {
        return DIRECTION_VEC[direction];
    }

    /**
     * Gets the corresponding vector for the given direction.
     *
     * @param direction The direction.
     * @return The vector.
     */
    static IVector3i directionVec(Direction direction) {
        return DIRECTION_VEC[direction.ordinal()];
    }

    /**
     * Gets the X coordinate of this vector.
     *
     * @return The X coordinate.
     */
    int x();

    /**
     * Gets the Y coordinate of this vector.
     *
     * @return The Y coordinate.
     */
    int y();

    /**
     * Gets the Z coordinate of this vector.
     *
     * @return The Z coordinate.
     */
    int z();

    /**
     * Determines if this vector is immutable or not.
     *
     * @return True if this vector is immutable, false otherwise.
     */
    boolean isImmutable();

    /**
     * Determines if this vector is mutable or not.
     *
     * @return True if this vector is mutable, false otherwise.
     */
    boolean isMutable();

    /**
     * Creates an immutable copy of this vector, with the same coordinates.
     * <p>
     * If this vector is already immutable, this method will return itself to avoid unnecessary allocations.
     *
     * @return An immutable copy of this vector.
     */
    IVector3iImmutable asImmutable();

    /**
     * Creates a mutable copy of this vector, with the same coordinates.
     *
     * @return A mutable copy of this vector.
     */
    IVector3iMutable asMutable();

    /**
     * Gets the X coordinate of this vector.
     *
     * @return The X coordinate.
     */
    default int getX() {
        return this.x();
    }

    /**
     * Gets the Y coordinate of this vector.
     *
     * @return The Y coordinate.
     */
    default int getY() {
        return this.y();
    }

    /**
     * Gets the Z coordinate of this vector.
     *
     * @return The Z coordinate.
     */
    default int getZ() {
        return this.z();
    }

    /**
     * Gets the length of this vector.
     *
     * @return The length of this vector.
     */
    default double length() {
        return Math.sqrt(this.lengthSquared());
    }

    /**
     * Gets the squared length of this vector.
     *
     * @return The squared length of this vector.
     */
    default double lengthSquared() {
        return this.x() * this.x() + this.y() * this.y() + this.z() * this.z();
    }

    /**
     * Gets the distance between this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @param z The Z coordinate of the other vector.
     * @return The distance between the two vectors.
     */
    default double distance(int x, int y, int z) {
        return Math.sqrt(this.distanceSquared(x, y, z));
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @param z The Z coordinate of the other vector.
     * @return The squared distance between the two vectors.
     */
    default double distanceSquared(int x, int y, int z) {
        double dx = this.x() - x;
        double dy = this.y() - y;
        double dz = this.z() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Gets the distance between this vector and another.
     *
     * @param other The other vector.
     * @return The distance between the two vectors.
     */
    default double distance(IVector3i other) {
        return this.distance(other.x(), other.y(), other.z());
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param other The other vector.
     * @return The squared distance between the two vectors.
     */
    default double distanceSquared(IVector3i other) {
        return this.distanceSquared(other.x(), other.y(), other.z());
    }

    /**
     * Gets the dot product of this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @param z The Z coordinate of the other vector.
     * @return The dot product of the two vectors.
     */
    default int dot(int x, int y, int z) {
        return this.x() * x + this.y() * y + this.z() * z;
    }

    /**
     * Gets the dot product of this vector and another.
     *
     * @param other The other vector.
     * @return The dot product of the two vectors.
     */
    default int dot(IVector3i other) {
        return this.dot(other.x(), other.y(), other.z());
    }
}
