package com.tridevmc.architecture.core.model.mesh;

import org.jetbrains.annotations.NotNull;

/**
 * Stores additional data shared across the vertices of a polygon.
 * <p>
 * Includes the texture index, tint index, and cull face.
 * <p>
 * Additional data can be added by other implementations.
 */
public interface IPolygonData {

    /**
     * Gets the texture index for this polygon.
     *
     * @return The texture index.
     */
    int getTextureIndex();

    /**
     * Gets the tint index for this polygon.
     *
     * @return The tint index.
     */
    int getTintIndex();

    /**
     * Gets the cull face for this polygon.
     *
     * @return The cull face.
     */
    @NotNull
    CullFace getCullFace();

}
