package com.tridevmc.architecture.core.model;

import com.google.common.collect.Lists;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.Ray;
import it.unimi.dsi.fastutil.objects.ObjectDoubleImmutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Performs a series of collision tests on a given mesh to create a voxelized representation of it.
 */
public class Voxelizer {

    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private static final IVector3 xNormal = IVector3.UNIT_X;
    private static final IVector3 yNormal = IVector3.UNIT_Y;
    private static final IVector3 zNormal = IVector3.UNIT_Z;

    private final IMesh<?, ? extends IPolygonData<?>> mesh;
    private final int blockResolution;
    private final double resolution;
    private final IVector3i min, max;
    private final boolean[][][] voxels;
    private CompletableFuture<List<AABB>> simplifiedVoxelsFuture;

    /**
     * Creates a new voxelizer for the given mesh.
     *
     * @param mesh            The mesh to voxelize.
     * @param blockResolution The resolution of the voxel grid, in terms of voxels per 1 unit of length.
     */
    public Voxelizer(IMesh<?, ?> mesh, int blockResolution) {
        this.mesh = mesh;
        this.blockResolution = blockResolution;
        this.resolution = 1.0D / blockResolution;

        var minXBounds = this.mesh.getBounds().minX();
        var minYBounds = this.mesh.getBounds().minY();
        var minZBounds = this.mesh.getBounds().minZ();
        var maxXBounds = this.mesh.getBounds().maxX();
        var maxYBounds = this.mesh.getBounds().maxY();
        var maxZBounds = this.mesh.getBounds().maxZ();

        var minX = (int) (this.resolution * Math.round(minXBounds / this.resolution) / this.resolution);
        var minY = (int) (this.resolution * Math.round(minYBounds / this.resolution) / this.resolution);
        var minZ = (int) (this.resolution * Math.round(minZBounds / this.resolution) / this.resolution);
        var maxX = (int) (this.resolution * Math.round(maxXBounds / this.resolution) / this.resolution);
        var maxY = (int) (this.resolution * Math.round(maxYBounds / this.resolution) / this.resolution);
        var maxZ = (int) (this.resolution * Math.round(maxZBounds / this.resolution) / this.resolution);

        this.min = IVector3i.ofImmutable(minX, minY, minZ);
        this.max = IVector3i.ofImmutable(maxX, maxY, maxZ);

        this.voxels = new boolean[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
    }

    private List<AABB> getSimplifiedVoxelsResultSafely() {
        Objects.requireNonNull(this.simplifiedVoxelsFuture, "Voxelization has not been started yet, call voxelize() first.");
        try {
            return this.simplifiedVoxelsFuture.get();
        } catch (Exception e) {
            ArchitectureLog.error("Failed to voxelize mesh {}, throwing exception", this.mesh);
            throw new RuntimeException("Failed to voxelize mesh " + this.mesh, e);
        }
    }

    /**
     * Performs the voxelization process, blocking until completion.
     *
     * @return A list of AABBs representing the voxels that were found to be occupied.
     */
    public List<AABB> voxelizeNow() {
        if (this.simplifiedVoxelsFuture == null) {
            this.voxelize();
        }

        return this.getSimplifiedVoxelsResultSafely();
    }

    /**
     * Performs the voxelization process asynchronously.
     *
     * @return A future that will complete with a list of AABBs representing the voxels that were found to be occupied.
     */
    public CompletableFuture<List<AABB>> voxelize() {

        // Create a list of all the voxels that are intersected by the mesh, do this with a thread pool to speed up the process.
        var futures = new ArrayList<Future<AABB>>(this.totalVoxels());
        for (var x = this.min.x(); x <= this.max.x(); x++) {
            for (var y = this.min.y(); y <= this.max.y(); y++) {
                for (var z = this.min.z(); z <= this.max.z(); z++) {
                    var box = this.getBoxForOffset(x, y, z);
                    futures.add(POOL.submit(() -> this.isBoxValidVoxel(box) ? box : null));
                }
            }
        }

        // The resulting future should be complete once all the voxels have been checked, so we can simplify the list.
        this.simplifiedVoxelsFuture = CompletableFuture.supplyAsync(() -> futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                ArchitectureLog.error("Failed to voxelize mesh {}, throwing exception", this.mesh);
                throw new RuntimeException("Failed to voxelize mesh " + this.mesh, e);
            }
        }).filter(Objects::nonNull).toList());

        return this.simplifiedVoxelsFuture;
    }

    public AABB getBoxForOffset(int x, int y, int z) {
        double bX = x * this.resolution;
        double bY = y * this.resolution;
        double bZ = z * this.resolution;
        return new AABB(bX, bY, bZ, bX + this.resolution, bY + this.resolution, bZ + this.resolution);
    }

    private int totalVoxels() {
        return this.voxels.length * this.voxels[0].length * this.voxels[0][0].length;
    }

    public boolean isBoxValidVoxel(AABB box) {
        return this.doesBoxIntersect(box) || this.isPointInsideMesh(box.center());
    }

    /**
     * Checks if the given box intersects with the mesh.
     *
     * @param box The box to check.
     * @return True if the box intersects with the mesh, false otherwise.
     */
    public boolean doesBoxIntersect(AABB box) {
        var out = this.mesh.searchStream(box.deflate(1D / (this.blockResolution * 32))).anyMatch(p -> p.intersect(box));
        return out;
    }

    /**
     * Checks if the given point is inside the mesh.
     *
     * @param point The point to check.
     * @return True if the point is inside the mesh, false otherwise.
     */
    private boolean isPointInsideMesh(IVector3 point) {
        var meshBounds = this.mesh.getBounds();
        var fromPoint = IVector3.ofImmutable(meshBounds.minX() - 1, point.y(), point.z());
        var rayDirection = IVector3.ofImmutable(1, 0, 0);
        var ray = new Ray(fromPoint, rayDirection);

        List<ObjectDoubleImmutablePair<Ray.Hit>> hits = ray.intersect(this.mesh).map(h -> {
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

    public IMesh<?, ? extends IPolygonData<?>> mesh() {
        return this.mesh;
    }

    public int blockResolution() {
        return this.blockResolution;
    }

    public double resolution() {
        return this.resolution;
    }

    public IVector3i min() {
        return this.min;
    }

    public IVector3i max() {
        return this.max;
    }
}
