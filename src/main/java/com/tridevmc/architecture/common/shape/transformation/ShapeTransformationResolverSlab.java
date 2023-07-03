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
 * with a facing property. Specifically to be applied to slabs, which are on the bottom half of the block by default.
 */
public class ShapeTransformationResolverSlab implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverSlab INSTANCE = new ShapeTransformationResolverSlab();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        ShapeOrientationProperty.Value<Direction> facingValue = orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE);
        Direction facing = facingValue != null ? facingValue.value() : Direction.NORTH;

        // Slabs are on the bottom half of the block by default. If the block is facing up, we need to move it up by half a block.
        return switch (facing) {
            case UP -> ITrans3.ofTranslationImmutable(0, 0.5, 0);
            case DOWN -> ITrans3.ofIdentity();
            case NORTH -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 90));
            case SOUTH -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, -90));
            case WEST -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0));
            case EAST -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, -90, 0, 0));
        };
    }
}
