package com.tridevmc.architecture.core.math.floating;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * An extension of {@link IVector2F} that allows for mutation of the vector.
 * <p>
 * Construct using {@link IVector2FMutable#of(double, double)}.
 *
 * @see IVector2F
 */
public interface IVector2FMutable extends IVector2F {

    /**
     * Creates a new mutable vector with the given coordinates.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return The new vector.
     */
    static IVector2FMutable of(double x, double y) {
        return new Vector2F.Mutable(x, y);
    }

    /**
     * Creates a new mutable vector from the given vector.
     *
     * @param vec The vector to create a mutable copy of.
     * @return The new mutable vector.
     */
    static IVector2FMutable of(IVector2F vec) {
        return of(vec.getX(), vec.getY());
    }

    /**
     * Sets the X coordinate of the vector.
     *
     * @param x The X coordinate of the vector.
     * @return This vector.
     */
    IVector2FMutable setX(double x);

    /**
     * Sets the Y coordinate of the vector.
     *
     * @param y The Y coordinate of the vector.
     * @return This vector.
     */
    IVector2FMutable setY(double y);

    /**
     * Sets the X and Y coordinates of the vector.
     *
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     * @return This vector.
     */
    IVector2FMutable set(double x, double y);

    /**
     * Sets the U coordinate of the vector, which is the same as the X coordinate.
     *
     * @param u The U coordinate of the vector.
     * @return This vector.
     */
    default IVector2FMutable setU(double u) {
        return this.setX(u);
    }

    /**
     * Sets the V coordinate of the vector, which is the same as the Y coordinate.
     *
     * @param v The V coordinate of the vector.
     * @return This vector.
     */
    default IVector2FMutable setV(double v) {
        return this.setY(v);
    }

    /**
     * Sets the X and Y coordinates of the vector.
     *
     * @param vec The vector to copy the coordinates from.
     * @return This vector.
     */
    default IVector2FMutable set(IVector2F vec) {
        return this.set(vec.x(), vec.y());
    }

    /**
     * Adds the given values to the X and Y coordinates of the vector.
     *
     * @param x The X coordinate to add.
     * @param y The Y coordinate to add.
     * @return This vector.
     */
    default IVector2FMutable add(double x, double y) {
        return this.set(this.x() + x, this.y() + y);
    }

    /**
     * Adds the given values to the X and Y coordinates of the vector.
     *
     * @param vec The vector to add.
     * @return This vector.
     */
    default IVector2FMutable add(IVector2F vec) {
        return this.add(vec.x(), vec.y());
    }

    /**
     * Subtracts the given values from the X and Y coordinates of the vector.
     *
     * @param x The X coordinate to subtract.
     * @param y The Y coordinate to subtract.
     * @return This vector.
     */
    default IVector2FMutable sub(double x, double y) {
        return this.set(this.x() - x, this.y() - y);
    }

    /**
     * Subtracts the given values from the X and Y coordinates of the vector.
     *
     * @param vec The vector to subtract.
     * @return This vector.
     */
    default IVector2FMutable sub(IVector2F vec) {
        return this.sub(vec.x(), vec.y());
    }

    /**
     * Multiplies the X and Y coordinates of the vector by the given values.
     *
     * @param x The X coordinate to multiply.
     * @param y The Y coordinate to multiply.
     * @return This vector.
     */
    default IVector2FMutable mul(double x, double y) {
        return this.set(this.x() * x, this.y() * y);
    }

    /**
     * Multiplies the X and Y coordinates of the vector by the given values.
     *
     * @param vec The vector to multiply.
     * @return This vector.
     */
    default IVector2FMutable mul(IVector2F vec) {
        return this.mul(vec.x(), vec.y());
    }

    /**
     * Divides the X and Y coordinates of the vector by the given values.
     *
     * @param x The X coordinate to divide.
     * @param y The Y coordinate to divide.
     * @return This vector.
     */
    default IVector2FMutable div(double x, double y) {
        return this.set(this.x() / x, this.y() / y);
    }

    /**
     * Divides the X and Y coordinates of the vector by the given values.
     *
     * @param vec The vector to divide.
     * @return This vector.
     */
    default IVector2FMutable div(IVector2F vec) {
        return this.div(vec.x(), vec.y());
    }

    /**
     * Calculates the cross product of this vector and the given vector, and stores the result in this vector.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return This vector.
     */
    default IVector2FMutable cross(double x, double y) {
        return this.set(this.x() * x - this.y() * y, this.x() * y + this.y() * x);
    }

    /**
     * Calculates the cross product of this vector and the given vector, and stores the result in this vector.
     *
     * @param vec The other vector.
     * @return This vector.
     */
    default IVector2FMutable cross(IVector2F vec) {
        return this.cross(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the minimum of this vector and the given values.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @return This vector.
     */
    default IVector2FMutable min(double x, double y) {
        return this.set(Math.min(this.x(), x), Math.min(this.y(), y));
    }

    /**
     * Sets the values of this vector to the minimum of this vector and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector2FMutable min(IVector2F vec) {
        return this.min(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the maximum of this vector and the given values.
     *
     * @param x The X coordinate to compare.
     * @param y The Y coordinate to compare.
     * @return This vector.
     */
    default IVector2FMutable max(double x, double y) {
        return this.set(Math.max(this.x(), x), Math.max(this.y(), y));
    }

    /**
     * Sets the values of this vector to the maximum of this vector and the given vector.
     *
     * @param vec The vector to compare.
     * @return This vector.
     */
    default IVector2FMutable max(IVector2F vec) {
        return this.max(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the absolute values of this vector.
     *
     * @return This vector.
     */
    default IVector2FMutable abs() {
        return this.set(Math.abs(this.x()), Math.abs(this.y()));
    }

    /**
     * Sets the values of this vector to the floor values of this vector.
     *
     * @return This vector.
     */
    default IVector2FMutable floor() {
        return this.set(Math.floor(this.x()), Math.floor(this.y()));
    }

    /**
     * Sets the values of this vector to the ceiling values of this vector.
     *
     * @return This vector.
     */
    default IVector2FMutable ceil() {
        return this.set(Math.ceil(this.x()), Math.ceil(this.y()));
    }

    /**
     * Calculates the average of this vector and the given vector, and stores the result in this vector.
     *
     * @param x The X coordinate of the other vector.
     * @param y The Y coordinate of the other vector.
     * @return This vector.
     */
    default IVector2FMutable avg(double x, double y) {
        return this.set((this.x() + x) / 2, (this.y() + y) / 2);
    }

    /**
     * Calculates the average of this vector and the given vector, and stores the result in this vector.
     *
     * @param vec The other vector.
     * @return This vector.
     */
    default IVector2FMutable avg(IVector2F vec) {
        return this.avg(vec.x(), vec.y());
    }

    /**
     * Sets the values of this vector to the average of this vector and the given array of vectors.
     *
     * @param vecs The vectors to average.
     * @return This vector.
     */
    default IVector2FMutable avg(IVector2F... vecs) {
        double x = this.x();
        double y = this.y();
        for (IVector2F vec : vecs) {
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
    default IVector2FMutable avg(Collection<? extends IVector2F> vecs) {
        double x = this.x();
        double y = this.y();
        for (IVector2F vec : vecs) {
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
    default IVector2FMutable avg(Stream<? extends IVector2F> vecs) {
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
    default IVector2FMutable setComponent(int component, double value) {
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
    default IVector2FImmutable asImmutable() {
        return IVector2FImmutable.of(this);
    }

    @Override
    default IVector2FMutable asMutable() {
        return IVector2FMutable.of(this);
    }

}