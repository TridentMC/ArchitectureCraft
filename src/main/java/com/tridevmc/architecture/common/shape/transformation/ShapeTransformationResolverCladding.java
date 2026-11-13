package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.IMatrix4Mutable;
import com.tridevmc.architecture.core.math.ITrans3;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;

public final class ShapeTransformationResolverCladding implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverCladding INSTANCE = new ShapeTransformationResolverCladding();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var property = orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE);
        var direction = property == null ? Direction.NORTH : property.value();
        var matrix = switch (direction.getAxis()) {
            case X -> IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 90, 0).asMutable();
            case Y -> IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0).asMutable();
            case Z -> IMatrix4Mutable.ofIdentity();
        };
        var distance = 15 / 32D;
        matrix.m03(matrix.m03() + direction.getStepX() * distance);
        matrix.m13(matrix.m13() + direction.getStepY() * distance);
        matrix.m23(matrix.m23() + direction.getStepZ() * distance);
        return ITrans3.ofImmutable(matrix);
    }
}
