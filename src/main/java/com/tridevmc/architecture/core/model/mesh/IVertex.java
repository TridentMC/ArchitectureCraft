package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
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
    IVector3 getPos();

    /**
     * Gets the normal of this vertex.
     *
     * @return The normal of this vertex.
     */
    @NotNull
    IVector3 getNormal();

    /**
     * Applies the given transformation to this vertex, returning a new vertex with the transformed position and normal.
     *
     * @param face         The face this vertex is a part of.
     * @param trans        The transformation to apply.
     * @param transformUVs Whether to transform the UVs of this vertex.
     * @return A new vertex with the transformed position, normal and UVs.
     */
    @NotNull
    IVertex transform(@NotNull IFace<?> face, @NotNull ITrans3 trans, boolean transformUVs);

    /**
     * Applies the given transformation to this vertex, returning a new vertex with the transformed position, normal, and UVs.
     *
     * @param face  The face this vertex is a part of.
     * @param trans The transformation to apply.
     * @return A new vertex with the transformed position, normal, and UVs.
     */
    @NotNull
    default IVertex transform(@NotNull IFace<?> face, @NotNull ITrans3 trans) {
        return this.transform(face, trans, true);
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

