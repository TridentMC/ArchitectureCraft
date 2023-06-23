package com.tridevmc.architecture.common.shape.orientation;

/**
 * Defines an object that stores a set of properties used to orient a shape in the world, should be heavily cached to avoid heap bloat.
 */
public record ShapeOrientation(
        ShapeOrientationProperty... properties
) {

    public static final ShapeOrientation IDENTITY = new ShapeOrientation();


}
