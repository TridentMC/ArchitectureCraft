package com.tridevmc.architecture.core.model.mesh;


import org.jetbrains.annotations.NotNull;

/**
 * Creates a polygon from the given face, data, and vertex indices.
 *
 * @param <T> the type of polygon to create.
 * @param <D> the type of data that is stored on the polygon.
 */
@FunctionalInterface
public interface IPolygonProvider<T extends IPolygon<D>, D extends IPolygonData<D>> {

    /**
     * Creates a polygon from the given face, data, and vertex indices.
     *
     * @param face          the face associated with the polygon.
     * @param data          the data to associate with the polygon.
     * @param vertexIndices the vertex indices to create the polygon from.
     * @return the created polygon.
     */
    T createPolygon(@NotNull IFace<D> face, @NotNull D data, int @NotNull ... vertexIndices);

}
