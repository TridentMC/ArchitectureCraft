package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.IMatrix4Mutable;
import com.tridevmc.architecture.core.math.ITrans3;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;

public final class ShapeTransformationResolverWindow implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverWindow FRAME = new ShapeTransformationResolverWindow();

    private ShapeTransformationResolverWindow() {
    }

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var property = orientation.getValue(ShapeOrientationPropertyAxis.INSTANCE);
        var axis = property == null ? Direction.Axis.Z : property.value();
        var matrix = IMatrix4Mutable.ofIdentity();
        if (axis == Direction.Axis.X) {
            matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 90, 0));
        } else if (axis == Direction.Axis.Y) {
            matrix.mul(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0));
        }



        return ITrans3.ofImmutable(matrix);
    }
}
