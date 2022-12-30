package com.tridevmc.architecture.core.physics;


import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygon;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;

import java.util.stream.Stream;

/**
 * A record representing a ray in 3D space, with an origin and direction.
 *
 * @param origin    the origin of the ray.
 * @param direction the direction of the ray.
 */
public record Ray(IVector3Immutable origin, IVector3Immutable direction) {
    /**
     * Attempts to intersect this ray with the given polygon.
     *
     * @param poly the polygon to intersect with.
     * @return a hit containing the point of intersection and the polygon, or an invalid hit if there is no intersection.
     */
    public <D extends IPolygonData> Hit intersect(IPolygon<D> poly) {
        return new Hit(this, poly.intersect(this), poly);
    }

    /**
     * Attempts to intersect this ray with the given mesh, returns a stream of valid hits.
     *
     * @param mesh the mesh to intersect with.
     * @return a stream of valid hits.
     */
    public <I, D extends IPolygonData> Stream<Hit> intersect(IMesh<I, D> mesh) {
        return this.intersectUnfiltered(mesh).filter(Hit::isValidHit);
    }

    /**
     * Attempts to intersect this ray with the given mesh, returns a stream of hits including invalid hits.
     *
     * @param mesh the mesh to intersect with.
     * @return a stream of hits or failed hits.
     */
    public <I, D extends IPolygonData> Stream<Hit> intersectUnfiltered(IMesh<I, D> mesh) {
        return mesh.getAABBTree().search(new AABB(this.origin, this.origin.add(this.direction)))
                .stream()
                .map(this::intersect);
    }

    /**
     * Gets the point on the ray at the given distance.
     *
     * @param t the distance along the ray.
     * @return the point on the ray.
     */
    public IVector3 getPoint(double t) {
        // Avoid a bunch of allocations by just doing the math here.
        var out = this.origin.asMutable();
        return out.add(this.direction.x() * t, this.direction.y() * t, this.direction.z() * t);
    }

    /**
     * A record representing a hit between a ray and a polygon.
     *
     * @param ray   the ray that was used to calculate any hit.
     * @param point the point of intersection, if any.
     * @param poly  the polygon that was hit, if any.
     */
    public record Hit(Ray ray, IVector3 point, IPolygon<? extends IPolygonData> poly) {
        /**
         * Determines if the hit is valid, i.e. if the hit point is not null.
         *
         * @return true if the hit is valid, false otherwise.
         */
        public boolean isValidHit() {
            return this.point != null;
        }

        /**
         * Gets the distance between the hit point and the given point.
         *
         * @param point the point to calculate the distance to.
         * @return the distance between the hit point and the given point.
         */
        public double distanceTo(IVector3 point) {
            return point.distance(this.point);
        }

        public Hit rounded() {
            // Round the hit point to the nearest 256th of a block.
            return new Hit(this.ray, IVector3.ofImmutable(Math.round(this.point.x() * 256) / 256D, Math.round(this.point.y() * 256) / 256D, Math.round(this.point.z() * 256) / 256D), this.poly);
        }
    }
}