package com.tridevmc.architecture.core.model.voxelize;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.physics.AABB;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public record VoxelizerRecord(ImmutableList<AABB> simplifiedVoxels, int blockResolution, double resolution,
                              IVector3i min,
                              IVector3i max,
                              boolean[][][] voxels) {


    private static VoxelizerRecord deserialize(ByteBuf bb) {
        var simplifiedVoxelsSize = bb.readInt();
        var blockResolution = bb.readInt();
        var resolution = bb.readDouble();
        var minX = bb.readInt();
        var minY = bb.readInt();
        var minZ = bb.readInt();
        var maxX = bb.readInt();
        var maxY = bb.readInt();
        var maxZ = bb.readInt();

        var voxelsX = bb.readInt();
        var voxelsY = bb.readInt();
        var voxelsZ = bb.readInt();

        var voxels = new boolean[voxelsX][voxelsY][voxelsZ];

        for (var x = 0; x < voxelsX; x++) {
            for (var y = 0; y < voxelsY; y++) {
                for (var z = 0; z < voxelsZ; z++) {
                    voxels[x][y][z] = bb.readBoolean();
                }
            }
        }

        var simplifiedVoxels = ImmutableList.<AABB>builder();
        for (var i = 0; i < simplifiedVoxelsSize; i++) {
            var minVX = bb.readDouble();
            var minVY = bb.readDouble();
            var minVZ = bb.readDouble();
            var maxVX = bb.readDouble();
            var maxVY = bb.readDouble();
            var maxVZ = bb.readDouble();

            simplifiedVoxels.add(new AABB(minVX, minVY, minVZ, maxVX, maxVY, maxVZ));
        }


        return new VoxelizerRecord(simplifiedVoxels.build(), blockResolution, resolution, IVector3i.ofImmutable(minX, minY, minZ), IVector3i.ofImmutable(maxX, maxY, maxZ), voxels);
    }

    private static void serialize(VoxelizerRecord src, ByteBuf bb) {
        bb.writeInt(src.simplifiedVoxels.size());
        bb.writeInt(src.blockResolution);
        bb.writeDouble(src.resolution);
        bb.writeInt(src.min.x());
        bb.writeInt(src.min.y());
        bb.writeInt(src.min.z());
        bb.writeInt(src.max.x());
        bb.writeInt(src.max.y());
        bb.writeInt(src.max.z());

        bb.writeInt(src.voxels.length);
        bb.writeInt(src.voxels[0].length);
        bb.writeInt(src.voxels[0][0].length);

        for (var x = 0; x < src.voxels.length; x++) {
            for (var y = 0; y < src.voxels[x].length; y++) {
                for (var z = 0; z < src.voxels[x][y].length; z++) {
                    bb.writeBoolean(src.voxels[x][y][z]);
                }
            }
        }

        for (var voxel : src.simplifiedVoxels) {
            bb.writeDouble(voxel.min().x());
            bb.writeDouble(voxel.min().y());
            bb.writeDouble(voxel.min().z());
            bb.writeDouble(voxel.max().x());
            bb.writeDouble(voxel.max().y());
            bb.writeDouble(voxel.max().z());
        }
    }

    public static VoxelizerRecord fromFile(Path filePath) {
        try {
            var bytes = Files.readAllBytes(filePath);
            var bb = Unpooled.wrappedBuffer(bytes);
            var record = deserialize(bb);
            bb.release();
            return record;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read VoxelizerRecord from file: " + filePath, e);
        }
    }

    public IVoxelizer asVoxelizerForMesh(IMesh<?, ? extends IPolygonData<?>> mesh) {
        return new IVoxelizer() {

            @Override
            public ImmutableList<AABB> voxelizeNow() {
                return VoxelizerRecord.this.simplifiedVoxels;
            }

            @Override
            public CompletableFuture<ImmutableList<AABB>> voxelize() {
                return CompletableFuture.completedFuture(VoxelizerRecord.this.simplifiedVoxels);
            }

            @Override
            public IMesh<?, ? extends IPolygonData<?>> getMesh() {
                return mesh;
            }

            @Override
            public int getBlockResolution() {
                return VoxelizerRecord.this.blockResolution;
            }

            @Override
            public double getResolution() {
                return VoxelizerRecord.this.resolution;
            }

            @Override
            public IVector3i getMin() {
                return VoxelizerRecord.this.min;
            }

            @Override
            public IVector3i getMax() {
                return VoxelizerRecord.this.max;
            }
        };
    }

    public static VoxelizerRecord fromVoxelizer(IVoxelizer voxelizer) {
        return new VoxelizerRecord(voxelizer.voxelizeNow(), voxelizer.getBlockResolution(), voxelizer.getResolution(),
                voxelizer.getMin(), voxelizer.getMax(), new boolean[voxelizer.getMax().x()][voxelizer.getMax().y()][voxelizer.getMax().z()]);
    }

    public void saveToFile(Path filePath) {
        // Make the file and parent directories if they don't exist.
        try {
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create file for VoxelizerRecord: " + filePath, e);
        }

        var bb = Unpooled.buffer();
        serialize(this, bb);

        try {
            Files.write(filePath, bb.array());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write VoxelizerRecord to file: " + filePath, e);
        }

        bb.release();
        ArchitectureLog.info("Saved VoxelizerRecord to file: " + filePath.toAbsolutePath());
    }
}
