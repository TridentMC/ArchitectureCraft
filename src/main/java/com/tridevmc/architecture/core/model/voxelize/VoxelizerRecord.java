package com.tridevmc.architecture.core.model.voxelize;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.physics.AABB;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public record VoxelizerRecord(ImmutableList<AABB> simplifiedVoxels, int blockResolution, double resolution,
                              IVector3i min,
                              IVector3i max,
                              boolean[][][] voxels) {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(VoxelizerRecord.class, new VoxelizerRecordAdapter())
            .create();

    public static class VoxelizerRecordAdapter implements JsonSerializer<VoxelizerRecord>, JsonDeserializer<VoxelizerRecord> {

        @Override
        public VoxelizerRecord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var obj = json.getAsJsonObject();

            var minObj = obj.getAsJsonObject("min");
            var maxObj = obj.getAsJsonObject("max");
            var min = IVector3i.ofImmutable(minObj.get("x").getAsInt(), minObj.get("y").getAsInt(), minObj.get("z").getAsInt());
            var max = IVector3i.ofImmutable(maxObj.get("x").getAsInt(), maxObj.get("y").getAsInt(), maxObj.get("z").getAsInt());

            var simplifiedVoxelsObj = obj.getAsJsonArray("simplifiedVoxels");
            var simplifiedVoxels = simplifiedVoxelsObj.asList().stream()
                    .map(e -> {
                        var minPoint = e.getAsJsonObject().getAsJsonObject("min");
                        var maxPoint = e.getAsJsonObject().getAsJsonObject("max");
                        return new AABB(minPoint.get("x").getAsDouble(), minPoint.get("y").getAsDouble(), minPoint.get("z").getAsDouble(),
                                maxPoint.get("x").getAsDouble(), maxPoint.get("y").getAsDouble(), maxPoint.get("z").getAsDouble());
                    }).collect(ImmutableList.toImmutableList());

            var blockResolution = obj.get("blockResolution").getAsInt();
            var resolution = obj.get("resolution").getAsDouble();
            var voxels = new boolean[max.x()][max.y()][max.z()];
            var voxelsObj = obj.getAsJsonArray("voxels");
            for (int x = 0; x < voxelsObj.size(); x++) {
                var yArray = voxelsObj.get(x).getAsJsonArray();
                for (int y = 0; y < yArray.size(); y++) {
                    var zArray = yArray.get(y).getAsJsonArray();
                    for (int z = 0; z < zArray.size(); z++) {
                        voxels[x][y][z] = zArray.get(z).getAsBoolean();
                    }
                }
            }

            return new VoxelizerRecord(simplifiedVoxels, blockResolution, resolution, min, max, voxels);
        }

        @Override
        public JsonElement serialize(VoxelizerRecord src, Type typeOfSrc, JsonSerializationContext context) {
            var obj = new JsonObject();
            obj.add("simplifiedVoxels", context.serialize(src.simplifiedVoxels));
            obj.addProperty("blockResolution", src.blockResolution);
            obj.addProperty("resolution", src.resolution);
            obj.add("min", context.serialize(src.min));
            obj.add("max", context.serialize(src.max));
            obj.add("voxels", context.serialize(src.voxels));
            return obj;
        }
    }

    public static VoxelizerRecord fromFile(Path filePath) {
        try (var reader = Files.newBufferedReader(filePath)) {
            ArchitectureLog.info("Loading VoxelizerRecord from file: " + filePath.toAbsolutePath());
            return GSON.fromJson(reader, VoxelizerRecord.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load VoxelizerRecord from file: " + filePath, e);
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

        try (var writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save VoxelizerRecord to file: " + filePath, e);
        }

        ArchitectureLog.info("Saved VoxelizerRecord to file: " + filePath.toAbsolutePath());
    }
}
