package com.tridevmc.architecture.core.math;

/**
 * A 2D vector, primarily used for texture coordinates.
 * <p>
 * Can be instantiated using the {@link IVector2#ofImmutable(double, double)} and {@link IVector2#ofMutable(double, double)} methods.
 * <p>
 * For mutable vectors, see {@link IVector2Mutable}, for immutable vectors, see {@link IVector2Immutable}.
 */
public interface IVector2 {

    IVector2Immutable ZERO = IVector2.ofImmutable(0, 0);

    IVector2Immutable ONE = IVector2.ofImmutable(1, 1);

    IVector2Immutable UNIT_X = IVector2.ofImmutable(1, 0);

    IVector2Immutable UNIT_Y = IVector2.ofImmutable(0, 1);

    IVector2Immutable UNIT_NX = IVector2.ofImmutable(-1, 0);

    IVector2Immutable UNIT_NY = IVector2.ofImmutable(0, -1);

    /**
     * Creates a new immutable vector with the given coordinates.
     * <p>
     * See {@link IVector2Immutable#of(double, double)} for more information.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2Immutable ofImmutable(double x, double y) {
        return IVector2Immutable.of(x, y);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector2#asImmutable()} instead.
     * <p>
     * See: {@link IVector3Immutable#of(IVector3)}
     *
     * @param vector The vector to create an immutable copy of.
     * @return The new immutable vector.
     */
    static IVector2Immutable ofImmutable(IVector2 vector) {
        return IVector2Immutable.of(vector);
    }

    /**
     * Creates a new mutable vector with the given coordinates.
     * <p>
     * See: {@link IVector2Mutable#of(double, double)}
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2Mutable ofMutable(double x, double y) {
        return IVector2Mutable.of(x, y);
    }

    /**
     * Creates a new mutable vector from the given vector.
     * <p>
     * See: {@link IVector2Mutable#of(IVector2)}.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     **/
    static IVector2Mutable ofMutable(IVector2 vec) {
        return IVector2Mutable.of(vec);
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
    IVector2Immutable asImmutable();

    /**
     * Creates a mutable copy of this vector, with the same coordinates.
     *
     * @return A mutable copy of this vector.
     */
    IVector2Mutable asMutable();

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
     * Gets the U coordinate of the vector, which is the same as the X coordinate.
     *
     * @return The U coordinate of the vector.
     */
    default double u() {
        return this.x();
    }

    /**
     * Gets the V coordinate of the vector, which is the same as the Y coordinate.
     *
     * @return The V coordinate of the vector.
     */
    default double v() {
        return this.y();
    }

    /**
     * Gets the U coordinate of the vector, which is the same as the X coordinate.
     *
     * @return The U coordinate of the vector.
     */
    default double getU() {
        return this.x();
    }

    /**
     * Gets the V coordinate of the vector, which is the same as the Y coordinate.
     *
     * @return The V coordinate of the vector.
     */
    default double getV() {
        return this.y();
    }

    /**
     * Calculates the length of the vector.
     *
     * @return The length of the vector.
     */
    default double length() {
        return Math.sqrt(this.lengthSquared());
    }

    /**
     * Calculates the squared length of the vector.
     *
     * @return The squared length of the vector.
     */
    default double lengthSquared() {
        return this.x() * this.x() + this.y() * this.y();
    }

    /**
     * Calculates the distance between this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return The distance between the two vectors.
     */
    default double distance(double x, double y) {
        return Math.sqrt(this.distanceSquared(x, y));
    }

    /**
     * Calculates the squared distance between this vector and another.
     *
     * @param other The other vector.
     * @return The squared distance between the two vectors.
     */
    default double distance(IVector2 other) {
        return this.distance(other.x(), other.y());
    }

    /**
     * Calculates the squared distance between this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return The squared distance between the two vectors.
     */
    default double distanceSquared(double x, double y) {
        double dx = this.x() - x;
        double dy = this.y() - y;
        return dx * dx + dy * dy;
    }

    /**
     * Calculates the squared distance between this vector and another.
     *
     * @param other The other vector.
     * @return The squared distance between the two vectors.
     */
    default double distanceSquared(IVector2 other) {
        return this.distanceSquared(other.x(), other.y());
    }

    /**
     * Calculates the dot product of this vector and another.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return The dot product of the two vectors.
     */
    default double dot(double x, double y) {
        return this.x() * x + this.y() * y;
    }

    /**
     * Calculates the dot product of this vector and another.
     *
     * @param other The other vector.
     * @return The dot product of the two vectors.
     */
    default double dot(IVector2 other) {
        return this.dot(other.x(), other.y());
    }

    /**
     * Gets the specified component of the vector.
     *
     * @param index The index of the component, 0 for X, 1 for Y.
     * @return The component.
     * @throws IndexOutOfBoundsException If the index is not 0 or 1.
     */
    default double getComponent(int index) {
        return switch (index) {
            case 0 -> this.x();
            case 1 -> this.y();
            default -> throw new IndexOutOfBoundsException("Index: " + index);
        };
    }
}
