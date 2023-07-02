package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of {@link IShapeTransformationResolver} that always returns the identity transformation.
 * <p>
 * Useful for shapes that wouldn't be impacted by rotation like a sphere.
 */
public class ShapeOrientationResolverIdentity implements IShapeTransformationResolver {
    @Override
    public @NotNull ITrans3 resolve(@NotNull ShapeOrientation orientation) {
        return ITrans3.ofIdentity();
    }
}
