package com.tridevmc.architecture.core.physics;

import com.tridevmc.architecture.legacy.math.LegacyVector3;

public class PhysicsHelper {

    /**
     * Tests whether the given triangle and AABB intersect using the separating axis theorem.
     *
     * @param v0       the position of the first vertex of the triangle
     * @param v1       the position of the second vertex of the triangle
     * @param v2       the position of the third vertex of the triangle
     * @param axis     the separating axis to test
     * @param aabbSize the size of the AABB, half the distance between its minimum and maximum bounds
     * @return true if the triangle and AABB intersect, false otherwise
     */
    public static boolean testSeparatingAxis(LegacyVector3 v0, LegacyVector3 v1, LegacyVector3 v2, LegacyVector3 axis, LegacyVector3 aabbSize) {
        double v0Projection = v0.dot(axis);
        double v1Projection = v1.dot(axis);
        double v2Projection = v2.dot(axis);

        double r = aabbSize.x() * Math.abs(LegacyVector3.UNIT_X.dot(axis)) +
                aabbSize.y() * Math.abs(LegacyVector3.UNIT_Y.dot(axis)) +
                aabbSize.z() * Math.abs(LegacyVector3.UNIT_Z.dot(axis));

        double maxProjection = Math.max(v0Projection, Math.max(v1Projection, v2Projection));
        double minProjection = Math.min(v0Projection, Math.min(v1Projection, v2Projection));

        return Math.max(-maxProjection, minProjection) > r;
    }
}
