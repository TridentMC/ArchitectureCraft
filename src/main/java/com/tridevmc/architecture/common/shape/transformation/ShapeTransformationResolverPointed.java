package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link IShapeTransformationResolver} that creates a transformation matrix for an orientation
 * with a facing property.
 */
public class ShapeTransformationResolverPointed implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverPointed INSTANCE = new ShapeTransformationResolverPointed();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        // Assumes the object is facing WEST, so rotate accordingly
        ShapeOrientationProperty.Value<Direction> facingValue = orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE);
        Direction facing = facingValue != null ? facingValue.value() : Direction.NORTH;

        return switch (facing) {
            case UP -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 90));
            case DOWN -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, -90));
            case NORTH -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, -90, 0));
            case SOUTH -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 90, 0));
            case WEST -> ITrans3.ofIdentity();
            case EAST -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 180, 0));
        };
    }
}
