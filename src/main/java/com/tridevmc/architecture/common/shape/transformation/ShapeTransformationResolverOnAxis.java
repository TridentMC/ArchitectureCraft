package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link IShapeTransformationResolver} that creates a transformation matrix for an orientation
 * with an axis property. Assumes that the model is on the Y axis by default.
 */
public class ShapeTransformationResolverOnAxis implements IShapeTransformationResolver {
    public static final ShapeTransformationResolverOnAxis INSTANCE = new ShapeTransformationResolverOnAxis();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var axis = orientation.getValue(ShapeOrientationPropertyAxis.INSTANCE).value();
        return switch (axis) {
            case X -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, -90));
            case Y -> ITrans3.ofIdentity();
            case Z -> ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 90, 0, 0));
        };
    }
}
