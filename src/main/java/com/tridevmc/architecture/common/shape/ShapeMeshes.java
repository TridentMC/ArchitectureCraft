package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.model.Voxelizer;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.objson.OBJSON;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for getting a mesh for a given shape enum, and a voxelizer for a given shape enum.
 */
public class ShapeMeshes {

    // TODO: There are likely special cases here we need to error for.

    private static final Map<EnumShape, IMesh<String, PolygonData>> MESHES = new HashMap<>();
    private static final Map<EnumShape, Voxelizer> VOXELIZERS = new HashMap<>();

    static {
        Arrays.stream(EnumShape.values()).forEach(
                enumShape -> {
                    try {
                        var objson = OBJSON.fromResource(enumShape.getAssetLocation());
                        var mesh = objson.mesh();
                        var voxelizer = objson.voxelizer();
                        register(enumShape, mesh, voxelizer);
                    } catch (Exception e) {
                        ArchitectureLog.error("Failed to load mesh for shape: " + enumShape.getAssetLocation(), e);
                    }
                }
        );
    }

    private static void register(EnumShape enumShape, IMesh<String, PolygonData> mesh, Voxelizer voxelizer) {
        MESHES.put(enumShape, mesh);
        VOXELIZERS.put(enumShape, voxelizer);
    }

    public static IMesh<String, PolygonData> getMesh(EnumShape enumShape) {
        return MESHES.get(enumShape);
    }

    public static Voxelizer getVoxelizer(EnumShape enumShape) {
        return VOXELIZERS.get(enumShape);
    }

}
