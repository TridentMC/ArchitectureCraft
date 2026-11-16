package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointed;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;
import com.tridevmc.architecture.core.math.ITrans3;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ShapePlacementLogicOnFace implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicOnFace CORNER = new ShapePlacementLogicOnFace(Mode.CORNER);
    public static final ShapePlacementLogicOnFace ALONG_LOOK = new ShapePlacementLogicOnFace(Mode.ALONG_LOOK);

    private final Mode mode;
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFacing.INSTANCE, ShapeOrientationPropertySpin.INSTANCE
    );

    private static final double EPSILON = 1E-6;

    private ShapePlacementLogicOnFace(Mode mode) {
        this.mode = mode;
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture block, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
        var normal = hit.getDirection();
        return switch (this.mode) {
            case CORNER -> corner(normal, faceHit(normal, pos, hit));
            case ALONG_LOOK -> along(normal, tangent(normal, player));
        };
    }

    private static FaceHit faceHit(Direction normal, BlockPos pos, BlockHitResult hit) {
        var basis = ShapeTransformationResolverPointed.INSTANCE.resolve(
                ShapeOrientation.forProperties(ShapeOrientationPropertyFacing.of(normal)));
        var matrix = basis.matrix();
        var relative = hit.getLocation().subtract(Vec3.atCenterOf(pos));
        // The basis is rigid; its transposed rotation projects the hit onto local X/Z.
        double x = matrix.m00() * relative.x + matrix.m10() * relative.y
                + matrix.m20() * relative.z;
        double z = matrix.m02() * relative.x + matrix.m12() * relative.y
                + matrix.m22() * relative.z;
        return new FaceHit(basis, x, z);
    }

    private static ShapeOrientation corner(Direction normal, FaceHit hit) {
        Direction forward;
        if (hit.x() > EPSILON) {
            forward = hit.z() < -EPSILON ? Direction.NORTH : Direction.EAST;
        } else {
            forward = hit.z() < -EPSILON ? Direction.WEST : Direction.SOUTH;
        }
        return along(normal, hit.basis().transformDirection(forward));
    }

    static ShapeOrientation along(Direction up, Direction forward) {
        return encode(cross(forward, up), up);
    }

    private static Direction tangent(Direction normal, Player player) {
        if (normal.getAxis().isVertical()) {
            return player.getDirection();
        }
        var axis = normal.getUnitVec3();
        var look = player.getLookAngle();
        var projected = look.subtract(axis.scale(look.dot(axis)));
        if (projected.lengthSqr() < EPSILON * EPSILON) {
            return Direction.UP;
        }
        var nearest = Direction.UP;
        double best = Double.NEGATIVE_INFINITY;
        for (var direction : Direction.values()) {
            if (direction.getAxis() == normal.getAxis()) {
                continue;
            }
            double alignment = projected.dot(direction.getUnitVec3());
            if (alignment > best + EPSILON) {
                nearest = direction;
                best = alignment;
            }
        }
        return nearest;
    }

    private static Direction cross(Direction a, Direction b) {
        if (a.getAxis() == b.getAxis()) {
            throw new IllegalArgumentException("Frame axes must be orthogonal: " + a + ", " + b);
        }
        return Direction.getApproximateNearest(a.getUnitVec3().cross(b.getUnitVec3()));
    }

    private static ShapeOrientation encode(Direction west, Direction up) {
        if (west.getAxis() == up.getAxis()) {
            throw new IllegalArgumentException("Frame axes must be orthogonal: " + west + ", " + up);
        }
        for (var spin : EnumSpin.getValues()) {
            var orientation = ShapeOrientation.forProperties(
                    ShapeOrientationPropertyFacing.of(west), ShapeOrientationPropertySpin.of(spin));
            if (ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation)
                    .transformDirection(Direction.UP) == up) {
                return orientation;
            }
        }
        throw new IllegalArgumentException("No orientation for frame: " + west + ", " + up);
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }

    private enum Mode {
        CORNER, ALONG_LOOK
    }

    private record FaceHit(ITrans3 basis, double x, double z) {
    }
}
