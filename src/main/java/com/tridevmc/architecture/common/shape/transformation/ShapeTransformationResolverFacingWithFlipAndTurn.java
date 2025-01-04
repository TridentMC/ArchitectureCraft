package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.*;
import com.tridevmc.architecture.core.math.IMatrix4;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.IMatrix4Mutable;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.tridevmc.architecture.common.shape.orientation.EnumTurn.TURNED;
import static com.tridevmc.architecture.common.shape.orientation.EnumTurn.UNTURNED;

public class ShapeTransformationResolverFacingWithFlipAndTurn implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverFacingWithFlipAndTurn INSTANCE = new ShapeTransformationResolverFacingWithFlipAndTurn();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var flip = orientation.getValue(ShapeOrientationPropertyFlip.INSTANCE);
        var facingValue = orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE);
        var turn = orientation.getValue(ShapeOrientationPropertyTurn.INSTANCE);
        var facing = facingValue != null ? facingValue.value() : Direction.NORTH;

        IMatrix4Mutable matrix;
        if (Objects.requireNonNull(turn).value() == TURNED) {
            matrix = IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0).asMutable();
        } else {
            matrix = IMatrix4.ofMutable(IMatrix4.IDENTITY);
        }

        if (Objects.requireNonNull(flip).value() == EnumFlip.FLIPPED) {
            // Shape is facing negative X, so we need to flip it while keeping it on the same side of the block.
            matrix = IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 180).asMutable().translate(-0.5, 0, 0).mul(matrix);
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
