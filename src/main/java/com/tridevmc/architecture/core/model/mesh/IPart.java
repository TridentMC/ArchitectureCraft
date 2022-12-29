package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.Transform;
import com.tridevmc.architecture.core.physics.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Common interface used to represent a part of a mesh and provide access to its faces.
 *
 * @param <I> The type of data used to identify the part.
 * @param <D> The type of data that is stored on the polygons.
 */
public interface IPart<I, D extends IPolygonData> {

    /**
     * Gets the identifier of this part.
     *
     * @return The identifier of this part.
     */
    @NotNull
    I getId();

    /**
     * Gets the faces of this part.
     *
     * @return The faces of this part.
     */
    @NotNull
    ImmutableList<IFace<D>> getFaces();

    /**
     * Gets the bounds of this part.
     *
     * @return The bounds of this part.
     */
    @NotNull
    AABB getBounds();

    /**
     * Applies the given transformation to this part, returning a new part with the transformed faces.
     *
     * @param trans        The transformation to apply.
     * @param transformUVs Whether to transform the UVs of the polygons.
     * @return A new part with the transformed faces.
     */
    @NotNull
    IPart<I, D> transform(@NotNull Transform trans, boolean transformUVs);

    /**
     * Applies the given transformation to this part, returning a new part with the transformed faces.
     *
     * @param trans The transformation to apply.
     * @return A new part with the transformed faces.
     */
    @NotNull
    default IPart<I, D> transform(@NotNull Transform trans) {
        return this.transform(trans, true);
    }

    /**
     * Gets a stream of the faces of this part.
     *
     * @return A stream of the faces of this part.
     */
    @NotNull
    default Stream<IFace<D>> getFaceStream() {
        return this.getFaces().stream();
    }

}
