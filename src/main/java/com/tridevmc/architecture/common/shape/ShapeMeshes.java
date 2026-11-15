package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.shape.orientation.EnumConnectionState;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.placement.ShapeConnectionProperties;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.Mesh;
import com.tridevmc.architecture.core.model.mesh.Part;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.objson.OBJSON;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import com.tridevmc.architecture.core.model.voxelize.Voxelizer;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/**
 * Responsible for getting a mesh for a given shape enum, and a voxelizer for a given shape enum.
 */
public final class ShapeMeshes {

    private static final Map<Identifier, IMesh<String, PolygonData>> ASSETS = new HashMap<>();
    private static final Map<MeshKey, IMesh<String, PolygonData>> MESHES = new HashMap<>();
    private static final Map<String, IVoxelizer> VOXELIZERS = new HashMap<>();

    private record MeshKey(EnumShape shape, int variant) {
    }

    private enum EnumCornerKind {
        DETACHED, HORIZONTAL, VERTICAL, INNER, OUTER, ACROSS;

        String partName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
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

    public static boolean isWindow(EnumShape shape) {
        return switch (shape) {
            case WINDOW_FRAME, WINDOW_CORNER, WINDOW_MULLION -> true;
            default -> false;
        };
    }


    private static int variant(EnumShape shape, ShapeOrientation orientation) {
        var properties = isWindow(shape)
                ? new ShapeOrientationPropertyRelativeConnection[]{ShapeOrientationPropertyRelativeConnection.BOTTOM,
                ShapeOrientationPropertyRelativeConnection.RIGHT, ShapeOrientationPropertyRelativeConnection.TOP,
                ShapeOrientationPropertyRelativeConnection.LEFT, ShapeConnectionProperties.DOWN_LEFT,
                ShapeConnectionProperties.DOWN_RIGHT, ShapeConnectionProperties.UP_LEFT, ShapeConnectionProperties.UP_RIGHT}
                : new ShapeOrientationPropertyRelativeConnection[]{ShapeOrientationPropertyRelativeConnection.FRONT,
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
            case ROOF_TILE, ROOF_OUTER_CORNER, ROOF_INNER_CORNER, ROOF_RIDGE,
                    ROOF_VALLEY, ROOF_SMART_RIDGE, ROOF_SMART_VALLEY -> true;
            default -> false;
        }) {
            return ShapeGeometry.roof(shape, variant);
        }
        if (isWindow(shape)) {
            return window(shape, variant);
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

    private static IMesh<String, PolygonData> window(EnumShape shape, int connections) {
        var corner = shape == EnumShape.WINDOW_CORNER;
        var source = asset(corner ? EnumShape.WINDOW_CORNER : EnumShape.WINDOW_FRAME);
        var builder = new Mesh.Builder<String, PolygonData>(shape.getName() + "_" + connections);
        var sides = new Direction[]{Direction.DOWN, corner ? Direction.SOUTH : Direction.EAST, Direction.UP, Direction.WEST};
        addIfPresent(builder, source, "glass");
        for (int i = 0; i < sides.length; i++) {
            if ((connections & (1 << i)) == 0) {
                addIfPresent(builder, source, "edge." + sides[i].getSerializedName());
                addIfPresent(builder, source, "glass." + sides[i].getSerializedName());
            }
        }
        for (int i = 0; i < 4; i++) {
            var vertical = i < 2 ? 0 : 2;
            var horizontal = (i & 1) == 0 ? 3 : 1;
            var v = (connections & (1 << vertical)) != 0;
            var h = (connections & (1 << horizontal)) != 0;
            var diagonal = (connections & (1 << (i + 4))) != 0;
            if (v && h && diagonal) {
                continue;
            }
            var kind = corner ? (v ? EnumCornerKind.DETACHED : h ? EnumCornerKind.ACROSS : EnumCornerKind.OUTER)
                    : v && h ? EnumCornerKind.INNER : v ? EnumCornerKind.VERTICAL : h ? EnumCornerKind.HORIZONTAL : EnumCornerKind.DETACHED;
            addIfPresent(builder, source, "corner." + kind.partName() + "." + sides[vertical].getSerializedName()
                    + "_" + sides[horizontal].getSerializedName());
        }
        if (corner) {
            addIfPresent(builder, source, "post");
            addIfPresent(builder, source, (connections & 1) != 0 ? "post.connected.down" : "post.down");
            addIfPresent(builder, source, (connections & 4) != 0 ? "post.connected.up" : "post.up");
        }
        if (shape == EnumShape.WINDOW_MULLION) {
            var mullion = new Part.Builder<String, PolygonData>().setId("mullion");
            ShapeGeometry.box(mullion, 7 / 16D, 0, 13 / 32D, 9 / 16D, 1, 19 / 32D, 0);
            builder.addPart(mullion.build());
        }
        return builder.build();
    }

    private static void addIfPresent(Mesh.Builder<String, PolygonData> builder,
            IMesh<String, PolygonData> source, String name) {
        var part = source.getParts().get(name);
        if (part != null) {
            builder.addPart(part);
        }
    }
}
