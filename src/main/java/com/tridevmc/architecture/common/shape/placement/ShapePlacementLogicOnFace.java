package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ShapePlacementLogicOnFace implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicOnFace ALONG_LOOK = new ShapePlacementLogicOnFace();

    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFacing.INSTANCE, ShapeOrientationPropertySpin.INSTANCE
    );

    private static final double EPSILON = 1E-6;

    private ShapePlacementLogicOnFace() {
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture block, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
        var normal = hit.getDirection();
        return along(normal, tangent(normal, player));
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


}
