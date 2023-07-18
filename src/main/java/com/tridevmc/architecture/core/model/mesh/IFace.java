package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Common interface used to represent a face and provide access to its polygons.
 */
public interface IFace<D extends IPolygonData<D>> {

    /**
     * Gets the vertex at the given index.
     *
     * @param index The index of the vertex to get.
     * @return The vertex at the given index.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    @NotNull
    IVertex getVertex(int index);

    /**
     * Gets the polygons of this face.
     *
     * @return The polygons of this face.
     */
    @NotNull
    ImmutableList<IPolygon<D>> getPolygons();

    /**
     * Gets the vertices of this face, in a set order to be referenced by the polygons.
     *
     * @return The vertices of this face.
     */
    @NotNull
    ImmutableList<IVertex> getVertices();

    /**
     * Gets the normal of this face.
     *
     * @return The normal of this face.
     */
    @NotNull
    IVector3Immutable getNormal();

    /**
     * Applies the given transformation to this face, returning a new face with the transformed polygons.
     *
     * @param trans        the transformation to apply.
     * @param transformUVs whether to transform the UVs of this face.
     * @return a new face with the transformed polygons.
     */
    @NotNull
    IFace<D> transform(@NotNull ITrans3 trans, boolean transformUVs);

    /**
     * Applies the given transformation to this face, returning a new face with the transformed polygons.
     *
     * @param trans the transformation to apply.
     * @return a new face with the transformed polygons.
     */
    @NotNull
    default IFace<D> transform(@NotNull ITrans3 trans) {
        return this.transform(trans, true);
    }

    /**
     * Gets a stream of the polygons of this face.
     *
     * @return A stream of the polygons of this face.
     */
    @NotNull
    default Stream<IPolygon<D>> getPolygonStream() {
        return this.getPolygons().stream();
    }

}
