package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.shape.orientation.EnumConnectionState;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.placement.ShapeConnectionProperties;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.Mesh;
import com.tridevmc.architecture.core.model.mesh.Part;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.objson.OBJSON;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import com.tridevmc.architecture.core.model.voxelize.Voxelizer;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.StairsShape;

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

    private enum CornerKind {
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
        return getVoxelizer(getMesh(shape));
    }

    public static synchronized IVoxelizer getVoxelizer(IMesh<String, PolygonData> mesh) {
        return VOXELIZERS.computeIfAbsent(mesh.getName(), key -> new Voxelizer(mesh, 16));
    }

    public static boolean isWindow(EnumShape shape) {
        return switch (shape) {
            case WINDOW_FRAME, WINDOW_CORNER, WINDOW_MULLION, WINDOW_SMART, WINDOW_MULLION_SMART -> true;
            default -> false;
        };
    }

    public static boolean isRoof(EnumShape shape) {
        return switch (shape) {
            case ROOF_TILE, ROOF_OUTER_CORNER, ROOF_INNER_CORNER, ROOF_RIDGE, ROOF_SMART_RIDGE,
                    ROOF_VALLEY, ROOF_SMART_VALLEY, ROOF_OVERHANG, ROOF_OVERHANG_OUTER_CORNER,
                    ROOF_OVERHANG_INNER_CORNER, ROOF_OVERHANG_GABLE_LH, ROOF_OVERHANG_GABLE_RH,
                    ROOF_OVERHANG_GABLE_END_LH, ROOF_OVERHANG_GABLE_END_RH, ROOF_OVERHANG_RIDGE,
                    ROOF_OVERHANG_VALLEY -> true;
            default -> false;
        };
    }

    private static int variant(EnumShape shape, ShapeOrientation orientation) {
        if (shape == EnumShape.STAIRS_SMART) {
            var variant = orientation.getValue(ShapeConnectionProperties.STAIRS);
            return variant == null ? StairsShape.STRAIGHT.ordinal() : variant.value().ordinal();
        }
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
            case ROOF_TILE, ROOF_OUTER_CORNER, ROOF_INNER_CORNER, ROOF_RIDGE, ROOF_VALLEY,
                    ROOF_SMART_RIDGE, ROOF_SMART_VALLEY -> true;
            default -> false;
        }) {
            return ShapeGeometry.roof(shape, variant);
        }
        if (shape == EnumShape.STAIRS_SMART) {
            var stair = StairsShape.values()[variant];
            var model = switch (stair) {
                case STRAIGHT -> asset(EnumShape.STAIRS);
                case INNER_LEFT, INNER_RIGHT -> asset(EnumShape.STAIRS_INNER_CORNER);
                case OUTER_LEFT, OUTER_RIGHT -> asset(EnumShape.STAIRS_OUTER_CORNER);
            };
            if (stair == StairsShape.INNER_LEFT || stair == StairsShape.OUTER_LEFT) {
                model = model.transform(ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 90, 0)));
            }
            var builder = new Mesh.Builder<String, PolygonData>(shape.getName() + "_" + variant);
            model.getParts().values().forEach(builder::addPart);
            return builder.build();
        }
        if (isWindow(shape)) {
            return window(shape, variant);
        }
        return asset(shape);
    }

    private static IMesh<String, PolygonData> asset(EnumShape shape) {
        var location = shape == EnumShape.CLADDING_SHEET
                ? Identifier.fromNamespaceAndPath(shape.getId().getNamespace(), "shape/cladding.objson")
                : shape.getAssetLocation();
        return ASSETS.computeIfAbsent(location, key -> OBJSON.fromResource(key).mesh());
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
            var kind = corner ? (v ? CornerKind.DETACHED : h ? CornerKind.ACROSS : CornerKind.OUTER)
                    : v && h ? CornerKind.INNER : v ? CornerKind.VERTICAL : h ? CornerKind.HORIZONTAL : CornerKind.DETACHED;
            addIfPresent(builder, source, "corner." + kind.partName() + "." + sides[vertical].getSerializedName()
                    + "_" + sides[horizontal].getSerializedName());
        }
        if (corner) {
            addIfPresent(builder, source, "post");
            addIfPresent(builder, source, (connections & 1) != 0 ? "post.connected.down" : "post.down");
            addIfPresent(builder, source, (connections & 4) != 0 ? "post.connected.up" : "post.up");
        }
        if (shape == EnumShape.WINDOW_MULLION || shape == EnumShape.WINDOW_MULLION_SMART) {
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
