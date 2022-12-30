package com.tridevmc.architecture.core.math;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * An extension of {@link IVector2} that allows for mutation of the vector.
 * <p>
 * Construct using {@link IVector2Mutable#of(double, double)}.
 *
 * @see IVector2
 */
public interface IVector2Mutable extends IVector2 {

    /**
     * Creates a new mutable vector with the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2Mutable of(double x, double y) {
        return new Vector2.Mutable(x, y);
    }

    /**
     * Creates a new mutable vector from the given vector.
     *
     * @param vec The vector to create a mutable copy of.
     * @return The new mutable vector.
     */
    static IVector2Mutable of(IVector2 vec) {
        return of(vec.getX(), vec.getY());
    }

    /**
     * Sets the X coordinate of the vector.
     *
     * @param x The X coordinate of the vector.
     * @return This vector.
     */
    IVector2Mutable setX(double x);

    /**
     * Sets the Y coordinate of the vector.
     *
     * @param y The Y coordinate of the vector.
     * @return This vector.
     */
    IVector2Mutable setY(double y);

    /**
     * Sets the X and Y coordinates of the vector.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return This vector.
     */
    IVector2Mutable set(double x, double y);

    /**
     * Sets the U coordinate of the vector, which is the same as the X coordinate.
     *
     * @param u The U coordinate of the vector.
     * @return This vector.
     */
    default IVector2Mutable setU(double u) {
        return this.setX(u);
    }

    /**
     * Sets the V coordinate of the vector, which is the same as the Y coordinate.
     *
     * @param v The V coordinate of the vector.
     * @return This vector.
     */
    default IVector2Mutable setV(double v) {
        return this.setY(v);
    }

    /**
     * Sets the X and Y coordinates of the vector.
     *
     * @param vec The vector to copy the coordinates from.
     * @return This vector.
     */
    default IVector2Mutable set(IVector2 vec) {
        return this.set(vec.x(), vec.y());
    }

    /**
     * Adds the given values to the X and Y coordinates of the vector.
     *
     * @param x The X coordinate to add.
     * @param y The Y coordinate to add.
     * @return This vector.
     */
    default IVector2Mutable add(double x, double y) {
        return this.set(this.x() + x, this.y() + y);
    }

    /**
     * Adds the given values to the X and Y coordinates of the vector.
     *
     * @param vec The vector to add.
     * @return This vector.
     */
    default IVector2Mutable add(IVector2 vec) {
        return this.add(vec.x(), vec.y());
    }

    /**
     * Subtracts the given values from the X and Y coordinates of the vector.
     *
     * @param x The X coordinate to subtract.
     * @param y The Y coordinate to subtract.
     * @return This vector.
     */
    default IVector2Mutable sub(double x, double y) {
        return this.set(this.x() - x, this.y() - y);
    }

    /**
     * Subtracts the given values from the X and Y coordinates of the vector.
     *
     * @param vec The vector to subtract.
     * @return This vector.
     */
    default IVector2Mutable sub(IVector2 vec) {
        return this.sub(vec.x(), vec.y());
    }

    /**
     * Multiplies the X and Y coordinates of the vector by the given values.
     *
     * @param x The X coordinate to multiply.
     * @param y The Y coordinate to multiply.
     * @return This vector.
     */
    default IVector2Mutable mul(double x, double y) {
        return this.set(this.x() * x, this.y() * y);
    }

    /**
     * Multiplies the X and Y coordinates of the vector by the given values.
     *
     * @param vec The vector to multiply.
     * @return This vector.
     */
    default IVector2Mutable mul(IVector2 vec) {
        return this.mul(vec.x(), vec.y());
    }

    /**
     * Divides the X and Y coordinates of the vector by the given values.
     *
     * @param x The X coordinate to divide.
     * @param y The Y coordinate to divide.
     * @return This vector.
     */
    default IVector2Mutable div(double x, double y) {
        return this.set(this.x() / x, this.y() / y);
    }

    /**
     * Divides the X and Y coordinates of the vector by the given values.
     *
     * @param vec The vector to divide.
     * @return This vector.
     */
    default IVector2Mutable div(IVector2 vec) {
        return this.div(vec.x(), vec.y());
    }

    /**
     * Calculates the cross product of this vector and the given vector, and stores the result in this vector.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return This vector.
     */
    default IVector2Mutable cross(double x, double y) {
        return this.set(this.x() * x - this.y() * y, this.x() * y + this.y() * x);
    }

    /**
     * Calculates the cross product of this vector and the given vector, and stores the result in this vector.
     *
     * @param vec The other vector.
     * @return This vector.
     */
    default IVector2Mutable cross(IVector2 vec) {
        return this.cross(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the minimum of this vector and the given values.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @return This vector.
     */
    default IVector2Mutable min(double x, double y) {
        return this.set(Math.min(this.x(), x), Math.min(this.y(), y));
    }

    /**
     * Sets the values of this vector to the minimum of this vector and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector2Mutable min(IVector2 vec) {
        return this.min(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the maximum of this vector and the given values.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @return This vector.
     */
    default IVector2Mutable max(double x, double y) {
        return this.set(Math.max(this.x(), x), Math.max(this.y(), y));
    }

    /**
     * Sets the values of this vector to the maximum of this vector and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector2Mutable max(IVector2 vec) {
        return this.max(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the absolute values of this vector.
     *
     * @return This vector.
     */
    default IVector2Mutable abs() {
        return this.set(Math.abs(this.x()), Math.abs(this.y()));
    }

    /**
     * Sets the values of this vector to the floor values of this vector.
     *
     * @return This vector.
     */
    default IVector2Mutable floor() {
        return this.set(Math.floor(this.x()), Math.floor(this.y()));
    }

    /**
     * Sets the values of this vector to the ceiling values of this vector.
     *
     * @return This vector.
     */
    default IVector2Mutable ceil() {
        return this.set(Math.ceil(this.x()), Math.ceil(this.y()));
    }

    /**
     * Calculates the average of this vector and the given vector, and stores the result in this vector.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return This vector.
     */
    default IVector2Mutable avg(double x, double y) {
        return this.set((this.x() + x) / 2, (this.y() + y) / 2);
    }

    /**
     * Calculates the average of this vector and the given vector, and stores the result in this vector.
     *
     * @param vec The other vector.
     * @return This vector.
     */
    default IVector2Mutable avg(IVector2 vec) {
        return this.avg(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the average of this vector and the given array of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector2Mutable avg(IVector2... vecs) {
        double x = this.x();
        double y = this.y();
        for (IVector2 vec : vecs) {
            x += vec.x();
            y += vec.y();
        }
        return this.set(x / (vecs.length + 1), y / (vecs.length + 1));
    }

    /**
     * Sets the values of this vector to the average of this vector and the given collection of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector2Mutable avg(Collection<? extends IVector2> vecs) {
        double x = this.x();
        double y = this.y();
        for (IVector2 vec : vecs) {
            x += vec.x();
            y += vec.y();
        }
        return this.set(x / (vecs.size() + 1), y / (vecs.size() + 1));
    }

    /**
     * Sets the values of this vector to the average of this vector and the given stream of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector2Mutable avg(Stream<? extends IVector2> vecs) {
        return this.avg(vecs.toList());
    }

    /**
     * Sets the value of the specified component of this vector to the given value.
     *
     * @param component The component to set, 0 for X, 1 for Y.
     * @param value     The value to set.
     * @return This vector.
     * @throws IndexOutOfBoundsException If the component is not 0 or 1.
     */
    default IVector2Mutable setComponent(int component, double value) {
        return switch (component) {
            case 0 -> this.setX(value);
            case 1 -> this.setY(value);
            default -> throw new IllegalArgumentException("Invalid component: " + component);
        };
    }

    @Override
    default boolean isImmutable() {
        return false;
    }

    @Override
    default boolean isMutable() {
        return true;
    }

    @Override
    default IVector2Immutable asImmutable() {
        return IVector2Immutable.of(this);
    }

    @Override
    default IVector2Mutable asMutable() {
        return IVector2Mutable.of(this);
    }
}