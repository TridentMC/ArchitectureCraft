package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.EnumTurn;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyHorizontalFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.IMatrix4Mutable;
import com.tridevmc.architecture.core.math.ITrans3;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;

public final class ShapeTransformationResolverWindow implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverWindow FRAME = new ShapeTransformationResolverWindow(false);
    public static final ShapeTransformationResolverWindow CORNER = new ShapeTransformationResolverWindow(true);
    private final boolean corner;

    private ShapeTransformationResolverWindow(boolean corner) {
        this.corner = corner;
    }

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var property = orientation.getValue(ShapeOrientationPropertyAxis.INSTANCE);
        var axis = property == null ? (this.corner ? Direction.Axis.Y : Direction.Axis.Z) : property.value();
        var matrix = IMatrix4Mutable.ofIdentity();
        if (this.corner) {
            if (axis == Direction.Axis.X) {
                matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 90));
            } else if (axis == Direction.Axis.Z) {
                matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0));
            }
            var facing = orientation.getValue(ShapeOrientationPropertyHorizontalFacing.INSTANCE);
            var degrees = facing == null ? 0 : switch (facing.value()) {
                case EAST -> -90;
                case SOUTH -> 180;
                case WEST -> 90;
                default -> 0;
            };
            matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, degrees, 0));
        } else {
            if (axis == Direction.Axis.X) {
                matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 90, 0));
            } else if (axis == Direction.Axis.Y) {
                matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0));
            }
            var turn = orientation.getValue(ShapeOrientationPropertyTurn.INSTANCE);
            if (turn != null && turn.value() == EnumTurn.TURNED) {
                matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 90));
            }
        }
        return ITrans3.ofImmutable(matrix);
    }
}
