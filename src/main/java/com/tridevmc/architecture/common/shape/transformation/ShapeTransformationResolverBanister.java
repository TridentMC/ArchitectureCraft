package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.core.math.ITrans3;

import org.jetbrains.annotations.NotNull;

public final class ShapeTransformationResolverBanister implements IShapeTransformationResolver {

    public static final ShapeTransformationResolverBanister INSTANCE = new ShapeTransformationResolverBanister();

    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        var offset = orientation.getValue(ShapeOrientationPropertyOffset.INSTANCE);
        var distance = offset == null ? 0 : offset.value().getOffset() * 0.75;
        return ITrans3.ofImmutable(ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation)
                .matrix().asMutable().translate(distance, 0, 0));
    }
}
