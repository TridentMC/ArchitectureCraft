package com.tridevmc.architecture.core.math;

import net.minecraft.core.Direction;

/**
 * A 3D vector, primarily used for positions and normals.
 * <p>
 * Can be instantiated using the {@link IVector3#ofImmutable(double, double, double)} and {@link IVector3#ofMutable(double, double, double)} methods.
 * <p>
 * For mutable vectors, see {@link IVector3Mutable}, for immutable vectors, see {@link IVector3Immutable}.
 */
public interface IVector3 {

    IVector3Immutable ZERO = ofImmutable(0, 0, 0);
    IVector3Immutable ONE = ofImmutable(1, 1, 1);
    IVector3Immutable BLOCK_CENTER = ofImmutable(0.5, 0.5, 0.5);
    IVector3Immutable INV_BLOCK_CENTER = ofImmutable(-0.5, -0.5, -0.5);

    IVector3Immutable UNIT_X = ofImmutable(1, 0, 0);
    IVector3Immutable UNIT_Y = ofImmutable(0, 1, 0);
    IVector3Immutable UNIT_Z = ofImmutable(0, 0, 1);

    IVector3Immutable UNIT_NX = ofImmutable(-1, 0, 0);
    IVector3Immutable UNIT_NY = ofImmutable(0, -1, 0);
    IVector3Immutable UNIT_NZ = ofImmutable(0, 0, -1);

    IVector3Immutable UNIT_PYNZ = ofImmutable(0, 0.707, -0.707);
    IVector3Immutable UNIT_PXPY = ofImmutable(0.707, 0.707, 0);
    IVector3Immutable UNIT_PYPZ = ofImmutable(0, 0.707, 0.707);
    IVector3Immutable UNIT_NXPY = ofImmutable(-0.707, 0.707, 0);

    IVector3Immutable[] UNITS = new IVector3Immutable[]{
            UNIT_X, UNIT_Y, UNIT_Z,
            UNIT_NX, UNIT_NY, UNIT_NZ,
            UNIT_PYNZ, UNIT_PXPY, UNIT_PYPZ, UNIT_NXPY
    };
    IVector3Immutable[][] FACE_BASES = {
            {UNIT_X, UNIT_Z}, // DOWN
            {UNIT_X, UNIT_NZ}, // UP
            {UNIT_NX, UNIT_Y}, // NORTH
            {UNIT_X, UNIT_Y}, // SOUTH
            {UNIT_Z, UNIT_Y}, // WEST
            {UNIT_NZ, UNIT_Y}, // EAST
    };
    IVector3Immutable[] DIRECTION_VEC = {
            ofImmutable(0, -1, 0),
            ofImmutable(0, 1, 0),
            ofImmutable(0, 0, -1),
            ofImmutable(0, 0, 1),
            ofImmutable(-1, 0, 0),
            ofImmutable(1, 0, 0)
    };

    /**
     * Creates a new immutable vector with the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3Immutable ofImmutable(double x, double y, double z) {
        return IVector3Immutable.of(x, y, z);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector3#asImmutable()} instead.
     * <p>
     * See: {@link IVector3Immutable#of(IVector3)}.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     **/
    static IVector3Immutable ofImmutable(IVector3 vec) {
        return IVector3Immutable.of(vec);
    }

    /**
     * Creates a new mutable vector with the given coordinates.
     * <p>
     * See: {@link IVector3Mutable#of(double, double, double)}.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3Mutable ofMutable(double x, double y, double z) {
        return IVector3Mutable.of(x, y, z);
    }

    /**
     * Creates a new mutable vector from the given vector.
     * <p>
     * See: {@link IVector3Mutable#of(IVector3)}.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector3Mutable ofMutable(IVector3 vec) {
        return IVector3Mutable.of(vec);
    }

    /**
     * Gets the corresponding vector for the given direction.
     *
     * @param direction The direction index.
     * @return The vector.
     */
    static IVector3Immutable forDirection(int direction) {
        return DIRECTION_VEC[direction];
    }

    /**
     * Gets the corresponding vector for the given direction.
     *
     * @param direction The direction.
     * @return The vector.
     */
    static IVector3Immutable forDirection(Direction direction) {
        return DIRECTION_VEC[direction.ordinal()];
    }

    /**
     * Gets the X coordinate of the vector.
     *
     * @return The X coordinate of the vector.
     */
    double x();

    /**
     * Gets the Y coordinate of the vector.
     *
     * @return The Y coordinate of the vector.
     */
    double y();

    /**
     * Gets the Z coordinate of the vector.
     *
     * @return The Z coordinate of the vector.
     */
    double z();


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
    IVector3Immutable asImmutable();

    /**
     * Creates a mutable copy of this vector, with the same coordinates.
     *
     * @return A mutable copy of this vector.
     */
    IVector3Mutable asMutable();

    /**
     * Gets the X coordinate of the vector.
     *
     * @return The X coordinate of the vector.
     */
    default double getX() {
        return this.x();
    }

    /**
     * Gets the Y coordinate of the vector.
     *
     * @return The Y coordinate of the vector.
     */

    default double getY() {
        return this.y();
    }

    /**
     * Gets the Z coordinate of the vector.
     *
     * @return The Z coordinate of the vector.
     */
    default double getZ() {
        return this.z();
    }

    /**
     * Gets the length of the vector.
     *
     * @return The length of the vector.
     */
    default double length() {
        return Math.sqrt(this.lengthSquared());
    }

    /**
     * Gets the squared length of the vector.
     *
     * @return The squared length of the vector.
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
    default double distance(double x, double y, double z) {
        return Math.sqrt(this.distanceSquared(x, y, z));
    }

    /**
     * Gets the distance between this vector and another.
     *
     * @param vec The other vector.
     * @return The distance between the two vectors.
     */
    default double distance(IVector3 vec) {
        return this.distance(vec.x(), vec.y(), vec.z());
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @param z The Z coordinate of the other vector.
     * @return The squared distance between the two vectors.
     */
    default double distanceSquared(double x, double y, double z) {
        double dx = this.x() - x;
        double dy = this.y() - y;
        double dz = this.z() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param vec The other vector.
     * @return The squared distance between the two vectors.
     */
    default double distanceSquared(IVector3 vec) {
        return this.distanceSquared(vec.x(), vec.y(), vec.z());
    }

    /**
     * Gets the dot product of this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @param z The Z coordinate of the other vector.
     * @return The dot product of the two vectors.
     */
    default double dot(double x, double y, double z) {
        return this.x() * x + this.y() * y + this.z() * z;
    }

    /**
     * Gets the dot product of this vector and another.
     *
     * @param vec The other vector.
     * @return The dot product of the two vectors.
     */
    default double dot(IVector3 vec) {
        return this.dot(vec.x(), vec.y(), vec.z());
    }

    /**
     * Gets the specified component of this vector.
     *
     * @param index The index of the component to get, 0 for X, 1 for Y, 2 for Z.
     * @return The component of this vector.
     * @throws IndexOutOfBoundsException If the index is not 0, 1 or 2.
     */
    default double getComponent(int index) {
        return switch (index) {
            case 0 -> this.x();
            case 1 -> this.y();
            case 2 -> this.z();
            default -> throw new IndexOutOfBoundsException("Index: " + index);
        };
    }

}
