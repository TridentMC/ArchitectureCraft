package com.tridevmc.architecture.core.physics;

import com.tridevmc.architecture.core.math.IVector3;

public class PhysicsHelper {

    /**
     * Tests whether the given triangle and AABB intersect using the separating axis theorem.
     *
     * @param v0       the position of the first vertex of the triangle.
     * @param v1       the position of the second vertex of the triangle.
     * @param v2       the position of the third vertex of the triangle.
     * @param axis     the separating axis to test.
     * @param aabbSize the size of the AABB, half the distance between its minimum and maximum bounds.
     * @return true if the triangle and AABB intersect, false otherwise.
     */
    public static boolean testSeparatingAxis(IVector3 v0, IVector3 v1, IVector3 v2, IVector3 axis, IVector3 aabbSize) {
        var v0Projection = v0.dot(axis);
        var v1Projection = v1.dot(axis);
        var v2Projection = v2.dot(axis);

        var r = aabbSize.x() * Math.abs(IVector3.UNIT_X.dot(axis)) +
                aabbSize.y() * Math.abs(IVector3.UNIT_Y.dot(axis)) +
                aabbSize.z() * Math.abs(IVector3.UNIT_Z.dot(axis));

        var maxProjection = Math.max(v0Projection, Math.max(v1Projection, v2Projection));
        var minProjection = Math.min(v0Projection, Math.min(v1Projection, v2Projection));

        return Math.max(-maxProjection, minProjection) > r;
    }

    /**
     * Tests whether the given quad and AABB intersect using the separating axis theorem.
     *
     * @param v0       the position of the first vertex of the quad.
     * @param v1       the position of the second vertex of the quad.
     * @param v2       the position of the third vertex of the quad.
     * @param v3       the position of the fourth vertex of the quad.
     * @param axis     the separating axis to test.
     * @param aabbSize the size of the AABB, half the distance between its minimum and maximum bounds.
     * @return true if the quad and AABB intersect, false otherwise.
     */
    public static boolean testSeparatingAxis(IVector3 v0, IVector3 v1, IVector3 v2, IVector3 v3, IVector3 axis, IVector3 aabbSize) {
        double v0Projection = v0.dot(axis);
        double v1Projection = v1.dot(axis);
        double v2Projection = v2.dot(axis);
        double v3Projection = v3.dot(axis);

        double r = aabbSize.x() * Math.abs(IVector3.UNIT_X.dot(axis)) +
                aabbSize.y() * Math.abs(IVector3.UNIT_Y.dot(axis)) +
                aabbSize.z() * Math.abs(IVector3.UNIT_Z.dot(axis));

        double maxProjection = Math.max(v0Projection, Math.max(v1Projection, Math.max(v2Projection, v3Projection)));
        double minProjection = Math.min(v0Projection, Math.min(v1Projection, Math.min(v2Projection, v3Projection)));

        return Math.max(-maxProjection, minProjection) > r;
    }

}
