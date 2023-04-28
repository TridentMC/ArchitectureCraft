package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a payload for a polygon, containing the provider, data, and vertices.
 * <p>
 * This is expected to be used in place of passing a provider, vertices, and data directly to a face builder and allows standard Builder objects to be used by IPolygon implementations.
 *
 * @param <D> the type of data that is stored on the polygon.
 */
public interface IRawPolygonPayload<P extends IPolygon<D>, D extends IPolygonData<D>> {

    /**
     * Creates an immutable record of a raw polygon payload from the given provider, data, and vertices.
     *
     * @param provider the provider for the polygon to be created.
     * @param data     the data to be associated with the polygon.
     * @param vertices the vertices to be used to create the polygon.
     * @param <D>      the type of data that is stored on the polygon.
     * @return an immutable record of a raw polygon payload from the given provider, data, and vertices.
     */
    static <P extends IPolygon<D>, D extends IPolygonData<D>> RawPolygonPayload<P, D> of(IPolygonProvider<P, D> provider, D data, ImmutableList<IVertex> vertices) {
        return new RawPolygonPayload<>(provider, data, vertices);
    }

    /**
     * Gets the provider for the polygon to be created.
     *
     * @return the provider for the polygon to be created.
     */
    @NotNull
    IPolygonProvider<P, D> getProvider();

    /**
     * Gets the data to be associated with the polygon.
     *
     * @return the data to be associated with the polygon.
     */
    @NotNull
    D getData();

    /**
     * Gets the vertices to be used to create the polygon.
     *
     * @return the vertices to be used to create the polygon.
     */
    @NotNull
    ImmutableList<IVertex> getVertices();

}
