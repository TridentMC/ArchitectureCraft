package com.tridevmc.architecture.core.model.voxelize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.Ray;
import it.unimi.dsi.fastutil.objects.ObjectDoubleImmutablePair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IVoxelizer {
    ImmutableList<AABB> voxelizeNow();

    CompletableFuture<ImmutableList<AABB>> voxelize();

    default AABB getBoxForOffset(int x, int y, int z) {
        var resolution = this.getResolution();
        double bX = x * resolution;
        double bY = y * resolution;
        double bZ = z * resolution;
        return new AABB(bX, bY, bZ, bX + resolution, bY + resolution, bZ + resolution);
    }

    default boolean isBoxValidVoxel(AABB box) {
        return this.doesBoxIntersect(box) || this.isPointInsideMesh(box.center());
    }

   default boolean doesBoxIntersect(AABB box) {
       return this.getMesh().searchStream(box.deflate(1D / (this.getBlockResolution() * 32))).anyMatch(p -> p.intersect(box));
   }

    default boolean isPointInsideMesh(IVector3 point) {
        var mesh = this.getMesh();
        var meshBounds = mesh.getBounds();
        var fromPoint = IVector3.ofImmutable(meshBounds.minX() - 1, point.y(), point.z());
        var rayDirection = IVector3.ofImmutable(1, 0, 0);
        var ray = new Ray(fromPoint, rayDirection);

        List<ObjectDoubleImmutablePair<Ray.Hit>> hits = ray.intersect(mesh).map(h -> {
            var hit = h.rounded();
            return ObjectDoubleImmutablePair.of(hit, hit.distanceTo(point));
        }).toList();

        if (hits.isEmpty()) {
            return false;
        }
        // We have to collect all the closest points, so we can choose an option if there are multiple.
        // This is a safeguard against any bad geometry that might be present in the mesh.
        var closestHits = Lists.newArrayList(hits.get(0));
        for (var i = 1; i < hits.size(); i++) {
            var hitData = hits.get(i);
            if (Double.compare(hitData.rightDouble(), closestHits.get(0).rightDouble()) < 0) {
                closestHits.clear();
                closestHits.add(hitData);
            } else if (Double.compare(hitData.rightDouble(), closestHits.get(0).rightDouble()) == 0) {
                closestHits.add(hitData);
            }
        }

        for (var closestHit : closestHits) {
            if (closestHit.left().poly().isFacing(point)) {
                return true;
            }
        }

        return false;
    }

    IMesh<?, ? extends IPolygonData<?>> getMesh();

    int getBlockResolution();

    double getResolution();

    IVector3i getMin();

    IVector3i getMax();

}
