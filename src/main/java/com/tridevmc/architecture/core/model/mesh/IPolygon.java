package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.Ray;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Common interface used to represent a polygon and provide access to its vertices.
 * <p>
 * Implementations include triangles and quads.
 */
public interface IPolygon<D extends IPolygonData<D>> {

    /**
     * Gets the polygon data associated with this polygon.
     * Provides texture, tint, and cull face information as well as any implementation specific data.
     *
     * @return The polygon data associated with this polygon.
     */
    @NotNull
    D getPolygonData();

    /**
     * Gets the face that this polygon belongs to.
     *
     * @return The face that this polygon belongs to.
     */
    @NotNull
    IFace<D> getFace();

    /**
     * Gets the number of vertices in this polygon.
     *
     * @return The number of vertices in this polygon.
     */
    int getVertexCount();

    /**
     * Gets a list of the vertex indices of this polygon, in the order they should be rendered
     *
     * @return
     */
    @NotNull
    IntImmutableList getVertexIndices();

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
    IVector3Immutable getNormal();

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
    IVector3 intersect(Ray ray);

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
     * @param face         the face the new polygon is associated with.
     * @param trans        the transformation to apply.
     * @param transformUVs whether to transform the UVs of this polygon.
     * @return a new polygon with the transformed vertices.
     */
    @NotNull
    IPolygon<D> transform(@NotNull IFace<D> face, @NotNull ITrans3 trans, boolean transformUVs);

    /**
     * Applies the given transformation to this polygon, returning a new polygon with the transformed vertices.
     *
     * @param face  the face the new polygon is associated with.
     * @param trans the transformation to apply.
     * @return a new polygon with the transformed vertices.
     */
    @NotNull
    default IPolygon<D> transform(@NotNull IFace<D> face, @NotNull ITrans3 trans) {
        return this.transform(face, trans, true);
    }

    /**
     * Determines if this polygon is facing towards a given point.
     *
     * @param point the point to check.
     * @return true if this polygon is facing towards the point, false otherwise.
     */
    default boolean isFacing(@NotNull IVector3 point) {
        var center = this.getAABB().center();
        return this.getNormal().dot(point.x() - center.x(), point.y() - center.y(), point.z() - center.z()) < 0;
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

    /**
     * Gets the vertex at the given index.
     *
     * @param index the index of the vertex to get.
     * @return the vertex at the given index.
     */
    @NotNull
    default IVertex getVertex(int index) {
        return this.getFace().getVertex(this.getVertexIndices().getInt(index));
    }

}
