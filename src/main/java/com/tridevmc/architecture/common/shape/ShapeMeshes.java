package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.shape.orientation.EnumConnectionState;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.objson.OBJSON;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import com.tridevmc.architecture.core.model.voxelize.Voxelizer;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for getting a mesh for a given shape enum, and a voxelizer for a given shape enum.
 */
public final class ShapeMeshes {

    private static final Map<Identifier, IMesh<String, PolygonData>> ASSETS = new HashMap<>();
    private static final Map<MeshKey, IMesh<String, PolygonData>> MESHES = new HashMap<>();
    private static final Map<String, IVoxelizer> VOXELIZERS = new HashMap<>();

    private record MeshKey(EnumShape shape, int variant) {
    }


    private ShapeMeshes() {
    }

    public static synchronized IMesh<String, PolygonData> getMesh(EnumShape shape) {
        return getMesh(shape, ShapeOrientation.IDENTITY);
    }

    public static synchronized IMesh<String, PolygonData> getMesh(EnumShape shape, ShapeOrientation orientation) {
        var variant = variant(shape, orientation);
        return MESHES.computeIfAbsent(new MeshKey(shape, variant), key -> createMesh(key.shape(), key.variant()));
    }

    public static synchronized IVoxelizer getVoxelizer(EnumShape shape) {
        var mesh = getMesh(shape);
        return mesh == null ? null : getVoxelizer(mesh);
    }

    public static synchronized IVoxelizer getVoxelizer(IMesh<String, PolygonData> mesh) {
        return VOXELIZERS.computeIfAbsent(mesh.getName(), key -> new Voxelizer(mesh, 16));
    }


    private static int variant(EnumShape shape, ShapeOrientation orientation) {
        var properties = new ShapeOrientationPropertyRelativeConnection[]{ShapeOrientationPropertyRelativeConnection.FRONT,
                ShapeOrientationPropertyRelativeConnection.BACK, ShapeOrientationPropertyRelativeConnection.LEFT,
                ShapeOrientationPropertyRelativeConnection.RIGHT};
        var mask = 0;
        for (int i = 0; i < properties.length; i++) {
            var value = orientation.getValue(properties[i]);
            if (value != null && value.value() == EnumConnectionState.CONNECTED) {
                mask |= 1 << i;
            }
        }
        return mask;
    }

    private static IMesh<String, PolygonData> createMesh(EnumShape shape, int variant) {
        if (switch (shape) {
            case ROOF_TILE, ROOF_OUTER_CORNER, ROOF_INNER_CORNER, ROOF_RIDGE -> true;
            default -> false;
        }) {
            return ShapeGeometry.roof(shape, variant);
        }
        return asset(shape);
    }

    private static IMesh<String, PolygonData> asset(EnumShape shape) {
        var location = shape.getAssetLocation();
        return ASSETS.computeIfAbsent(location, key -> {
            try {
                return OBJSON.fromResource(key).mesh();
            } catch (Exception e) {
                ArchitectureLog.error("Failed to load mesh for shape: " + key, e);
                return null;
            }
        });
    }


}
