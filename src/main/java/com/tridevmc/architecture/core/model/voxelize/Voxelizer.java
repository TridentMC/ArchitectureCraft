package com.tridevmc.architecture.core.model.voxelize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.physics.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Performs a series of collision tests on a given mesh to create a voxelized representation of it.
 */
public class Voxelizer implements IVoxelizer {
    private static final ExecutorService POOL = Executors.newWorkStealingPool();
    private final IMesh<?, ? extends IPolygonData<?>> mesh;
    private final int blockResolution;
    private final double resolution;
    private final IVector3i min, max;
    private final boolean[][][] voxels;
    private CompletableFuture<ImmutableList<AABB>> simplifiedVoxelsFuture;

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

    private ImmutableList<AABB> getSimplifiedVoxelsResultSafely() {
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
    public ImmutableList<AABB> voxelizeNow() {
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
    public CompletableFuture<ImmutableList<AABB>> voxelize() {
        if (this.simplifiedVoxelsFuture != null) {
            ArchitectureLog.debug("Voxelization of mesh {} has already been started, returning existing future.", this.mesh.getName());
            return this.simplifiedVoxelsFuture;
        }

        // Create a list of all the voxels that are intersected by the mesh, do this with a thread pool to speed up the process.
        Future<AABB>[][][] futures = new Future[this.voxels.length][this.voxels[0].length][this.voxels[0][0].length];
        for (var x = this.min.x(); x <= this.max.x(); x++) {
            for (var y = this.min.y(); y <= this.max.y(); y++) {
                for (var z = this.min.z(); z <= this.max.z(); z++) {
                    var box = this.getBoxForOffset(x, y, z);
                    var xIndex = x - this.min.x();
                    var yIndex = y - this.min.y();
                    var zIndex = z - this.min.z();
                    futures[xIndex][yIndex][zIndex] = POOL.submit(() -> this.isBoxValidVoxel(box) ? box : null);
                }
            }
        }

        var allRawVoxelsFuture = CompletableFuture.supplyAsync(() -> {
            var out = new AABB[this.voxels.length][this.voxels[0].length][this.voxels[0][0].length];
            for (var x = 0; x < out.length; x++) {
                for (var y = 0; y < out[x].length; y++) {
                    for (var z = 0; z < out[x][y].length; z++) {
                        try {
                            out[x][y][z] = futures[x][y][z].get();
                            this.voxels[x][y][z] = out[x][y][z] != null;
                        } catch (Exception e) {
                            ArchitectureLog.error("Failed to get result for voxel at {}, {}, {}, throwing exception", x, y, z);
                            throw new RuntimeException("Failed to get result for voxel at " + x + ", " + y + ", " + z, e);
                        }
                    }
                }
            }
            return out;
        });

        // The resulting future should be complete once all the voxels have been checked, so we can simplify the list.
        this.simplifiedVoxelsFuture = allRawVoxelsFuture.thenApplyAsync(f -> this.compressVoxels(f).collect(
                ImmutableList.toImmutableList()
        ), POOL);

        return this.simplifiedVoxelsFuture;
    }

    private Stream<AABB> compressVoxels(AABB[][][] grid) {
        var xSize = grid.length;
        var ySize = grid[0].length;

        List<AABB>[] planes = new List[xSize];
        for (var x = 0; x < xSize; x++) {
            List<AABB>[] xPlane = new List[ySize];
            for (var y = 0; y < ySize; y++) {
                var compressedLine = this.compressLine(grid[x][y]).toArray(AABB[]::new);
                xPlane[y] = xPlane[y] == null ? Lists.newArrayList() : xPlane[y];
                xPlane[y].addAll(Lists.newArrayList(compressedLine));
            }

            var compressedPlane = this.compressPlane(xPlane).toArray(AABB[]::new);
            planes[x] = Lists.newArrayList(compressedPlane);
        }

        // Planes should still be next to eachother, so we can compress them into a single list.
        var out = new ArrayList<AABB>();
        var compressedPlanes = this.compressPlane(planes).toArray(AABB[]::new);
        out.addAll(Lists.newArrayList(compressedPlanes));

        ArchitectureLog.debug("Compressed {} voxels into {} ({}% reduction)", this.occupiedVoxels(), out.size(), 100D - Math.round(((double) out.size() / (double) this.occupiedVoxels()) * 10000D) / 100D);
        return out.stream();
    }

    private Stream<AABB> compressPlane(List<AABB>[] plane) {
        // First dimension represents different offsets along the plane, second dimension represents the line segments along that offset.
        var out = new ArrayList<AABB>();
        var currentStack = new ArrayList<AABB>();
        for (int planeOffset = 0; planeOffset < plane.length - 1; planeOffset++) {
            var currentLines = plane[planeOffset];
            if (currentLines.isEmpty()) {
                continue;
            }

            var nextLines = plane[planeOffset + 1];
            if (nextLines.isEmpty()) {
                // Add all the lines that are still present in the currentLines list to the output, we aren't going to be able to compress them any further.
                out.addAll(currentLines);
                continue;
            }

            for (int j = 0; j < currentLines.size(); j++) {
                var currentLine = currentLines.get(j);
                for (int k = 0; k < nextLines.size(); k++) {
                    var nextLine = nextLines.get(k);
                    if (currentLine == null || nextLine == null) {
                        continue;
                    }
                    if (currentLine.sharesFace(nextLine)) {
                        // Remove the current line from currentLines list then replace the next line with the union of the two.
                        currentLines.set(j, null);
                        nextLines.set(k, currentLine.union(nextLine));
                    }
                }
            }

            // Add all the lines that are still present in the currentLines list to the output.
            out.addAll(currentLines.stream().filter(Objects::nonNull).toList());
        }

        return out.stream();
    }

    private Stream<AABB> compressLine(AABB[] row) {
        // Iterate over the row and compress neighbouring voxels if they share a face, if they don't then add our current voxel to the output and start a new stack.
        var out = new ArrayList<AABB>();
        var currentStack = new ArrayList<AABB>();
        for (int i = 0; i < row.length; i++) {
            var currentVoxel = row[i];
            if (currentVoxel == null) {
                continue;
            }

            if (currentStack.isEmpty()) {
                currentStack.add(currentVoxel);
            } else {
                var lastVoxel = currentStack.get(currentStack.size() - 1);
                if (lastVoxel.isAdjacent(currentVoxel)) {
                    currentStack.add(currentVoxel);
                } else {
                    out.add(currentStack.stream().reduce(AABB::union).get());
                    currentStack.clear();
                    currentStack.add(currentVoxel);
                }
            }
        }

        if (!currentStack.isEmpty()) {
            out.add(currentStack.stream().reduce(AABB::union).get());
        }

        return out.stream();
    }

    private int totalVoxels() {
        return this.voxels.length * this.voxels[0].length * this.voxels[0][0].length;
    }

    private int occupiedVoxels() {
        int count = 0;
        for (var x = 0; x < this.voxels.length; x++) {
            for (var y = 0; y < this.voxels[x].length; y++) {
                for (var z = 0; z < this.voxels[x][y].length; z++) {
                    if (this.voxels[x][y][z]) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public IMesh<?, ? extends IPolygonData<?>> getMesh() {
        return this.mesh;
    }

    public int getBlockResolution() {
        return this.blockResolution;
    }

    public double getResolution() {
        return this.resolution;
    }

    public IVector3i getMin() {
        return this.min;
    }

    public IVector3i getMax() {
        return this.max;
    }
}
