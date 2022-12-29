package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.Transform;
import com.tridevmc.architecture.core.math.LegacyVector3;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface used to represent a vertex and provide access to its position, normal and texture coordinates.
 */
public interface IVertex {

    /**
     * Gets the position of this vertex.
     *
     * @return The position of this vertex.
     */
    @NotNull
    LegacyVector3 getPos();

    /**
     * Gets the normal of this vertex.
     *
     * @return The normal of this vertex.
     */
    @NotNull
    LegacyVector3 getNormal();

    /**
     * Applies the given transformation to this vertex, returning a new vertex with the transformed position and normal.
     *
     * @param trans        The transformation to apply.
     * @param transformUVs Whether to transform the UVs of this vertex.
     * @return A new vertex with the transformed position, normal and UVs.
     */
    @NotNull
    IVertex transform(@NotNull Transform trans, boolean transformUVs);

    /**
     * Applies the given transformation to this vertex, returning a new vertex with the transformed position, normal, and UVs.
     *
     * @param trans The transformation to apply.
     * @return A new vertex with the transformed position, normal, and UVs.
     */
    @NotNull
    default IVertex transform(@NotNull Transform trans) {
        return this.transform(trans, true);
    }

    /**
     * Gets the x coordinate of this vertex.
     *
     * @return The x coordinate of this vertex.
     */
    default double getX() {
        return this.getPos().x();
    }

    /**
     * Gets the y coordinate of this vertex.
     *
     * @return The y coordinate of this vertex.
     */
    default double getY() {
        return this.getPos().y();
    }

    /**
     * Gets the z coordinate of this vertex.
     *
     * @return The z coordinate of this vertex.
     */
    default double getZ() {
        return this.getPos().z();
    }

    /**
     * Gets the x normal of this vertex.
     *
     * @return The x normal of this vertex.
     */
    default double getNormalX() {
        return this.getNormal().x();
    }

    /**
     * Gets the y normal of this vertex.
     *
     * @return The y normal of this vertex.
     */
    default double getNormalY() {
        return this.getNormal().y();
    }

    /**
     * Gets the z normal of this vertex.
     *
     * @return The z normal of this vertex.
     */
    default double getNormalZ() {
        return this.getNormal().z();
    }

    /**
     * Gets the u texture coordinate of this vertex.
     *
     * @return The u texture coordinate of this vertex.
     */
    double getU();

    /**
     * Gets the v texture coordinate of this vertex.
     *
     * @return The v texture coordinate of this vertex.
     */
    double getV();


}

