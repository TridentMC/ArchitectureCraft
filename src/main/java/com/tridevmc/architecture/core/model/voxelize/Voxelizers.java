package com.tridevmc.architecture.core.model.voxelize;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.physics.AABB;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Voxelizers {

    private static final Map<String, IVoxelizer> VOXELIZERS = new HashMap<>();
    private static final int DEFAULT_BLOCK_RESOLUTION = 16;
    private static final boolean IS_CACHE_ENABLED = true;

    @Nullable
    private static VoxelizerRecord findVoxelizerRecordForMesh(IMesh<?, ?> mesh) {
        // Check if a file exists for the VoxelizerRecord to be loaded from disk.
        var filePath = Path.of("voxelizers", mesh.getName() + ".json");
        if (IS_CACHE_ENABLED && filePath.toFile().exists()) {
            // Load the VoxelizerRecord from the file.
            return VoxelizerRecord.fromFile(filePath);
        }
        return null;
    }

    private static void saveVoxelizerRecordOnCompletion(IVoxelizer voxelizer) {
        CompletableFuture<ImmutableList<AABB>> voxelize = voxelizer.voxelize();
        // Save the VoxelizerRecord to disk on completion, without blocking this thread.
        voxelize.thenAccept(voxels -> {
            var voxelizerRecord = VoxelizerRecord.fromVoxelizer(voxelizer);
            var filePath = Path.of("voxelizers", voxelizer.getMesh().getName() + ".json");
            voxelizerRecord.saveToFile(filePath);
        });
    }

    public static IVoxelizer of(IMesh<?, ?> mesh, int blockResolution) {
        var voxelizerRecord = findVoxelizerRecordForMesh(mesh);
        if (voxelizerRecord != null) {
            return VOXELIZERS.computeIfAbsent(mesh.getName(), n -> voxelizerRecord.asVoxelizerForMesh(mesh));
        } else {
            var voxelizer = new Voxelizer(mesh, blockResolution);
            saveVoxelizerRecordOnCompletion(voxelizer);
            return VOXELIZERS.computeIfAbsent(mesh.getName(), n -> voxelizer);
        }
    }

    public static IVoxelizer of(IMesh<?, ?> mesh) {
        return of(mesh, DEFAULT_BLOCK_RESOLUTION);
    }

}
