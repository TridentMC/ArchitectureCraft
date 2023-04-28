package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;

/**
 * Helper class for calculating data about various parts of a mesh while avoiding unnecessary allocations.
 */
public class MeshHelper {

    /**
     * Calculates the normal of a polygon from the given vertices.
     *
     * @param v0 the first vertex.
     * @param v1 the second vertex.
     * @param v2 the third vertex.
     * @return the normal of the polygon.
     */
    public static IVector3Immutable calculateNormal(IVertex v0, IVertex v1, IVertex v2) {
        return calculateNormal(v0.getPos(), v1.getPos(), v2.getPos());
    }

    /**
     * Calculates the normal of a polygon from the given vertices.
     *
     * @param v0 the first vertex.
     * @param v1 the second vertex.
     * @param v2 the third vertex.
     * @return the normal of the polygon.
     */
    public static IVector3Immutable calculateNormal(IVector3 v0, IVector3 v1, IVector3 v2) {
        var nX = (v1.getY() - v0.getY()) * (v2.getZ() - v0.getZ()) -
                (v1.getZ() - v0.getZ()) * (v2.getY() - v0.getY());
        var nY = (v1.getZ() - v0.getZ()) * (v2.getX() - v0.getX()) -
                (v1.getX() - v0.getX()) * (v2.getZ() - v0.getZ());
        var nZ = (v1.getX() - v0.getX()) * (v2.getY() - v0.getY()) -
                (v1.getY() - v0.getY()) * (v2.getX() - v0.getX());
        var nLength = Math.sqrt(nX * nX + nY * nY + nZ * nZ);
        return IVector3.ofImmutable(nX / nLength, nY / nLength, nZ / nLength);
    }

}
