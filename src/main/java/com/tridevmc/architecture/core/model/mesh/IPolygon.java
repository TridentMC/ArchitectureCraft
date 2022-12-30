package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.Transform;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.Ray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Common interface used to represent a polygon and provide access to its vertices.
 * <p>
 * Implementations include triangles and quads.
 */
public interface IPolygon<D extends IPolygonData> {

    /**
     * Gets the polygon data associated with this polygon.
     * Provides texture, tint, and cull face information as well as any implementation specific data.
     *
     * @return The polygon data associated with this polygon.
     */
    @NotNull
    D getPolygonData();

    /**
     * Gets the vertices of this polygon.
     *
     * @return The vertices of this polygon.
     */
    @NotNull
    ImmutableList<IVertex> getVertices();

    /**
     * Gets the normal of this polygon.
     *
     * @return The normal of this polygon.
     */
    @NotNull
    LegacyVector3 getNormal();

    /**
     * Gets the axis aligned bounding box of this polygon.
     *
     * @return The axis aligned bounding box of this polygon.
     */
    @NotNull
    AABB getAABB();

    /**
     * Performs a ray intersection test with this polygon, returning the point of intersection if any.
     *
     * @param ray the ray to intersect with.
     * @return the point of intersection, or null if there is no intersection.
     */
    @Nullable
    LegacyVector3 intersect(Ray ray);

    /**
     * Determines if this polygon intersects with the given AABB, returning true if it does and false otherwise.
     *
     * @param box the AABB to test against.
     * @return true if this polygon intersects with the given AABB, false otherwise.
     */
    boolean intersect(AABB box);

    /**
     * Applies the given transformation to this polygon, returning a new polygon with the transformed vertices.
     *
     * @param trans        the transformation to apply.
     * @param transformUVs whether to transform the UVs of this polygon.
     * @return a new polygon with the transformed vertices.
     */
    @NotNull
    IPolygon<D> transform(@NotNull Transform trans, boolean transformUVs);

    /**
     * Applies the given transformation to this polygon, returning a new polygon with the transformed vertices.
     *
     * @param trans the transformation to apply.
     * @return a new polygon with the transformed vertices.
     */
    @NotNull
    default IPolygon<D> transform(@NotNull Transform trans) {
        return this.transform(trans, true);
    }

    /**
     * Determines if this polygon is facing towards a given point.
     *
     * @param point the point to check.
     * @return true if this polygon is facing towards the point, false otherwise.
     */
    default boolean isFacing(@NotNull LegacyVector3 point) {
        return this.getNormal().dot(point.sub(this.getVertices().get(0).getPos())) < 0;
    }

    /**
     * Gets a stream of the vertices of this polygon.
     *
     * @return A stream of the vertices of this polygon.
     */
    @NotNull
    default Stream<IVertex> getVertexStream() {
        return this.getVertices().stream();
    }

}
