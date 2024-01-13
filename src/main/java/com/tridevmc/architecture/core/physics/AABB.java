package com.tridevmc.architecture.core.physics;

import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import com.tridevmc.architecture.core.model.mesh.IVertex;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents an axis-aligned bounding box, used in place of the Minecraft implementation
 * but can be converted to an instance of {@link net.minecraft.world.phys.AABB} for use within Minecraft's codebase.
 *
 * @param min The minimum point of the box.
 * @param max The maximum point of the box.
 */
public record AABB(@NotNull IVector3Immutable min, @NotNull IVector3Immutable max) {

    public static final AABB BLOCK_FULL = new AABB(0, 0, 0, 1, 1, 1);

    public AABB {
        if (min.x() > max.x() || min.y() > max.y() || min.z() > max.z()) {
            throw new IllegalArgumentException(String.format("Invalid AABB, min point (%s) is greater than max point (%s)", min, max));
        }
    }

    public AABB(@NotNull IVector3 min, @NotNull IVector3 max) {
        this(min.asImmutable(), max.asImmutable());
    }

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this(IVector3.ofImmutable(minX, minY, minZ), IVector3.ofImmutable(maxX, maxY, maxZ));
    }

    public static AABB fromVertices(IVertex... vertices) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (var vertex : vertices) {
            minX = Math.min(minX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            minZ = Math.min(minZ, vertex.getZ());
            maxX = Math.max(maxX, vertex.getX());
            maxY = Math.max(maxY, vertex.getY());
            maxZ = Math.max(maxZ, vertex.getZ());
        }

        return new AABB(
                minX, minY, minZ,
                maxX, maxY, maxZ
        );
    }

    public static AABB fromVertices(Collection<IVertex> vertices) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (var vertex : vertices) {
            minX = Math.min(minX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            minZ = Math.min(minZ, vertex.getZ());
            maxX = Math.max(maxX, vertex.getX());
            maxY = Math.max(maxY, vertex.getY());
            maxZ = Math.max(maxZ, vertex.getZ());
        }

        return new AABB(
                minX, minY, minZ,
                maxX, maxY, maxZ
        );
    }

    public static AABB fromVertices(IVertex v0, IVertex v1, IVertex v2) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        minX = Math.min(minX, Math.min(v0.getX(), Math.min(v1.getX(), v2.getX())));
        minY = Math.min(minY, Math.min(v0.getY(), Math.min(v1.getY(), v2.getY())));
        minZ = Math.min(minZ, Math.min(v0.getZ(), Math.min(v1.getZ(), v2.getZ())));
        maxX = Math.max(maxX, Math.max(v0.getX(), Math.max(v1.getX(), v2.getX())));
        maxY = Math.max(maxY, Math.max(v0.getY(), Math.max(v1.getY(), v2.getY())));
        maxZ = Math.max(maxZ, Math.max(v0.getZ(), Math.max(v1.getZ(), v2.getZ())));

        return new AABB(
                minX, minY, minZ,
                maxX, maxY, maxZ
        );
    }

    public static AABB fromVertices(IVertex v0, IVertex v1, IVertex v2, IVertex v3) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        minX = Math.min(minX, Math.min(v0.getX(), Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()))));
        minY = Math.min(minY, Math.min(v0.getY(), Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
        minZ = Math.min(minZ, Math.min(v0.getZ(), Math.min(v1.getZ(), Math.min(v2.getZ(), v3.getZ()))));
        maxX = Math.max(maxX, Math.max(v0.getX(), Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
        maxY = Math.max(maxY, Math.max(v0.getY(), Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));
        maxZ = Math.max(maxZ, Math.max(v0.getZ(), Math.max(v1.getZ(), Math.max(v2.getZ(), v3.getZ()))));

        return new AABB(
                minX, minY, minZ,
                maxX, maxY, maxZ
        );
    }

    /**
     * Gets the minimum point of the box on the X axis.
     *
     * @return The minimum point of the box on the X axis.
     */
    public double minX() {
        return this.min.x();
    }

    /**
     * Gets the minimum point of the box on the Y axis.
     *
     * @return The minimum point of the box on the Y axis.
     */
    public double minY() {
        return this.min.y();

    }

    /**
     * Gets the minimum point of the box on the Z axis.
     *
     * @return The minimum point of the box on the Z axis.
     */
    public double minZ() {
        return this.min.z();
    }

    /**
     * Gets the maximum point of the box on the X axis.
     *
     * @return The maximum point of the box on the X axis.
     */
    public double maxX() {
        return this.max.x();
    }

    /**
     * Gets the maximum point of the box on the Y axis.
     *
     * @return The maximum point of the box on the Y axis.
     */
    public double maxY() {
        return this.max.y();
    }

    /**
     * Gets the maximum point of the box on the Z axis.
     *
     * @return The maximum point of the box on the Z axis.
     */
    public double maxZ() {
        return this.max.z();
    }

    /**
     * Checks if this box intersects with another.
     *
     * @param other The other box to check.
     * @return True if the boxes intersect, false otherwise.
     */
    public boolean intersects(AABB other) {
        return this.maxX() >= other.minX() && this.minX() <= other.maxX() &&
                this.maxY() >= other.minY() && this.minY() <= other.maxY() &&
                this.maxZ() >= other.minZ() && this.minZ() <= other.maxZ();
    }

    public Stream<IVector3> intersects(Ray ray) {
        // We can implement this using the slab method described here: https://tavianator.com/fast-branchless-raybounding-box-intersections/
        var tMin = Double.NEGATIVE_INFINITY;
        var tMax = Double.POSITIVE_INFINITY;

        for (var i = 0; i < 3; i++) {
            var t1 = (this.min(i) - ray.origin().getComponent(i)) / ray.direction().getComponent(i);
            var t2 = (this.max(i) - ray.origin().getComponent(i)) / ray.direction().getComponent(i);
            if (t1 > t2) {
                var temp = t1;
                t1 = t2;
                t2 = temp;
            }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMin > tMax) {
                return Stream.empty();
            }
        }
        return Stream.of(ray.getPoint(tMin), ray.getPoint(tMax));
    }

    /**
     * Checks if this box contains another.
     *
     * @param other The other box to check.
     * @return True if this box contains the other, false otherwise.
     */
    public boolean contains(AABB other) {
        return this.maxX() >= other.maxX() && this.minX() <= other.minX() &&
                this.maxY() >= other.maxY() && this.minY() <= other.minY() &&
                this.maxZ() >= other.maxZ() && this.minZ() <= other.minZ();
    }

    /**
     * Checks if this box contains a point.
     *
     * @param point The point to check.
     * @return True if this box contains the point, false otherwise.
     */
    public boolean contains(IVector3 point) {
        return this.contains(point.x(), point.y(), point.z());
    }

    /**
     * Checks if this box contains a point.
     *
     * @param x The X coordinate of the point.
     * @param y The Y coordinate of the point.
     * @param z The Z coordinate of the point.
     * @return True if this box contains the point, false otherwise.
     */
    public boolean contains(double x, double y, double z) {
        return this.maxX() >= x && this.minX() <= x &&
                this.maxY() >= y && this.minY() <= y &&
                this.maxZ() >= z && this.minZ() <= z;
    }

    /**
     * Checks if this box is adjacent to another.
     *
     * @param other The other box to check.
     * @return True if the boxes are adjacent, false otherwise.
     */
    public boolean isAdjacent(AABB other) {
        return (this.minX() == other.maxX() || this.maxX() == other.minX()) ||
                (this.minY() == other.maxY() || this.maxY() == other.minY()) ||
                (this.minZ() == other.maxZ() || this.maxZ() == other.minZ());
    }

    /**
     * Checks if this box shares a face with another.
     *
     * @param other The other box to check.
     * @return True if the boxes share a face, false otherwise.
     */
    public boolean sharesFace(AABB other) {
        // Calculate the offset between the two boxes, and use that to choose which face to check.
        var dir = other.center().asMutable().sub(this.center());
        // Confirm that at least two of the axes are 0, otherwise the boxes are not adjacent.
        if(Math.ceil(dir.x()) + Math.ceil(dir.y()) + Math.ceil(dir.z()) > 1) {
            return false;
        }
        var face = Direction.getNearest(dir.x(), dir.y(), dir.z());

        switch (face) {
            case NORTH -> {
                return this.minZ() == other.maxZ()
                        && this.minX() == other.minX()
                        && this.maxX() == other.maxX()
                        && this.minY() == other.minY()
                        && this.maxY() == other.maxY();
            }
            case SOUTH -> {
                return this.maxZ() == other.minZ()
                        && this.minX() == other.minX()
                        && this.maxX() == other.maxX()
                        && this.minY() == other.minY()
                        && this.maxY() == other.maxY();
            }
            case EAST -> {
                return this.maxX() == other.minX()
                        && this.minZ() == other.minZ()
                        && this.maxZ() == other.maxZ()
                        && this.minY() == other.minY()
                        && this.maxY() == other.maxY();
            }
            case WEST -> {
                return this.minX() == other.maxX()
                        && this.minZ() == other.minZ()
                        && this.maxZ() == other.maxZ()
                        && this.minY() == other.minY()
                        && this.maxY() == other.maxY();
            }
            case UP -> {
                return this.maxY() == other.minY()
                        && this.minX() == other.minX()
                        && this.maxX() == other.maxX()
                        && this.minZ() == other.minZ()
                        && this.maxZ() == other.maxZ();
            }
            case DOWN -> {
                return this.minY() == other.maxY()
                        && this.minX() == other.minX()
                        && this.maxX() == other.maxX()
                        && this.minZ() == other.minZ()
                        && this.maxZ() == other.maxZ();
            }
            default -> throw new IllegalStateException("Unexpected value: " + face);
        }
    }

    /**
     * Creates a new box that encompasses both this box and another.
     *
     * @param other The other box to encompass.
     * @return The encompassing box.
     */
    public AABB union(AABB other) {
        return new AABB(
                this.min().min(other.min()),
                this.max().max(other.max())
        );
    }

    /**
     * Creates a new box that encompasses both this box and a point.
     *
     * @param x The X coordinate of the point.
     * @param y The Y coordinate of the point.
     * @param z The Z coordinate of the point.
     * @return The encompassing box.
     */
    public AABB union(double x, double y, double z) {
        return new AABB(
                this.min().min(x, y, z),
                this.max().max(x, y, z)
        );
    }

    /**
     * Grows this box by the specified amount, leaving the minimum point unchanged.
     *
     * @param x The amount to grow the box in the X axis.
     * @param y The amount to grow the box in the Y axis.
     * @param z The amount to grow the box in the Z axis.
     * @return The grown box.
     */
    public AABB grow(double x, double y, double z) {
        return new AABB(
                this.min(),
                this.max().add(x, y, z)
        );
    }

    /**
     * Grows this box by the specified amount, leaving the minimum point unchanged.
     *
     * @param amount The amount to grow the box in all axes.
     * @return The grown box.
     */
    public AABB grow(double amount) {
        return this.grow(amount, amount, amount);
    }

    /**
     * Shrinks this box by the specified amount, leaving the minimum point unchanged.
     *
     * @param x The amount to shrink the box in the X axis.
     * @param y The amount to shrink the box in the Y axis.
     * @param z The amount to shrink the box in the Z axis.
     * @return The shrunk box.
     */
    public AABB shrink(double x, double y, double z) {
        return new AABB(
                this.min(),
                this.max().sub(x, y, z)
        );
    }

    /**
     * Shrinks this box by the specified amount, leaving the minimum point unchanged.
     *
     * @param amount The amount to shrink the box in all axes.
     * @return The shrunk box.
     */
    public AABB shrink(double amount) {
        return this.shrink(amount, amount, amount);
    }

    /**
     * Inflates this box around it's center by the given amount.
     *
     * @param x The amount to inflate in the X axis.
     * @param y The amount to inflate in the Y axis.
     * @param z The amount to inflate in the Z axis.
     * @return The inflated box.
     */
    public AABB inflate(double x, double y, double z) {
        return new AABB(
                this.min().sub(x / 2, y / 2, z / 2),
                this.max().add(x / 2, y / 2, z / 2)
        );
    }

    /**
     * Inflate this box around it's center by the given amount.
     *
     * @param size The amount to inflate in all axes.
     * @return The inflated box.
     */
    public AABB inflate(double size) {
        return this.inflate(size, size, size);
    }

    /**
     * Deflates this box around it's center by the given amount.
     *
     * @param x The amount to deflate in the X axis.
     * @param y The amount to deflate in the Y axis.
     * @param z The amount to deflate in the Z axis.
     * @return The deflated box.
     */
    public AABB deflate(double x, double y, double z) {
        return new AABB(
                this.min().add(x / 2, y / 2, z / 2),
                this.max().sub(x / 2, y / 2, z / 2)
        );
    }

    /**
     * Deflates this box around it's center by the given amount.
     *
     * @param size The amount to deflate in all axes.
     * @return The deflated box.
     */
    public AABB deflate(double size) {
        return this.deflate(size, size, size);
    }

    /**
     * Gets the center point of this box.
     *
     * @return The center point.
     */
    public IVector3 center() {
        return this.min().asMutable().add(this.max()).div(2);
    }

    /**
     * Gets the size of this box.
     *
     * @return The size of the box.
     */
    public IVector3 size() {
        return this.max().sub(this.min());
    }

    /**
     * Gets the size of this box along the X axis.
     *
     * @return The size of the box along the X axis.
     */
    public double getXSize() {
        return this.maxX() - this.minX();
    }

    /**
     * Gets the size of this box along the Y axis.
     *
     * @return The size of the box along the Y axis.
     */
    public double getYSize() {
        return this.maxY() - this.minY();
    }

    /**
     * Gets the size of this box along the Z axis.
     *
     * @return The size of the box along the Z axis.
     */
    public double getZSize() {
        return this.maxZ() - this.minZ();
    }

    /**
     * Gets the minimum point of the box on the given axis.
     *
     * @param axis The axis to get the minimum point on, 0 for X, 1 for Y, 2 for Z.
     * @return The minimum point on the given axis.
     */
    public double min(int axis) {
        return this.min().getComponent(axis);
    }

    /**
     * Gets the maximum point of the box on the given axis.
     *
     * @param axis The axis to get the maximum point on, 0 for X, 1 for Y, 2 for Z.
     * @return The maximum point on the given axis.
     */
    public double max(int axis) {
        return this.max().getComponent(axis);
    }

    /**
     * Converts this box to an equivalent {@link net.minecraft.world.phys.AABB}.
     *
     * @return The equivalent {@link net.minecraft.world.phys.AABB}.
     */
    public net.minecraft.world.phys.AABB toMC() {
        return new net.minecraft.world.phys.AABB(
                this.minX(),
                this.minY(),
                this.minZ(),
                this.maxX(),
                this.maxY(),
                this.maxZ()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AABB aabb)) return false;
        return this.min.equals(aabb.min) && this.max.equals(aabb.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.min(),
                this.max()
        );
    }

}
