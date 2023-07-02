package com.tridevmc.architecture.common.shape.transformation;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

/**
 * Used for resolving a set of ShapeOrientationProperties into a Trans3 that can be used to transform a shape for collision or rendering.
 * <p>
 * The results of this resolver are not cached, and ideally the results should be stored on the state that the shape is being rendered for.
 */
@FunctionalInterface
public interface IShapeTransformationResolver {

    /**
     * Resolves the given ShapeOrientation into a Trans3 that can be used to transform a shape for collision or rendering.
     *
     * @param orientation the orientation to resolve.
     * @return the resolved transformation.
     */
    @NotNull
    ITrans3 resolve(@NotNull ShapeOrientation orientation);


}
