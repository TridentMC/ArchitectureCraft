package com.tridevmc.architecture.core.math.floating;

import net.minecraft.core.Direction;

/**
 * A 3D vector, primarily used for positions and normals.
 * <p>
 * Can be instantiated using the {@link IVector3F#ofImmutable(float, float, float)} and {@link IVector3F#ofMutable(float, float, float)} methods.
 * <p>
 * For mutable vectors, see {@link IVector3FMutable}, for immutable vectors, see {@link IVector3FImmutable}.
 */
public interface IVector3F {

    IVector3FImmutable ZERO = ofImmutable(0, 0, 0);
    IVector3FImmutable ONE = ofImmutable(1, 1, 1);
    IVector3FImmutable BLOCK_CENTER = ofImmutable(0.5F, 0.5F, 0.5F);
    IVector3FImmutable INV_BLOCK_CENTER = ofImmutable(-0.5F, -0.5F, -0.5F);

    IVector3FImmutable UNIT_X = ofImmutable(1, 0, 0);
    IVector3FImmutable UNIT_Y = ofImmutable(0, 1, 0);
    IVector3FImmutable UNIT_Z = ofImmutable(0, 0, 1);

    IVector3FImmutable UNIT_NX = ofImmutable(-1, 0, 0);
    IVector3FImmutable UNIT_NY = ofImmutable(0, -1, 0);
    IVector3FImmutable UNIT_NZ = ofImmutable(0, 0, -1);

    IVector3FImmutable UNIT_PYNZ = ofImmutable(0, 0.707F, -0.707F);
    IVector3FImmutable UNIT_PXPY = ofImmutable(0.707F, 0.707F, 0);
    IVector3FImmutable UNIT_PYPZ = ofImmutable(0, 0.707F, 0.707F);
    IVector3FImmutable UNIT_NXPY = ofImmutable(-0.707F, 0.707F, 0);

    IVector3FImmutable[] UNITS = new IVector3FImmutable[]{
            UNIT_X, UNIT_Y, UNIT_Z,
            UNIT_NX, UNIT_NY, UNIT_NZ,
            UNIT_PYNZ, UNIT_PXPY, UNIT_PYPZ, UNIT_NXPY
    };
    IVector3FImmutable[][] FACE_BASES = {
            {UNIT_X, UNIT_Z}, // DOWN
            {UNIT_X, UNIT_NZ}, // UP
            {UNIT_NX, UNIT_Y}, // NORTH
            {UNIT_X, UNIT_Y}, // SOUTH
            {UNIT_Z, UNIT_Y}, // WEST
            {UNIT_NZ, UNIT_Y}, // EAST
    };
    IVector3FImmutable[] DIRECTION_VEC = {
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
    static IVector3FImmutable ofImmutable(float x, float y, float z) {
        return IVector3FImmutable.of(x, y, z);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector3F#asImmutable()} instead.
     * <p>
     * See: {@link IVector3FImmutable#of(IVector3F)}.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     **/
    static IVector3FImmutable ofImmutable(IVector3F vec) {
        return IVector3FImmutable.of(vec);
    }

    /**
     * Creates a new mutable vector with the given coordinates.
     * <p>
     * See: {@link IVector3FMutable#of(float, float, float)}.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @param z The Z coordinate of the vector.
     * @return The new vector.
     */
    static IVector3FMutable ofMutable(float x, float y, float z) {
        return IVector3FMutable.of(x, y, z);
    }

    /**
     * Creates a new mutable vector from the given vector.
     * <p>
     * See: {@link IVector3FMutable#of(IVector3F)}.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     */
    static IVector3FMutable ofMutable(IVector3F vec) {
        return IVector3FMutable.of(vec);
    }

    /**
     * Gets the corresponding vector for the given direction.
     *
     * @param direction The direction index.
     * @return The vector.
     */
    static IVector3FImmutable forDirection(int direction) {
        return DIRECTION_VEC[direction];
    }

    /**
     * Gets the corresponding vector for the given direction.
     *
     * @param direction The direction.
     * @return The vector.
     */
    static IVector3FImmutable forDirection(Direction direction) {
        return DIRECTION_VEC[direction.ordinal()];
    }

    /**
     * Gets the X coordinate of the vector.
     *
     * @return The X coordinate of the vector.
     */
    float x();

    /**
     * Gets the Y coordinate of the vector.
     *
     * @return The Y coordinate of the vector.
     */
    float y();

    /**
     * Gets the Z coordinate of the vector.
     *
     * @return The Z coordinate of the vector.
     */
    float z();


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
    IVector3FImmutable asImmutable();

    /**
     * Creates a mutable copy of this vector, with the same coordinates.
     *
     * @return A mutable copy of this vector.
     */
    IVector3FMutable asMutable();

    /**
     * Gets the X coordinate of the vector.
     *
     * @return The X coordinate of the vector.
     */
    default float getX() {
        return this.x();
    }

    /**
     * Gets the Y coordinate of the vector.
     *
     * @return The Y coordinate of the vector.
     */

    default float getY() {
        return this.y();
    }

    /**
     * Gets the Z coordinate of the vector.
     *
     * @return The Z coordinate of the vector.
     */
    default float getZ() {
        return this.z();
    }

    /**
     * Gets the length of the vector.
     *
     * @return The length of the vector.
     */
    default float length() {
        return (float) Math.sqrt(this.lengthSquared());
    }

    /**
     * Gets the squared length of the vector.
     *
     * @return The squared length of the vector.
     */
    default float lengthSquared() {
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
    default float distance(float x, float y, float z) {
        return (float) Math.sqrt(this.distanceSquared(x, y, z));
    }

    /**
     * Gets the distance between this vector and another.
     *
     * @param vec The other vector.
     * @return The distance between the two vectors.
     */
    default float distance(IVector3F vec) {
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
    default float distanceSquared(float x, float y, float z) {
        float dx = this.x() - x;
        float dy = this.y() - y;
        float dz = this.z() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Gets the squared distance between this vector and another.
     *
     * @param vec The other vector.
     * @return The squared distance between the two vectors.
     */
    default float distanceSquared(IVector3F vec) {
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
    default float dot(float x, float y, float z) {
        return this.x() * x + this.y() * y + this.z() * z;
    }

    /**
     * Gets the dot product of this vector and another.
     *
     * @param vec The other vector.
     * @return The dot product of the two vectors.
     */
    default float dot(IVector3F vec) {
        return this.dot(vec.x(), vec.y(), vec.z());
    }

    /**
     * Gets the specified component of this vector.
     *
     * @param index The index of the component to get, 0 for X, 1 for Y, 2 for Z.
     * @return The component of this vector.
     * @throws IndexOutOfBoundsException If the index is not 0, 1 or 2.
     */
    default float getComponent(int index) {
        return switch (index) {
            case 0 -> this.x();
            case 1 -> this.y();
            case 2 -> this.z();
            default -> throw new IndexOutOfBoundsException("Index: " + index);
        };
    }

}
