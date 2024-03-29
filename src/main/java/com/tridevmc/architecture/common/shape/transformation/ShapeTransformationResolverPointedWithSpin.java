package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link IShapeTransformationResolver} that creates a transformation matrix for an orientation
 * with both a facing and a spin property.
 */
public class ShapeTransformationResolverPointedWithSpin implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverPointedWithSpin INSTANCE = new ShapeTransformationResolverPointedWithSpin();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        // Assumes the object is facing NORTH, so rotate accordingly
        var facingValue = orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE);
        var facing = facingValue != null ? facingValue.value() : Direction.NORTH;
        var spinValue = orientation.getValue(ShapeOrientationPropertySpin.INSTANCE);
        var spin = spinValue != null ? spinValue.value() : EnumSpin.NONE;

        // Because the translation rotation function applies in the XYZ order we can just pass everything in at once.
        return switch (facing) {
            case UP -> ITrans3.ofImmutable(
                    IMatrix4Immutable.ofRotationXYZ(
                            0.5, 0.5, 0.5, spin.getDegrees(), 0, -90
                    )
            );
            case DOWN -> ITrans3.ofImmutable(
                    IMatrix4Immutable.ofRotationXYZ(
                            0.5, 0.5, 0.5, spin.getDegrees(), 0, 90
                    )
            );
            case NORTH -> ITrans3.ofImmutable(
                    IMatrix4Immutable.ofRotationXYZ(
                            0.5, 0.5, 0.5, spin.getDegrees(), 0, 0
                    )
            );
            case SOUTH -> ITrans3.ofImmutable(
                    IMatrix4Immutable.ofRotationXYZ(
                            0.5, 0.5, 0.5, spin.getDegrees(), 180, 0
                    )
            );
            case WEST -> ITrans3.ofImmutable(
                    IMatrix4Immutable.ofRotationXYZ(
                            0.5, 0.5, 0.5, spin.getDegrees(), 90, 0
                    )
            );
            case EAST -> ITrans3.ofImmutable(
                    IMatrix4Immutable.ofRotationXYZ(
                            0.5, 0.5, 0.5, spin.getDegrees(), -90, 0
                    )
            );
        };
    }
}
