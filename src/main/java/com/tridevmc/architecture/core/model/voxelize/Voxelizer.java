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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Performs a series of collision tests on a given mesh to create a voxelized representation of it.
 */
public class Voxelizer implements IVoxelizer {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2,
            Thread.ofPlatform().daemon().name("architecturecraft-voxelizer-", 0).factory());
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

        var minX = (int) Math.floor(minXBounds / this.resolution);
        var minY = (int) Math.floor(minYBounds / this.resolution);
        var minZ = (int) Math.floor(minZBounds / this.resolution);
        var maxX = Math.max(minX, (int) Math.ceil(maxXBounds / this.resolution) - 1);
        var maxY = Math.max(minY, (int) Math.ceil(maxYBounds / this.resolution) - 1);
        var maxZ = Math.max(minZ, (int) Math.ceil(maxZBounds / this.resolution) - 1);

        this.min = IVector3i.ofImmutable(minX, minY, minZ);
        this.max = IVector3i.ofImmutable(maxX, maxY, maxZ);

        this.voxels = new boolean[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
    }

    @Override
    public ImmutableList<AABB> voxelizeNow() {
        return this.voxelize().join();
    }

    @Override
    public synchronized CompletableFuture<ImmutableList<AABB>> voxelize() {
        if (this.simplifiedVoxelsFuture == null) {
            // One job per mesh avoids thousands of queued voxel tasks and workers waiting for other workers.
            this.simplifiedVoxelsFuture = CompletableFuture.supplyAsync(() -> {
                var grid = new AABB[this.voxels.length][this.voxels[0].length][this.voxels[0][0].length];
                for (int x = 0; x < grid.length; x++) {
                    for (int y = 0; y < grid[x].length; y++) {
                        for (int z = 0; z < grid[x][y].length; z++) {
                            var box = this.getBoxForOffset(x + this.min.x(), y + this.min.y(), z + this.min.z());
                            if (this.isBoxValidVoxel(box)) {
                                var bounds = this.mesh.getBounds();
                                grid[x][y][z] = new AABB(
                                        Math.max(box.minX(), bounds.minX()), Math.max(box.minY(), bounds.minY()),
                                        Math.max(box.minZ(), bounds.minZ()), Math.min(box.maxX(), bounds.maxX()),
                                        Math.min(box.maxY(), bounds.maxY()), Math.min(box.maxZ(), bounds.maxZ()));
                                this.voxels[x][y][z] = true;
                            }
                        }
                    }
                }
                return this.compressVoxels(grid).collect(ImmutableList.toImmutableList());
            }, POOL);
        }
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
                xPlane[y] = Lists.newArrayList(compressedLine);
            }

            var compressedPlane = this.compressPlane(xPlane).toArray(AABB[]::new);
            planes[x] = Lists.newArrayList(compressedPlane);
        }

        var out = new ArrayList<AABB>();
        var compressedPlanes = this.compressPlane(planes).toArray(AABB[]::new);
        out.addAll(Lists.newArrayList(compressedPlanes));

        ArchitectureLog.debug("Compressed {} voxels into {} ({}% reduction)",
                this.occupiedVoxels(), out.size(),
                100D - Math.round(((double) out.size() / (double) this.occupiedVoxels()) * 10000D) / 100D);
        return out.stream();
    }

    private Stream<AABB> compressPlane(List<AABB>[] plane) {
        var out = new ArrayList<AABB>();

        // Handle empty plane case
        if (plane.length == 0) {
            return Stream.empty();
        }

        // Process all lines/planes
        for (int planeOffset = 0; planeOffset < plane.length; planeOffset++) {
            var currentLines = plane[planeOffset];
            if (currentLines == null || currentLines.isEmpty()) {
                continue;
            }

            // If this is the last plane, add all remaining lines and break
            if (planeOffset == plane.length - 1) {
                out.addAll(currentLines);
                break;
            }

            var nextLines = plane[planeOffset + 1];
            if (nextLines == null || nextLines.isEmpty()) {
                out.addAll(currentLines);
                continue;
            }

            // Try to merge current lines with next lines
            boolean[] mergedCurrentLines = new boolean[currentLines.size()];
            boolean[] mergedNextLines = new boolean[nextLines.size()];

            for (int j = 0; j < currentLines.size(); j++) {
                var currentLine = currentLines.get(j);
                if (currentLine == null) continue;

                for (int k = 0; k < nextLines.size(); k++) {
                    var nextLine = nextLines.get(k);
                    if (nextLine == null || mergedNextLines[k]) continue;

                    if (currentLine.sharesFace(nextLine)) {
                        nextLines.set(k, currentLine.union(nextLine));
                        mergedCurrentLines[j] = true;
                        mergedNextLines[k] = true;
                        break;
                    }
                }
            }

            // Add unmerged current lines to output
            for (int j = 0; j < currentLines.size(); j++) {
                if (!mergedCurrentLines[j] && currentLines.get(j) != null) {
                    out.add(currentLines.get(j));
                }
            }
        }

        return out.stream();
    }

    private Stream<AABB> compressLine(AABB[] row) {
        var out = new ArrayList<AABB>();
        if (row == null || row.length == 0) {
            return Stream.empty();
        }

        AABB current = null;

        for (AABB voxel : row) {
            if (voxel == null) {
                if (current != null) {
                    out.add(current);
                    current = null;
                }
                continue;
            }

            if (current == null) {
                current = voxel;
            } else if (current.sharesFace(voxel)) {
                current = current.union(voxel);
            } else {
                out.add(current);
                current = voxel;
            }
        }

        // Don't forget the last voxel/group
        if (current != null) {
            out.add(current);
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
