package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines an object that stores a set of properties used to orient a shape in the world, should be heavily cached to avoid heap bloat.
 */
public record ShapeOrientation(
        ShapeOrientationProperty.Value<?>... properties
) {

    public static final ShapeOrientation IDENTITY = new ShapeOrientation();

    /**
     * Creates a new orientation with the given properties.
     *
     * @param properties the properties to use for this orientation.
     * @return an orientation with the given properties.
     */
    public ShapeOrientation forProperties(ShapeOrientationProperty.Value<?>... properties) {
        return new ShapeOrientation(properties);
    }

    /**
     * Applies this orientation to a block state.
     *
     * @param state the state to apply this orientation to.
     * @return the state with this orientation applied.
     */
    public BlockState applyToState(BlockState state) {
        for (ShapeOrientationProperty.Value<?> property : this.properties) {
            state = property.applyToState(state);
        }
        return state;
    }

}
