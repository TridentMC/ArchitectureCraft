package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.EnumFlip;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFlip;
import com.tridevmc.architecture.core.math.IMatrix4;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.IMatrix4Mutable;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShapeTransformationResolverFacingWithFlip implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverFacingWithFlip INSTANCE = new ShapeTransformationResolverFacingWithFlip();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var flip = orientation.getValue(ShapeOrientationPropertyFlip.INSTANCE);
        var facingValue = orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE);
        var facing = facingValue != null ? facingValue.value() : Direction.NORTH;

        IMatrix4Mutable matrix;
        if (Objects.requireNonNull(flip).value() == EnumFlip.FLIPPED) {
            // Shape is facing negative X, so we need to flip it while keeping it on the same side of the block.
            matrix = IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 180).asMutable().translate(-0.5, 0, 0);
        } else {
            matrix = IMatrix4.ofMutable(IMatrix4.IDENTITY);
        }

        return switch (facing) {
            case UP ->
                    ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 90).asMutable().mul(matrix));
            case DOWN ->
                    ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, -90).asMutable().mul(matrix));
            case NORTH ->
                    ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 90, 0).asMutable().mul(matrix));
            case SOUTH ->
                    ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, -90, 0).asMutable().mul(matrix));
            case WEST ->
                    ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 180, 0).asMutable().mul(matrix));
            case EAST -> ITrans3.ofImmutable(matrix);
        };
    }
}
