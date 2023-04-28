package com.tridevmc.architecture.core.math.floating;

import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;

/**
 * A 2D vector, primarily used for texture coordinates.
 * <p>
 * Can be instantiated using the {@link IVector2F#ofImmutable(double, double)} and {@link IVector2F#ofMutable(double, double)} methods.
 * <p>
 * For mutable vectors, see {@link IVector2FMutable}, for immutable vectors, see {@link IVector2FImmutable}.
 */
public interface IVector2F {

    IVector2FImmutable ZERO = IVector2F.ofImmutable(0, 0);

    IVector2FImmutable ONE = IVector2F.ofImmutable(1, 1);

    IVector2FImmutable UNIT_X = IVector2F.ofImmutable(1, 0);

    IVector2FImmutable UNIT_Y = IVector2F.ofImmutable(0, 1);

    IVector2FImmutable UNIT_NX = IVector2F.ofImmutable(-1, 0);

    IVector2FImmutable UNIT_NY = IVector2F.ofImmutable(0, -1);

    /**
     * Creates a new immutable vector with the given coordinates.
     * <p>
     * See {@link IVector2FImmutable#of(double, double)} for more information.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2FImmutable ofImmutable(double x, double y) {
        return IVector2FImmutable.of(x, y);
    }

    /**
     * Creates a new immutable vector from the given vector.
     * <p>
     * Please note that this method will always return a new instance, even if the given vector is already immutable.
     * This has performance implications, it's recommended to use {@link IVector2F#asImmutable()} instead.
     * <p>
     * See: {@link IVector3Immutable#of(IVector3)}
     *
     * @param vector The vector to create an immutable copy of.
     * @return The new immutable vector.
     */
    static IVector2FImmutable ofImmutable(IVector2F vector) {
        return IVector2FImmutable.of(vector);
    }

    /**
     * Creates a new mutable vector with the given coordinates.
     * <p>
     * See: {@link IVector2FMutable#of(double, double)}
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2FMutable ofMutable(double x, double y) {
        return IVector2FMutable.of(x, y);
    }

    /**
     * Creates a new mutable vector from the given vector.
     * <p>
     * See: {@link IVector2FMutable#of(IVector2F)}.
     *
     * @param vec The vector to copy.
     * @return The new vector.
     **/
    static IVector2FMutable ofMutable(IVector2F vec) {
        return IVector2FMutable.of(vec);
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
    IVector2FImmutable asImmutable();

    /**
     * Creates a mutable copy of this vector, with the same coordinates.
     *
     * @return A mutable copy of this vector.
     */
    IVector2FMutable asMutable();

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
    default double distance(IVector2F other) {
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
    default double distanceSquared(IVector2F other) {
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
    default double dot(IVector2F other) {
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
