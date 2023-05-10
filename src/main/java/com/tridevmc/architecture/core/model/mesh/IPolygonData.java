package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

/**
 * Stores additional data shared across the vertices of a polygon.
 * <p>
 * Includes the texture index, tint index, and cull face.
 * <p>
 * Additional data can be added by other implementations.
 */
public interface IPolygonData<S extends IPolygonData<S>> {

    /**
     * Gets the texture index for this polygon.
     *
     * @return The texture index.
     */
    int textureIndex();

    /**
     * Gets the tint index for this polygon.
     *
     * @return The tint index.
     */
    int tintIndex();

    /**
     * Gets the cull face for this polygon.
     *
     * @return The cull face.
     */
    @NotNull
    CullFace cullFace();

    /**
     * Transforms this polygon data by the given transformation.
     * <p>
     * Primarily used for transforming the cull face.
     *
     * @param trans The transformation to apply.
     * @return The transformed polygon data.
     */
    S transform(@NotNull ITrans3 trans);

}
